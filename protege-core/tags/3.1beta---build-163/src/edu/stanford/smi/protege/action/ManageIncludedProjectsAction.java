package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ManageIncludedProjectsAction extends StandardAction {
    public ManageIncludedProjectsAction() {
        super(ResourceKey.PROJECT_MANAGE_INCLUDED);
    }
    
    public void actionPerformed(ActionEvent event) {
        System.out.println("manage included projects");
    }
}
