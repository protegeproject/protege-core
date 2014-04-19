package edu.stanford.smi.protege.util;

import java.util.Collection;

import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.ProjectViewEvent.Type;
import edu.stanford.smi.protege.widget.Widget;

public class ProjectViewDispatcher implements EventDispatcher {

	public void postEvent(Collection listeners, Object source, int type,
			Object arg1, Object arg2, Object arg3) {
		Type etype = Type.typeFromOrdinal(type);
		ProjectViewEvent event = new ProjectViewEvent((ProjectView) source, etype);
		switch (etype) {
		case addTab:
			event.setWidget((Widget) arg1);
		}
		for (ProjectViewListener listener : (Collection<ProjectViewListener>) listeners) {
			switch (etype) {
			case addTab:
				listener.tabAdded(event);
                break;
            case save:
                listener.saved(event);
                break;
            case close:
                listener.closed(event);
                break;
			default:
				Log.getLogger().warning("Unknown event type " + etype + "/" + type);
				return;
			}
		}
	}

}
