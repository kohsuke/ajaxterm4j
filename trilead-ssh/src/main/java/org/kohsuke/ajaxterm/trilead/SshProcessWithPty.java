package org.kohsuke.ajaxterm.trilead;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Session;
import org.kohsuke.ajaxterm.ProcessWithPty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wraps SSH {@link Session} as {@link ProcessWithPty}.
 *
 * @author Kohsuke Kawaguchi
 */
public class SshProcessWithPty extends ProcessWithPty {
    private final Session ssh;

    public SshProcessWithPty(Session ssh) {
        this.ssh = ssh;
    }

    @Override
    public void setWindowSize(int width, int height) throws IOException {
        ssh.requestWindowChange(width, height, 0, 0);
    }

    @Override
    public OutputStream getOutputStream() {
        return ssh.getStdin();
    }

    @Override
    public InputStream getErrorStream() {
        return ssh.getStderr();
    }

    @Override
    public InputStream getInputStream() {
        return ssh.getStdout();
    }

    @Override
    public void kill(int signal) throws IOException {
        ssh.signal(signal);
    }

    @Override
    public int exitValue() {
        Integer v = ssh.getExitStatus();
        if (v==null)    throw new IllegalThreadStateException();
        return v;
    }

    @Override
    public boolean isAlive() {
        return ssh.getExitStatus()==null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        ssh.waitForCondition(ChannelCondition.EXIT_STATUS,0);
        return ssh.getExitStatus();
    }

    @Override
    public void destroy() {
        try {
            kill(9);    // unblockable kill signal
        } catch (IOException e) {
            // swallow the error in case the connection was already terminated, etc.
        }
    }
}
