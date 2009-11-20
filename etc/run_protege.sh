#!/bin/sh

# ------------------- Where is Java? ------------------- 

# Change to the script' working directory, should be the Protege root directory 
cd $(dirname $0)


DARWIN="false"

if [  -x /usr/bin/uname ]
then
  if [ "x`/usr/bin/uname`" = "xDarwin" ] 
  then
    DARWIN="true"
  fi
fi

if [ ${DARWIN} = "true" ]
then
  JAVA_PATH=/usr/bin
else 
  # Attempt to use the bundled VM if none specified
  if [ "$JAVA_HOME" = "" ]; then
	JAVA_HOME=.
  fi

  JAVA_PATH=$JAVA_HOME/jre/bin
fi

# Check if the Java VM can be found 
if [ ! -e $JAVA_PATH/java ]; then
	echo Java VM could not be found. Please check your JAVA_HOME environment variable.
	exit 1
fi
# ------------------- Where is Java? ------------------- 

JARS=protege.jar:driver.jar:driver1.jar:looks.jar:unicode_panel.jar
MAIN_CLASS=edu.stanford.smi.protege.Application

# ------------------- JVM Options ------------------- 
MAXIMUM_MEMORY=-Xmx500M
OPTIONS=$MAXIMUM_MEMORY

#DELAY="-Dserver.delay=80 -Dserver.upload.kilobytes.second=128 -Dserver.download.kilobytes.second=500"
LOG4J_OPT="-Dlog4j.configuration=file:log4j.xml"

#Possible instrumentation options - debug, etc.
#DEBUG_OPT="-agentlib:jdwp=transport=dt_socket,address=8100,server=y,suspend=n"
# For yjp remember to set the LDLIBRARY path
# e.g export 
#      LD_LIBRARY_PATH=/home/tredmond/dev/packages/yjp-7.5.6/bin/linux-x86-32
#YJP_OPT="-agentlib:yjpagent=port=8142"


OPTIONS="${OPTIONS} ${DEBUG_OPT} ${YJP_OPT} ${DELAY} ${LOG4J_OPT}"
# ------------------- JVM Options ------------------- 

# Run Protege
$JAVA_PATH/java $OPTIONS -cp $JARS $MAIN_CLASS $1
