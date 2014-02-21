package org.kohsuke.ajaxterm.demo;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.InteractiveCallback;
import org.kohsuke.ajaxterm.Session;
import org.kohsuke.ajaxterm.trilead.SshProcessWithPty;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;

import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class AjaxTerm {
    Session session;

    public final AdjunctManager adjuncts;

    public AjaxTerm(ServletContext context) {
        adjuncts = new AdjunctManager(context,getClass().getClassLoader(),"adjuncts");
    }

    /**
     * When '/' is requested, show the login page or the terminal page depending on whether the session has already been created.
     */
    public HttpResponse doIndex() {
        if (session!=null)
            return HttpResponses.forwardToView(this,"session.gsp");
        else
            return HttpResponses.forwardToView(this,"login.gsp");
    }

    /**
     * This shuttles page update and keyboard interactions.
     */
    public void doU(StaplerRequest req, StaplerResponse rsp) throws Exception {
        session.handleUpdate(req,rsp);
    }

    /**
     * Start a terminal with a local shell.
     */
    public HttpResponse doLocal(
                    @QueryParameter int w,
                    @QueryParameter int h) throws Exception {
        destroy();
        session = new Session(w,h,Session.getAjaxTerm(),"/bin/bash","--login");

        return HttpResponses.redirectToDot();
    }

    /**
     * Start a terminal with remote SSH.
     */
    public HttpResponse doLogin(@QueryParameter String host, @QueryParameter int port, @QueryParameter String user, @QueryParameter final String password,
                                @QueryParameter int w,
                                @QueryParameter int h) throws Exception {
        Connection con = new Connection(host, port);
        if (!con.authenticateWithPassword(user,password))
            con.authenticateWithKeyboardInteractive(user,new InteractiveCallback() {
                public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo) throws Exception {
                    return new String[]{password};
                }
            });
        if (!con.isAuthenticationComplete())
            throw new IOException("Authentication failed");

        com.trilead.ssh2.Session s = con.openSession();
        s.requestPTY(Session.getAjaxTerm(), w,h,0,0,null);
        s.startShell();

        destroy();
        session = new Session(w,h,new SshProcessWithPty(s));

        return HttpResponses.redirectToDot();
    }

    /**
     * If there's any existing session, destroy it.
     */
    private void destroy() {
        if (session!=null)
            session.getChildProcess().destroy();
    }
}
