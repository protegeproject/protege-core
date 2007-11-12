rem This is an example Windows batch file for running
rem Protege from the command line.  It assumes that 
rem you have a Java JRE installed somewhere.

rem Note that this file is not used when you run Protege "normally"
rem by, for example, double clicking on a .pprj file. 

@echo off
set JAVA_PATH=.\jre\bin
set JARS=protege.jar;looks-2.1.3.jar;unicode_panel.jar;driver.jar;driver1.jar;driver2.jar;plugins/edu.stanford.smi.protegex.changes/change-model.jar
set MAIN_CLASS=edu.stanford.smi.protege.Application


rem ------------------- JVM Options ------------------- 
set MAXIMUM_MEMORY=-Xmx200M
OPTIONS=%MAXIMUM_MEMORY%

rem Possible instrumentation options - debug, etc.
rem set DEBUG_OPT="-Xdebug -Xrunjdwp:transport=dt_socket,address=8100,server=y,suspend=n"
rem set JCONSOLE="-Dcom.sun.management.jmxremote"
rem set PORTOPTS="-Dprotege.rmi.server.port=5200 -Dprotege.rmi.registry.port=5100 -Dprotege.rmi.server.local.port=2388"
rem set SSLOPTS="-Dprotege.rmi.usessl=true -Djavax.net.ssl.trustStore=protegeca -Djavax.net.ssl.trustStorePassword=protege"
set LOG4J_OPT=-Dlog4j.configuration=file:log4j.xml

set OPTIONS=%OPTIONS% %DEBUG_OPT% %PORT_OPTS% %SSLOPTS% %LOG4J_OPT%

rem ------------------- JVM Options ------------------- 


%JAVA_PATH%\java %OPTIONS% -cp %JARS% %MAIN_CLASS% %1
