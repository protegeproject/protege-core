tell application "Terminal"
	do script with command "cd /Applications/Protege_3.3; /System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Commands/java -Xmx250M -Dapple.laf.useScreenMenuBar=true -Xdock:name=Protege -cp protege.jar:looks-2.1.3.jar:unicode_panel.jar:driver.jar edu.stanford.smi.protege.Application"
end tell
