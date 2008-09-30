/**
 * 
 */
package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.event.ServerProjectNotificationEvent;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.EventGeneratorFrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.util.ProtegeJob;

public class ProjectNotifyJob extends ProtegeJob {
    private static final long serialVersionUID = 8247122286715823281L;
    
    String message;
    
    public ProjectNotifyJob(KnowledgeBase kb, String message) {
        super(kb);
        this.message = message;
    }
    
    @Override
    public Object run() throws ProtegeException {
        if (!serverSideCheckOperationAllowed(MetaProjectConstants.OPERATION_CONFIGURE_SERVER)) {
            return null;
        }
        String projectName = getMetaProjectInstance().getName();
        FrameStoreManager fsm = getKnowledgeBase().getFrameStoreManager();
        EventGeneratorFrameStore eventGenerator = fsm.getFrameStoreFromClass(EventGeneratorFrameStore.class);
        eventGenerator.addCustomEvent(new ServerProjectNotificationEvent(projectName, message));
        return null;
    }
}