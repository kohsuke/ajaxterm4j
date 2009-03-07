package org.kohsuke.ajaxterm;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author Kohsuke Kawaguchi
 */
public class AjaxTerm {
    Session session;

    public void doU(StaplerResponse rsp,
                    @QueryParameter String s,
                    @QueryParameter String k,
                    @QueryParameter boolean c,
                    @QueryParameter int w,
                    @QueryParameter int h) throws Exception {
        if(session==null)
            session = new Session(80,25);
        if(k!=null) {
            session.out.write(k);
            session.out.flush();
        }
        Thread.sleep(20);

        rsp.setContentType("application/xml");
        Terminal t = session.terminal;
        if(t.showCursor) {
            rsp.addHeader("Cursor-X",String.valueOf(t.getCx()));
            rsp.addHeader("Cursor-Y",String.valueOf(t.getCy()));
        }
        rsp.getWriter().println(t.dumpHtml(c));
    }
}
