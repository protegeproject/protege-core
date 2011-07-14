package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * A renderer used by the RoleWidget to display the "abstract" icon after the word abstract.  This hope is that this
 * will more easily allow the user to make the link between this role and the same icon on the classes panel.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class RoleRenderer extends DefaultRenderer {

    private static final long serialVersionUID = 1044368024395724864L;

    public void load(Object o) {
        setMainText(o.toString());
        boolean isAbstract = o.equals(RoleConstraint.ABSTRACT);
        appendIcon(Icons.getClsIcon(false, isAbstract, false, false));
    }
}
/*
class RoleRenderer extends BasicComboBoxRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    	super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        boolean isAbstract = value.equals(RoleConstraint.ABSTRACT);
        Icon icon = Icons.getClsIcon(false, isAbstract, false);
        setIcon(icon);
    	return this;
	}
}
*/
