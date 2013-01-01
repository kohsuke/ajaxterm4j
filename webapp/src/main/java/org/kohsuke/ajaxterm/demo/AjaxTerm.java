package org.kohsuke.ajaxterm.demo;

import org.kohsuke.ajaxterm.Session;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Kohsuke Kawaguchi
 */
public class AjaxTerm {
    Session session;

    public HttpResponse doIndex() {
        return HttpResponses.redirectTo("ajaxterm.html");
    }

    public void doU(StaplerRequest req, StaplerResponse rsp,
                    @QueryParameter String s,
                    @QueryParameter int w,
                    @QueryParameter int h) throws Exception {
        if(session==null)
            session = new Session(w,h,"/bin/bash","--login");
        session.handleUpdate(req,rsp);
    }
}
