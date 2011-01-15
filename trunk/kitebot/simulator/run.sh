#!/bin/sh

#runs from .class files generated by eclipse project into bin
java -Djava.security.policy=applet/java.policy.applet -Dfile.encoding=UTF-8 -classpath bin:lib/jcommon-1.0.16.jar:lib/jfreechart-1.0.13.jar sun.applet.AppletViewer applet/applet.html

#runs from jar generated manually with eclipse found at lib/kitebot.jar
# java -Djava.security.policy=applet/java.policy.applet -Dfile.encoding=UTF-8 -classpath lib/kitebot.jar:lib/jcommon-1.0.16.jar:lib/jfreechart-1.0.13.jar sun.applet.AppletViewer applet/applet.html

#runs with sun java explicitly.  Probably needed if using openjdk that comes with recent distributions (e.g. ubuntu 10.04+) which doesn't do applets well
# /usr/lib/jvm/java-6-sun/bin/java -Djava.security.policy=applet/java.policy.applet -Dfile.encoding=UTF-8 -classpath lib/kitebot.jar:lib/jcommon-1.0.16.jar:lib/jfreechart-1.0.13.jar sun.applet.AppletViewer applet/applet.html


