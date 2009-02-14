package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.KnowledgeBase;

public interface ProjectFixupPlugin extends Plugin {
	
	public void fixProject(KnowledgeBase internalKB);
}
