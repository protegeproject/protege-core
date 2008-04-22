package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * This action removes the "inclusion" flag from all frames.  When the current
 * project is saved it will then contain copies of the frames of all the included projects.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MergeIncludedProjects extends LocalProjectAction {

    public MergeIncludedProjects() {
        super(ResourceKey.PROJECT_MERGE_INCLUDED);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().mergeIncludedProjectsRequest();
    }
}
