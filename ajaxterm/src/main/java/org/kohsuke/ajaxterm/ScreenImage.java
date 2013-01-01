package org.kohsuke.ajaxterm;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * State of the virtual screen held by the terminal.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ScreenImage implements Serializable {
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

    public final int cursorX, cursorY, screenX, screenY;


    public ScreenImage(int timestamp, String screen, Terminal t) {
        this.timestamp = timestamp;
        this.screen = screen;
        if (t.showCursor) {
            this.cursorX = t.getCx();
            this.cursorY = t.getCy();
        } else {
            this.cursorX = this.cursorY = -1;
        }
        this.screenX = t.width;
        this.screenY = t.height;
    }

    public void renderResponse(HttpServletResponse rsp) throws IOException {
        rsp.setContentType("application/xml;charset=UTF-8");
        if(this.cursorX!=-1 || this.cursorY!=-1) {
            rsp.addHeader("Cursor-X",String.valueOf(cursorX));
            rsp.addHeader("Cursor-Y",String.valueOf(cursorY));
        }
        rsp.addHeader("Screen-X",String.valueOf(screenX));
        rsp.addHeader("Screen-Y", String.valueOf(screenY));
        rsp.addHeader("Screen-Timestamp", String.valueOf(timestamp));
        rsp.getWriter().println(screen);
    }

    private static final long serialVersionUID = 1L;
}
