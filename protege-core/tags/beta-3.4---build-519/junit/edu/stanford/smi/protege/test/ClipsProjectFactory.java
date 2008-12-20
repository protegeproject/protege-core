package edu.stanford.smi.protege.test;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.storage.clips.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClipsProjectFactory extends AbstractProjectFactory {
    public Project createProject() {
        Collection errors = new ArrayList();
        Project project = Project.createNewProject(new ClipsKnowledgeBaseFactory(), errors);
        checkErrors(errors);
        return project;
    }
}
