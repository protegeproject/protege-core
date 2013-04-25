package edu.stanford.smi.protege.util;

import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.widget.Widget;

public class ProjectViewEvent extends AbstractEvent {
	private static final long serialVersionUID = -7883249925606924610L;


    public enum Type {
		addTab, removeTab, save, close;
		
		static Type typeFromOrdinal(int ordinal) {
			for (Type t : Type.values()) {
				if (t.ordinal() == ordinal) {
					return t;
				}
			}
			return null;
		}
	};
	
	private ProjectView projectView;
	private Type type;
	private Widget widget;
	
	public ProjectViewEvent(ProjectView projectView, Type type) {
		super(projectView, type.ordinal());
		this.projectView = projectView;
		this.type = type;
	}
	
	/**
	 * @return Returns the projectView.
	 */
	public ProjectView getProjectView() {
		return projectView;
	}
	/**
	 * @return Returns the type.
	 */
	public Type getType() {
		return type;
	}

	public void setWidget(Widget widget) {
		if (type != Type.addTab) {
			throw new UnsupportedOperationException("Adding a widget to a " + type + " event?");
		}
		this.widget = widget;
	}
	
	
	public Widget getWidget() {
	  return widget;
	}
}
