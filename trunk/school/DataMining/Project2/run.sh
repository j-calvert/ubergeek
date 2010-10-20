#! /bin/bash
PROJECT_DIR=$(dirname $(realpath $0))
TEST_CLASS="edu.washington.cs.dm.jeremyc.netflix.PredictorTester"
JAVA=$(type -p java)

JAVA_MEM_OPT="-Xmx256m -XX:MaxPermSize=124m"

CLASSPATH=${PROJECT_DIR}/build

export CLASSPATH

exec $JAVA $JAVA_MEM_OPT $TEST_CLASS $*

exit 0

