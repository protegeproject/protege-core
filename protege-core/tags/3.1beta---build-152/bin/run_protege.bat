rem This is an example Windows batch file for running
rem Protege from the command line.  It assumes that 
rem you have a Java JRE installed somewhere.

rem Note that this file is not used when you run Protege "normally"
rem by, for example, double clicking on a .pprj file. 

@echo off
set JAVA_PATH=.\jre\bin
set JARS=protege.jar;looks.jar;unicode_panel.jar;driver.jar;driver1.jar;driver2.jar
set MAIN_CLASS=edu.stanford.smi.protege.Application

set MAX_MEMORY=-Xmx100M 
set OPTIONS= %MAX_MEMORY%

%JAVA_PATH%\java %OPTIONS% -cp %JARS% %MAIN_CLASS% %1
