import subprocess

def is_python_running():
    """Returns True if a Python program is running, False otherwise."""

    p = subprocess.Popen(["ps", "aux"], stdout=subprocess.PIPE)
    output = p.communicate()[0]
    for line in output.splitlines():
        if bytes("python", 'utf-8') in line:
            print(line.decode('utf-8'))
            return True
    return False

if __name__ == "__main__":
    if is_python_running():
        print("A Python program is running.")
    else:
        print("No Python program is running.")