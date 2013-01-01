package org.kohsuke.ajaxterm;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * C library interface.
 * 
 * @author Kohsuke Kawaguchi
 */
public interface CLibrary extends Library {
    void setsid();
    int dup(int fd);
    void close(int fd);
    int setenv(String name, String value, int replace);
    int execve(String filename, String[] argv, String[] env);
    int execv(String filename, String[] argv);
    void kill(int pid, int signal);
    int fcntl(int fd, int cmd, int v);
    int getdtablesize();

    int waitpid(int pid, IntByReference status, int options);
    int WNOHANG = 1;

    int F_GETFD = 1;
    int F_SETFD = 2;
    int FD_CLOEXEC = 1;
    
    public static CLibrary LIBC = (CLibrary) Native.loadLibrary("c",CLibrary.class);
}
