package edu.stanford.smi.protege.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class ClientConnectMain {
    private static Logger log = Log.getLogger(ClientConnectMain.class);
    
    private String host;
    private String user;
    private String password;
    private String projectName;
    
    private JFrame mainFrame;
   
    
    public ClientConnectMain(String[] args) {
        host = args[0];
        user = args[1];
        password = args[2];
        projectName = args[3];
    }
    
    public void connect() {
        RemoteProjectManager rpm = RemoteProjectManager.getInstance();
        Project p = rpm.getProject(host, user, password, projectName, true);
        initializeProtegeGui();
        ProjectManager.getProjectManager().setCurrentProject(p, true, false);
    }
    
    private void initializeProtegeGui() {
        ThreadGroup group = new ThreadGroup(Thread.currentThread().getThreadGroup(), "Safe Thread Group") {
            public void uncaughtException(Thread thread, Throwable throwable) {
                Log.getLogger().log(Level.SEVERE, "Uncaught Exception", throwable);
            }
        };
        SystemUtilities.initGraphics();
        mainFrame = ComponentFactory.createMainFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                ProjectManager.getProjectManager().exitApplicationRequest();
            }
        });

        ProjectManager.getProjectManager().setRootPane(mainFrame.getRootPane());
        ApplicationProperties.restoreMainFrameProperties(mainFrame);
        mainFrame.setVisible(true);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            log.info("Usage: ClientConnect host/port user password project-name");
            System.exit(-1);
        }
        ClientConnectMain connectJob = new ClientConnectMain(args);
        connectJob.connect();
        // We have to give the thread time to get going or the entire application will exit!
        SystemUtilities.sleepMsec(5000);
    }

}
