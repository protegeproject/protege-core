set JDKBIN=jre\bin

rem Note that a space character in the following path must be replaced with '%%20' in 
rem a batch file.  If you are typing directly on the command line, a space must be
rem replaced with '%20'.

set CODEBASE_URL=file:/c:/program%%20files/protege_3.5/protege.jar

start /D%JDKBIN% /min rmiregistry.exe

set CLASSPATH=protege.jar;looks.jar;unicode_panel.jar;driver.jar;driver0.jar;driver1.jar
set MAINCLASS=edu.stanford.smi.protege.server.Server
set METAPROJECT=examples\server\metaproject.pprj

set MAX_MEMORY=-Xmx500M 
set HEADLESS=-Djava.awt.headless=true
set CODEBASE=-Djava.rmi.server.codebase=%CODEBASE_URL%
set LOG4J_OPT=-Dlog4j.configuration=file:log4j.xml

rem --- Optional arguments; uncomment if necessary ---
rem set HOSTNAME=-Djava.rmi.server.hostname=localhost
rem set "PORTOPTS=-Dprotege.rmi.server.port=5200 -Dprotege.rmi.registry.port=5100"
rem TX=-Dtransaction.level=READ_COMMITTED
rem "DEBUG_OPT=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

set OPTIONS=%MAX_MEMORY% %CODEBASE% %HEADLESS% %LOG4J_OPT% %HOSTNAME% %PORTOPTS% %TX% %DEBUG_OPT%

rem ------------------- Cmd Options -------------------
rem If you want automatic saving of the project, 
rem setup the number of seconds in SAVE_INTERVAL_VALUE
rem set SAVE_INTERVAL=-saveIntervalSec=120
rem ------------------- Cmd Options -------------------


%JDKBIN%\java %OPTIONS% -cp %CLASSPATH% %MAINCLASS% %SAVE_INTERVAL% %METAPROJECT%  
