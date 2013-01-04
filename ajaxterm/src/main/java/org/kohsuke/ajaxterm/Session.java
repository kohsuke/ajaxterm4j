package org.kohsuke.ajaxterm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
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
    private final ProcessWithPty childProcess;

    private final Terminal terminal;

    private final long time = System.currentTimeMillis();

    /**
     * When was this session accessed the last time?
     */
    private long lastAccess;

    private final Reader in;
    private final Writer out;

    /**
     * Creates a terminal session that pumps message between the child process and {@link Terminal}.
     *
     *
     * @param width
     *      Width of the terminal. For example, 80.
     * @param height
     *      Height of the terminal. For example, 25.
     * @param terminal
     *      Terminal name set to the TERM environment variable.
     *      For the JavaScript terminal implemented in ajaxterm.js, specify the value taken from {@link #getAjaxTerm()}
     * @param commands
     *      Command line arguments of the process to launch.
     *      {"/bin/bash","--login"} for example.
     */
    public Session(int width, int height, String terminal, String... commands) throws IOException {
        this(width, height, new PtyProcessBuilder().commands(commands).env("TERM",terminal).forkWithHelper());
    }

    /**
     *
     * @param width
     *      Width of the terminal. For example, 80.
     * @param height
     *      Height of the terminal. For example, 25.
     * @param childProcessWithTty
     *      A child process forked with pty as its stdin/stdout.
     *      Normally this needs to be created with {@link PtyProcessBuilder}.
     *      Make sure to set the correct terminal name in its environment variable.
     *
     * @see PtyProcessBuilder
     */
    public Session(int width, int height, ProcessWithPty childProcessWithTty) throws IOException {
        this.terminal = new Terminal(width,height);
        this.childProcess = childProcessWithTty;
        childProcess.setWindowSize(width,height);

        in = new InputStreamReader(childProcess.getInputStream());
        out = new OutputStreamWriter(childProcess.getOutputStream());

        setName("Terminal pump thread for "+ childProcessWithTty);
        start(); // start pumping
    }

    public Terminal getTerminal() {
        return terminal;
    }

    /**
     * When was this session accessed by the client the last time?
     */
    public long getLastAccess() {
        return lastAccess;
    }

    /**
     * When was this session allocated?
     */
    public long getTime() {
        return time;
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
        } catch (IOException e) {
            // fd created by forkpty seems to cause I/O error when the other side is closed via kill -9
            if (!hasChildProcessFinished())
                LOGGER.log(Level.WARNING, "Session pump thread is dead", e);
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    private boolean hasChildProcessFinished() {
        try {
            childProcess.exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            return false;
        }
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

    public Process getChildProcess() {
        return childProcess;
    }

    /**
     * Name of the terminal ajaxterm.js is implmenting.
     *
     * A static method instead of a constant to avoid compile-time bake-in to the client code.
     */
    public static String getAjaxTerm() {
        return "linux";
    }

    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
}
