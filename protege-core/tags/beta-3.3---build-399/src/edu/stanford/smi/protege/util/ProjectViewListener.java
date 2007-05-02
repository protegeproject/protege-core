package edu.stanford.smi.protege.util;

import java.util.EventListener;

public interface ProjectViewListener extends EventListener {

	void tabAdded(ProjectViewEvent event);

    void saved(ProjectViewEvent event);

    void closed(ProjectViewEvent event);
	
	
}
