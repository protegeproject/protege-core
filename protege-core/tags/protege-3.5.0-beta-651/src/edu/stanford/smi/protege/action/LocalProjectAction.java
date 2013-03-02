package edu.stanford.smi.protege.action;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class LocalProjectAction extends ProjectAction {

    private static final long serialVersionUID = -4106992321190276536L;

    protected LocalProjectAction(ResourceKey key) {
        this(key, false);
    }

    protected LocalProjectAction(ResourceKey key, boolean useLargeIcons) {
        super(key, useLargeIcons);
        if (isEnabled()) {
            Project p = getProject();
            boolean isLocal = p != null && !p.isMultiUserClient();
            if (!isLocal) {
                setEnabled(false);
            }
        }
    }
}
