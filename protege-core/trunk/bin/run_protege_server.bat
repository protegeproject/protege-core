set JDKBIN=jre\bin

rem Note that a space character in the following path must be replaced with '%%20' in 
rem a batch file.  If you are typing directly on the command line, a space must be
rem replaced with '%20'.

set CODEBASE_URL=file:/c:/program%%20files/protege_3.3_beta/protege.jar

start /min %JDKBIN%\rmiregistry

set CLASSPATH=protege.jar;looks-2.1.3.jar;unicode_panel.jar;driver.jar;driver0.jar;driver1.jar;plugins/edu.stanford.smi.protegex.changes/change-model.jar
set MAINCLASS=edu.stanford.smi.protege.server.Server
set METAPROJECT=examples\server\metaproject.pprj

set MAX_MEMORY=-Xmx100M 
set CODEBASE=-Djava.rmi.server.codebase=%CODEBASE_URL%

set OPTIONS=%MAX_MEMORY% %CODEBASE%

%JDKBIN%\java %OPTIONS% -cp %CLASSPATH% %MAINCLASS% %METAPROJECT%  
