import socket
import sys
import time

def get_lock(process_name):
    print(process_name)
    # Without holding a reference to our socket somewhere it gets garbage
    # collected when the function exits
    get_lock._lock_socket = socket.socket(socket.AF_UNIX, socket.SOCK_DGRAM)

    try:
        # The null byte (\0) means the socket is created 
        # in the abstract namespace instead of being created 
        # on the file system itself.
        # Works only in Linux
        get_lock._lock_socket.bind('\0' + process_name)
        print ('I got the lock')
    except socket.error:
        print ('lock exists')
        sys.exit()

if __name__ == "__main__":
    get_lock("pumpControl.py")