package org.kohsuke.ajaxterm;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Kohsuke Kawaguchi
 */
public class AjaxTerm {
    Session session;

    public void doU(StaplerRequest req, StaplerResponse rsp,
                    @QueryParameter String s,
                    @QueryParameter int w,
                    @QueryParameter int h) throws Exception {
        if(session==null)
            session = new Session(w,h,"/bin/bash","--login");
        session.handleUpdate(req,rsp);
    }
}
