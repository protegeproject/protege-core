rem This is the shutdown script for the Protege that should be run on a server console.
rem It will save all the server projects.
rem In case your server does not run on the default port, then provide the server name and the port as an argument for the script.
rem E.g. shutdown_protege_server.bat localhost:5200

@echo off
set JDKBIN=jre\bin

set CLASSPATH=protege.jar;looks.jar;unicode_panel.jar;driver.jar;driver0.jar;driver1.jar
set MAINCLASS=edu.stanford.smi.protege.server.Shutdown

set MAX_MEMORY=-Xmx200M 

set OPTIONS=%MAX_MEMORY%

%JDKBIN%\java %OPTIONS% -cp %CLASSPATH% %MAINCLASS% %1


 