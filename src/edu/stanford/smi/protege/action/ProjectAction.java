package edu.stanford.smi.protege.action;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Base class for actions that access the current project.  By default instances of this class are disabled
 * if no project is loaded.  To override this call #setEnabled(boolean) in the constructor. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ProjectAction extends StandardAction {

    {
        setEnabled(getProjectManager().getCurrentProject() != null);
    }
    
    public ProjectAction(String text) {
        super(text);
    }
    
    public ProjectAction(String label, Icon icon) {
        super(label, icon);
    }
    
    public ProjectAction(ResourceKey key) {
        super(key);
    }
    
    public ProjectAction(ResourceKey key, boolean useLargeIcon) {
        super(key, useLargeIcon);
    }
    
    protected ProjectManager getProjectManager() {
        return ProjectManager.getProjectManager();
    }

    protected Project getProject() {
        return getProjectManager().getCurrentProject();
    }

    protected KnowledgeBase getKnowledgeBase() {
        Project p = getProject();
        return (p == null) ? null : p.getKnowledgeBase();
    }

    protected ProjectView getProjectView() {
        return getProjectManager().getCurrentProjectView();
    }

    protected CommandManager getCommandManager() {
        KnowledgeBase kb = getKnowledgeBase();
        return (kb == null) ? null : kb.getCommandManager();
    }

    protected JComponent getMainPanel() {
        return getProjectManager().getMainPanel();
    }
}
