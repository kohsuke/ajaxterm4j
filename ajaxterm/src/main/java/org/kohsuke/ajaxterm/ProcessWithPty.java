package org.kohsuke.ajaxterm;

import java.io.IOException;

/**
 * {@link Process} with additional controls for pseudo-terminal.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ProcessWithPty extends Process {
    protected ProcessWithPty() {
    }

    public abstract void setWindowSize(int width, int height) throws IOException;

    public abstract void kill(int signal) throws IOException;

    /**
     * Is this process still alive?
     */
    public boolean isAlive() {
        try {
            exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            return false;
        }
    }
}
