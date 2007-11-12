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


CLASSPATH=protege.jar:looks-2.1.3.jar:unicode_panel.jar:driver.jar:driver0.jar:driver1.jar:driver2.jar:plugins/edu.stanford.smi.protegex.changes/change-model.jar
MAINCLASS=edu.stanford.smi.protege.server.Server


# ------------------- JVM Options ------------------- 
MAX_MEMORY=-Xmx200M
HEADLESS=-Djava.awt.headless=true
CODEBASE_URL=file:$PWD/protege.jar
CODEBASE=-Djava.rmi.server.codebase=$CODEBASE_URL
HOSTNAME_PARAM=-Djava.rmi.server.hostname=$HOSTNAME
TX="-Dtransaction.level=READ_COMMITTED"
LOG4J_OPT="-Dlog4j.configuration=file:log4j.xml"

OPTIONS="$MAX_MEMORY $HEADLESS $CODEBASE $HOSTNAME_PARAM ${TX} ${LOG4J_OPT}"

#
# Instrumentation debug, delay simulation,  etc
#
#DELAY="-Dserver.delay=80"
#PORTOPTS="-Dprotege.rmi.server.port=5200 -Dprotege.rmi.registry.port=5100"
#DEBUG_OPT="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

OPTIONS="${OPTIONS} ${DELAY} ${PORTOPTS} ${DEBUG_OPT}"
# ------------------- JVM Options ------------------- 

# ------------------- Cmd Options -------------------
# If you want automatic saving of the project, 
# setup the number of seconds in SAVE_INTERVAL_VALUE
# SAVE_INTERVAL=-saveIntervalSec=120
# ------------------- Cmd Options -------------------

METAPROJECT=examples/server/metaproject.pprj

$JAVA_PATH/rmiregistry &
$JAVA_PATH/java -cp $CLASSPATH $TX $OPTIONS $MAINCLASS $SAVE_INTERVAL $METAPROJECT
