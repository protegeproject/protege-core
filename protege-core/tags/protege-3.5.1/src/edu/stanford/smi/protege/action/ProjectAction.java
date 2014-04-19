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

    private static final long serialVersionUID = 6438135888112682135L;

    {
        setEnabled(getProjectManager().getCurrentProject() != null);
    }

    protected ProjectAction(String text) {
        super(text);
    }

    protected ProjectAction(String label, Icon icon) {
        super(label, icon);
    }

    protected ProjectAction(ResourceKey key) {
        super(key);
    }

    protected ProjectAction(ResourceKey key, boolean useLargeIcon) {
        super(key, useLargeIcon);
    }

    protected static ProjectManager getProjectManager() {
        return ProjectManager.getProjectManager();
    }

    protected static Project getProject() {
        return getProjectManager().getCurrentProject();
    }

    protected static KnowledgeBase getKnowledgeBase() {
        Project p = getProject();
        return (p == null) ? null : p.getKnowledgeBase();
    }

    protected static ProjectView getProjectView() {
        return getProjectManager().getCurrentProjectView();
    }

    protected static CommandManager getCommandManager() {
        KnowledgeBase kb = getKnowledgeBase();
        return (kb == null) ? null : kb.getCommandManager();
    }

    protected static JComponent getMainPanel() {
        return getProjectManager().getMainPanel();
    }
}
