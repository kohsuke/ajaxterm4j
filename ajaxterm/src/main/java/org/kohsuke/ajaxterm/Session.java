package org.kohsuke.ajaxterm;

import com.sun.jna.ptr.IntByReference;
import static org.kohsuke.ajaxterm.UtilLibrary.LIBUTIL;
import static org.kohsuke.ajaxterm.CLibrary.*;
import static org.kohsuke.ajaxterm.CLibrary.FD_CLOEXEC;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a session.
 *
 * <p>
 * A {@link Thread} is used to shuttle data back and force between the HTTP client
 * and the process that was forked. You can check the liveness of this thread to see
 * if the child process is still alive or not.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Session extends Thread {
    /**
     * PID of the child process.
     */
    private final int pid;

    /**
     * Exit code of the process.
     */
    private int exitCode;

    private final Terminal terminal;

    private final long time = System.currentTimeMillis();

    /**
     * When was this session accessed the last time?
     */
    private long lastAccess;

    private final Reader in;
    private final Writer out;

    /**
     *
     * @param width
     *      Width of the terminal. For example, 80.
     * @param height
     *      Height of the terminal. For example, 25.
     * @param commands
     *      Command line arguments of the process to launch.
     *      {"/bin/bash","--login"} for example.
     */
    public Session(int width, int height, String... commands) throws Exception {
        if(commands.length==0)
            throw new IllegalArgumentException("No command line arguments");
        this.terminal = new Terminal(width,height);

        // make execv call to force classloading
        // once we fork, the child process cannot load more classes reliably.
        LIBC.execv("-",new String[]{"-","-"});
        for( int i=LIBC.getdtablesize()-1; i>0; i-- ) {
            LIBC.fcntl(1,F_GETFD,0);
        }

        IntByReference pty = new IntByReference();
        pid = LIBUTIL.forkpty(pty, null, null, null);
        if(pid==0) {
            // on child process
            LIBC.setsid();
            for( int i=LIBC.getdtablesize()-1; i>=3; i-- ) {
                LIBC.fcntl(i, F_SETFD,LIBC.fcntl(i, F_GETFD,0)|FD_CLOEXEC);
            }

            LIBC.setenv("TERM","linux",1);
            LIBC.execv(commands[0],commands);
        }

        /*
        in = new FileReader(createFileDescriptor(pty.getValue()));
        out = new FileWriter(createFileDescriptor(pty.getValue()));
        */
        FileDescriptor fileDescriptor = createFileDescriptor(pty.getValue());
        in = new FileReader(fileDescriptor);
        out = new FileWriter(fileDescriptor);

        setName("Terminal pump thread for "+ Arrays.asList(commands));
        start(); // start pumping
    }

    private FileDescriptor createFileDescriptor(int v) throws NoSuchFieldException, IllegalAccessException {
        FileDescriptor fd = new FileDescriptor();
        Field f = FileDescriptor.class.getDeclaredField("fd");
        f.setAccessible(true);
        f.set(fd,v);
        return fd;
    }

    /**
     * When was this session accessed by the client the last time?
     */
    public long getLastAccess() {
        return lastAccess;
    }

    /**
     * PID of the forked child process.
     */
    public int getPID() {
        return pid;
    }

    /**
     * Exit code of the process, if the process has already terminated.
     *
     * If {@linkplain #isAlive() it's still running}, this method returns 0.
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * When was this session allocated?
     */
    public long getTime() {
        return time;
    }

    /**
     * Kills the process.
     */
    public void kill() throws IOException {
        in.close();
        out.close();
        LIBC.kill(pid,15/*SIGTERM*/);
    }

    @Override
    public void run() {
        char[] buf = new char[128];
        int len;

        try {
            while((len=in.read(buf))>=0) {
                terminal.write(new String(buf,0,len));
                String reply = terminal.read();
                if(reply!=null)
                    out.write(reply);
            }
            hasChildProcessFinished();
        } catch (IOException e) {
            if (!hasChildProcessFinished())
                LOGGER.log(Level.WARNING, "Session pump thread is dead", e);
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    private boolean hasChildProcessFinished() {
        IntByReference status = new IntByReference();
        boolean b = LIBC.waitpid(pid, status, WNOHANG) > 0;
        if (b) {
            int x = status.getValue();
            if ((x&0x7F)!=0)    exitCode=128+(x&0x7F);
            exitCode = (x>>8)&0xFF;
        }
        return b;
    }

    private void closeQuietly(Closeable c) {
        try {
            if (c!=null)    c.close();
        } catch (IOException e) {
            // silently ignore
        }
    }

    /**
     * Receives the call from the client-side JavaScript.
     */
    public void handleUpdate(HttpServletRequest req, HttpServletResponse rsp) throws IOException, InterruptedException {
        handleUpdate(
                req.getParameter("k"),
                req.getParameter("c") != null,
                Integer.parseInt(req.getParameter("t"))).renderResponse(rsp);
    }

    /**
     * Receives the call from the client-side JavaScript.
     */
    public ScreenImage handleUpdate(String keys, boolean color, int clientTimestamp) throws IOException, InterruptedException {
        lastAccess = System.currentTimeMillis();
        write(keys);
        Thread.sleep(20);   // give a bit of time to let the app respond. poor version of Nagel's algorithm

        terminal.setCssClass(isAlive() ? "":"dead");
        return terminal.dumpHtml(color,clientTimestamp);
    }

    /**
     * Write to the child process.
     */
    public void write(String k) throws IOException {
        if(k!=null && k.length()!=0) {
            out.write(k);
            out.flush();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
}
