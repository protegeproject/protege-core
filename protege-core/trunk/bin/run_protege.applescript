tell application "Terminal"
	do script with command "cd /Applications/Protege_3.1; java -Xmx250M 
-Dapple.laf.useScreenMenuBar=true -Xdock:name=Protege -cp protege.jar:looks.jar:unicode_panel.jar:driver.jar edu.stanford.smi.protege.Application"
end tell
