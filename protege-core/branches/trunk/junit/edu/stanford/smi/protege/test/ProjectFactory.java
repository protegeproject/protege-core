package edu.stanford.smi.protege.test;

import edu.stanford.smi.protege.model.*;

public interface ProjectFactory {
    Project createProject();
    Project saveAndReloadProject(Project p);
}
