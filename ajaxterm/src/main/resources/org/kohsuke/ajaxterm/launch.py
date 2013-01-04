#!/usr/bin/env python
import pty,os,sys,select,fcntl,termios,struct

def debug(msg):
    # print msg
    pass

cmd=sys.argv[1:]

pid, fd = os.forkpty()
if pid == 0:
    # Slave
    # os.chdir("/")
    # os.unsetenv("BOO")
    # os.putenv("FOO","xyz")
    os.execvp(cmd[0],cmd)

buf=""  # data sent from the parent process that we haven't processed yet

while True:
    rr,wr,xr=select.select([fd,0],[],[])
    for d in rr:
        if d==fd:
            # child process to parent
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
                    raise
        else:
            # parent to child
            buf += os.read(0,1024)
            debug("Got %d\n"%len(buf))
            while len(buf)>=3:
                # it takes at least 3 bytes for the header
                cmd,l=struct.unpack("!BH",buf[0:3])
                if len(buf)<3+l:
                    debug("skipping %d/%d/%d\n"%(len(buf),3+l,cmd))
                    break   # this command not fully read yet
                else:
                    payload=buf[3:(3+l)]
                    buf = buf[3+l:]

                    if cmd==1:
                        # data to child process
                        debug("writing %s:%d\n"%(payload,l))
                        os.write(fd,payload)
                    elif cmd==2:
                        # send signal
                        s=struct.unpack("!H",payload)[0]
                        os.kill(pid,s)
                    elif cmd==3:
                        # terminal size change
                        h,w=struct.unpack("!HH",payload)
                        fcntl.ioctl(fd, termios.TIOCSWINSZ, struct.pack("HHHH",h,w,0,0))
                    else:
                        sys.stderr.write("Unknown command: %d\n"%cmd)
