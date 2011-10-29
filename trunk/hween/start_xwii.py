#!/usr/bin/python

import os
import sys
import fcntl
pid_file = 'program.pid'
fp = open(pid_file, 'w')
try:
    fcntl.lockf(fp, fcntl.LOCK_EX | fcntl.LOCK_NB)
except IOError:
    # another instance is running
    sys.exit(0)

profile = "/usr/share/xwii/profiles/UG1.xwii"

os.chdir("/usr/share/xwii")

print "./xwii " + profile
print "Press 1 + 2 to put the Wiimote in discoverable mode."
x = os.popen("./xwii " + profile)
print x.read()

