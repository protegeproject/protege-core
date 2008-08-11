package edu.stanford.smi.protege.server;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.job.GetProjectStatusJob;
import edu.stanford.smi.protege.server.job.ProjectNotifyJob;
import edu.stanford.smi.protege.server.job.SetProjectStatusJob;
import edu.stanford.smi.protege.server.job.ShutdownProjectJob;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.ui.StatusBar;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

/**
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RemoteProjectUtil {
	private static transient Logger log = Log.getLogger(RemoteProjectUtil.class);

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
            @Override
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


    public static void shutdownProject(Project p, int warningTimeInSeconds) {
		int lastWarning = 5;
		int finalGracePeriodInSeconds = 7;

		int t = warningTimeInSeconds;
		ArrayList<Integer> ints = new ArrayList<Integer>();

		while (t > lastWarning) {
			ints.add(Integer.valueOf(t));
			if (t >= 120) {
				t = t/120 * 60;
			} else if (t >= 20) {
				t = t/20 * 10;
			} else if (t >= 10) {
				t = t/10 * 5;
			}
		}
		ints.add(Integer.valueOf(t));
		//ints.add(Integer.valueOf(0));

		shutdownProject(p, ints.toArray(new Integer[ints.size()]), finalGracePeriodInSeconds);
    }


	public static void shutdownProject(Project p, Integer[] warningTimesInSeconds, int finalGracePeriodInSeconds) {
	    if (!p.isMultiUserClient() && !p.isMultiUserServer()) {
	        log.warning("Hey - whats up? I can't find a server to shutdown");
	        return;
	    }
	    Arrays.sort(warningTimesInSeconds, new Comparator<Integer>() {

	        public int compare(Integer o1, Integer o2) {
	            return o2.compareTo(o1);
	        }

	    });
	    KnowledgeBase kb = p.getKnowledgeBase();
	    (new SetProjectStatusJob(kb, ProjectStatus.SHUTTING_DOWN)).execute();

	    for (int i = 0; i < warningTimesInSeconds.length; i++) {
	        int timeLeft = warningTimesInSeconds[i];
	        int timeToNextWarning = i == warningTimesInSeconds.length -1 ? timeLeft : timeLeft - warningTimesInSeconds[i + 1];
	        String message = "Project Shutdown in " + (timeLeft <= 60 ? ""  + timeLeft + " seconds" :  "" + timeLeft / 60 + " minutes");
	        new ProjectNotifyJob(kb, message).execute();
	        try {
	            Thread.sleep(timeToNextWarning * 1000);
	        }
	        catch (InterruptedException e) {
	            log.severe("Unexpected interrupt - you trying to kill me or what?");
	        }
	    }
	    (new SetProjectStatusJob(kb, ProjectStatus.CLOSED_FOR_MAINTENANCE)).execute();
	    if (finalGracePeriodInSeconds != 0) {
	        try {
	            Thread.sleep(finalGracePeriodInSeconds * 1000);
	        } catch (InterruptedException e) {
	            log.severe("Unexpected interrupt - you trying to kill me or what?");
	        }
	    }
	    (new ShutdownProjectJob(kb)).execute();
	}

	public static ProjectStatus getProjectStatus(KnowledgeBase kb, String project) {
		return (ProjectStatus) new GetProjectStatusJob(kb, project).execute();
	}

}
