package edu.stanford.smi.protege.server;

import java.awt.*;
import java.util.*;

import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RemoteProjectUtil {
    private static Thread thread;
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
                while (thread == this) {
                    try {
                        sleep(DELAY_MSEC);
                        updateStatus(project, bar);
                    } catch (InterruptedException e) {
                    }
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
