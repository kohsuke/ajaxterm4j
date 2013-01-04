package org.kohsuke.ajaxterm.demo;

import org.kohsuke.ajaxterm.Session;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;

import javax.servlet.ServletContext;

/**
 * @author Kohsuke Kawaguchi
 */
public class AjaxTerm {
    Session session;

    public final AdjunctManager adjuncts;

    public AjaxTerm(ServletContext context) {
        adjuncts = new AdjunctManager(context,getClass().getClassLoader(),"adjuncts");
    }

    public void doU(StaplerRequest req, StaplerResponse rsp,
                    @QueryParameter int w,
                    @QueryParameter int h) throws Exception {
        if(session==null)
            session = new Session(w,h,Session.getAjaxTerm(),"/bin/bash","--login");
        session.handleUpdate(req,rsp);
    }
}
