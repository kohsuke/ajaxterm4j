package org.kohsuke.ajaxterm4j;

import org.junit.Test;
import org.kohsuke.ajaxterm.Session;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class SessionTest {
    @Test
    public void basics() throws Exception {
        Session s = new Session(80,25,"/bin/bash","-i");
        assert s.isAlive();
        s.write("echo hello world\n");
        Thread.sleep(5000);
        String dump = s.getTerminal().dumpLatin1();
        assert dump.contains(" echo hello world");
        assert dump.contains("\nhello world");
        System.out.println(dump);
        s.write("exit 3\n");
        s.join(1000);
        assertThat(s.isAlive(),is(false));
        assertThat(s.getExitCode(), is(3));
    }
}
