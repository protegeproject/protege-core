package edu.stanford.smi.protege.server;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.ui.StatusBar;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RemoteProjectUtil {
    private static Thread thread;
    //ESCA-JAVA0077 
    private static final int DELAY_MSEC = 2000;

    public static void configure(ProjectView view) {
        StatusBar statusBar = new StatusBar();
        view.add(statusBar, BorderLayout.SOUTH);
        createUpdateThread((RemoteClientProject) view.getProject(), statusBar);
    }

    public static void dispose(ProjectView view) {
        thread = null;
    }

    private static void createUpdateThread(final RemoteClientProject project, final StatusBar bar) {
        thread = new Thread("Status Bar Updater") {
            public void run() {
              try {
                while (thread == this) {
                    try {
                        sleep(DELAY_MSEC);
                        updateStatus(project, bar);
                    } catch (InterruptedException e) {
                      Log.getLogger().log(Level.INFO, "Exception caught", e);
                    }
                }
              } catch (Throwable t) {
                Log.getLogger().log(Level.INFO, "Exception caught",t);
              }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private static void updateStatus(RemoteClientProject project, StatusBar bar) {
        Collection users = new ArrayList(project.getCurrentUsers());
        users.remove(project.getLocalUser());
        String userText = StringUtilities.commaSeparatedList(users);
        String text;
        if (userText.length() == 0) {
            text = "No other users";
        } else {
            text = "Other users: " + userText;
        }
        bar.setText(text);
    }
}
