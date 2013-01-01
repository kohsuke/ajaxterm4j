package org.kohsuke.ajaxterm.demo;

import org.kohsuke.stapler.Stapler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Kohsuke Kawaguchi
 */
public class WebAppMain implements ServletContextListener {
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Stapler.setRoot(servletContextEvent,new AjaxTerm(servletContextEvent.getServletContext()));
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
