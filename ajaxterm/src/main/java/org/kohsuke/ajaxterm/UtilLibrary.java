package org.kohsuke.ajaxterm;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * @author Kohsuke Kawaguchi
 */
public interface UtilLibrary extends Library {
    int forkpty(IntByReference master, Pointer _, Pointer termios, Pointer winp);

    public static UtilLibrary LIBUTIL = (UtilLibrary)Native.loadLibrary("util",UtilLibrary.class);
}
