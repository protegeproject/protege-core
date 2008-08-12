#!/bin/sh

# Change to the script' working directory, should be the Protege root directory 
cd $(dirname $0)

if [  -x /usr/bin/uname  -a "x`/usr/bin/uname`" == "xDarwin" ] 
then
  JAVA_PATH=/usr/bin
else 
  # Attempt to use the bundled VM if none specified
  if [ "$JAVA_HOME" == "" ]; then
	JAVA_HOME=.
  fi

  JAVA_PATH=$JAVA_HOME/jre/bin
fi

# Check if the Java VM can be found 
if [ ! -e $JAVA_PATH/java ]; then
	echo Java VM could not be found. Please check your JAVA_HOME environment variable.
	exit 1
fi

JARS=protege.jar:mysql-connector-java-5.0.0-beta-bin.jar:looks.jar:unicode_panel.jar
MAIN_CLASS=edu.stanford.smi.protege.Application
MAXIMUM_MEMORY=-Xmx1900M
OPTIONS=$MAXIMUM_MEMORY

#Possible instrumentation options - debug, etc.
DEBUG_OPT="-agentlib:jdwp=transport=dt_socket,address=8100,server=y,suspend=n"
export LD_LIBRARY_PATH=/home/tredmond/dev/packages/yjp-7.5.6/bin/linux-x86-32
YJP_OPT="-agentlib:yjpagent=port=8142"

# Run Protege
$JAVA_PATH/java $OPTIONS ${DEBUG_OPT} ${YJP_OPT} -cp $JARS $MAIN_CLASS $1
