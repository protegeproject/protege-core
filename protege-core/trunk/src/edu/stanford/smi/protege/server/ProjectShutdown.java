package edu.stanford.smi.protege.server;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.job.ProjectNotifyJob;
import edu.stanford.smi.protege.server.job.SetProjectStatusJob;
import edu.stanford.smi.protege.server.job.ShutdownProjectJob;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;

public class ProjectShutdown {
    private static Logger log = Log.getLogger(ProjectShutdown.class);
    
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
            int timeToNextWarning = (i == warningTimesInSeconds.length -1) ? timeLeft : timeLeft - warningTimesInSeconds[i + 1];
            String message = "Project Shutdown in " + ((timeLeft <= 60) ? ""  + timeLeft + " seconds" :  "" + (timeLeft / 60) + " minutes");
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

}
