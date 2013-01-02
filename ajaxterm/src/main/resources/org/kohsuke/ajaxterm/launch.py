#!/usr/bin/env python
import pty,os,sys,select,fcntl,termios,struct

w=sys.argv[1]
h=sys.argv[2]
cmd=sys.argv[3:]

pid, fd = os.forkpty()
if pid == 0:
    # Slave
    # os.chdir("/")
    # os.unsetenv("BOO")
    # os.putenv("FOO","xyz")
    os.execvp(cmd[0],cmd)

fcntl.ioctl(fd, termios.TIOCSWINSZ, struct.pack("HHHH",int(h),int(w),0,0))
                        
while True:
    rr,wr,xr=select.select([fd,0],[],[])
    for d in rr:
        if d==fd:
            try:
                os.write(1,os.read(fd,1024))
            except OSError, e:
                if e.errno==5:
                    # EOF from terminal
                    _,status=os.waitpid(pid,0)
                    if (status&0xFF)==0:
                        status = (status&0x7F00)>>8;
                    else:
                        status = 128+(status&0xFF);
                    sys.exit(status)
                else:
                    pass
        else:
            os.write(fd,os.read(0, 1024))
