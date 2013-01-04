package org.kohsuke.ajaxterm4j;

import org.junit.Test;
import org.kohsuke.ajaxterm.Session;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class SessionTest {
    @Test
    public void basics() throws Exception {
        Session s = new Session(80,25,Session.getAjaxTerm(),"/bin/bash","-i");
        assertThat(s.isAlive(), is(true));
        s.write("echo hello world\n");
        Thread.sleep(5000);
        String dump = s.getTerminal().dumpLatin1();
        assertThat(dump, containsString(" echo hello world"));
        assertThat(dump, containsString("\nhello world"));
        System.out.println(dump);
        s.write("exit 3\n");
        s.join(1000);
        assertThat(s.isAlive(), is(false));
        assertThat(s.getChildProcess().exitValue(), is(3));
    }
}
