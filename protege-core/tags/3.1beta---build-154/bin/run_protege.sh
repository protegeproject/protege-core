#!/bin/sh

# Change to the script' working directory, should be the Protege root directory 
cd $(dirname $0)

# Attempt to use the bundled VM if none specified
if [ "$JAVA_HOME" == "" ]; then
	JAVA_HOME=.
fi

JAVA_PATH=$JAVA_HOME/jre/bin

# Check if the Java VM can be found 
if [ ! -e $JAVA_PATH/java ]; then
	echo Java VM could not be found. Please check your JAVA_HOME environment variable.
	exit 1
fi

JARS=protege.jar:driver.jar:driver1.jar:looks.jar:unicode_panel.jar
MAIN_CLASS=edu.stanford.smi.protege.Application
MAXIMUM_MEMORY=-Xmx100M
OPTIONS=$MAXIMUM_MEMORY

# Run Protege
$JAVA_PATH/java $OPTIONS -cp $JARS $MAIN_CLASS $1
