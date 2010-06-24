#!/bin/sh

# ------------------- Where is Java? ------------------- 

# Change to the script' working directory, should be the Protege root directory 
cd $(dirname $0)

if [  -x /usr/bin/uname  -a  "x`/usr/bin/uname`" = "xDarwin" ] 
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


CLASSPATH=protege.jar:looks.jar:unicode_panel.jar:driver.jar:driver0.jar:driver1.jar:driver2.jar
MAINCLASS=edu.stanford.smi.protege.server.Shutdown


# ------------------- JVM Options ------------------- 
MAX_MEMORY=-Xmx100M

# ------------------- JVM Options ------------------- 

$JAVA_PATH/java -cp $CLASSPATH $MAINCLASS $1
