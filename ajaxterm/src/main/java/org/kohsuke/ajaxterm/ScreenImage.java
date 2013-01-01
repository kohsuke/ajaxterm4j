package org.kohsuke.ajaxterm;

/**
 * State of the virtual screen held by the terminal.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ScreenImage {
    /**
     * HTML dump of the screen image.
     */
    public final String screen;
    /**
     * Represents the timestamp of the screen image.
     *
     * This value should be passed to the client, and it should then send it back
     * for the next invocation of {@link Terminal#dumpHtml(boolean, int)} for
     * up-to-date check of the screen image.
     */
    public final int timestamp;

    public ScreenImage(int timestamp, String screen) {
        this.timestamp = timestamp;
        this.screen = screen;
    }
}
