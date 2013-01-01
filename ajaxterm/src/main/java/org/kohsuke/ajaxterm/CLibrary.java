package org.kohsuke.ajaxterm;

import com.sun.jna.Library;
import com.sun.jna.Memory;
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
    int unsetenv(String name);
    int execve(String filename, String[] argv, String[] env);
    int execv(String filename, String[] argv);
    void kill(int pid, int signal);
    int fcntl(int fd, int cmd, int v);
    int getdtablesize();
    void chdir(String dir);

    int ioctl(int fd, int cmd, Memory arg);
    int TIOCSWINSZ = 0x5414;    // taken from Linux, hopefully the same across the board

    int waitpid(int pid, IntByReference status, int options);
    int WNOHANG = 1;

    int F_GETFD = 1;
    int F_SETFD = 2;
    int FD_CLOEXEC = 1;
    
    public static CLibrary LIBC = (CLibrary) Native.loadLibrary("c",CLibrary.class);
}
