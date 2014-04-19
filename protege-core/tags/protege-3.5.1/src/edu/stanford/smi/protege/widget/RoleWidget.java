package edu.stanford.smi.protege.widget;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for acquiring the "role" (abstract or concrete) of a class.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RoleWidget extends ComboBoxWidget {

    private static final long serialVersionUID = -6134697762789716619L;

    public void comboBoxValueChanged() {
        Object newValue = getComboBoxSelection();
        if (RoleConstraint.isAbstract((String) newValue)) {
            makeConcreteAbstract();
        } else {
            makeAbstractConcrete();
        }
    }

    private void makeAbstractConcrete() {
        valueChanged();
    }

    private void makeConcreteAbstract() {
        Cls cls = (Cls) getInstance();
        int instanceCount = (cls == null) ? 0 : cls.getDirectInstanceCount();
        if (instanceCount > 0) {
            String s = (instanceCount == 1) ? "" : "s";
            String text = "The class " + cls.getBrowserText();
            text += " has " + instanceCount + " direct instance" + s + ".\n";
            text += "Changing the role to \"abstract\" will cause these instances to be deleted.\n\n";
            text += "Do you really want to make this change?";
            int response = ModalDialog.showMessageDialog(RoleWidget.this, text, ModalDialog.MODE_YES_NO);
            if (response == ModalDialog.OPTION_YES) {
                Iterator<Instance> i = new ArrayList<Instance>(cls.getDirectInstances()).iterator();
                while (i.hasNext()) {
                    Instance instance = i.next();
                    instance.delete();
                }
                valueChanged();
            } else {
                setComboBoxValue(RoleConstraint.CONCRETE.toString());
            }
        } else {
            valueChanged();
        }
    }

    public void initialize() {
        setDisplayNullEntry(false);
        super.initialize();
        setRenderer(new RoleRenderer());
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.ROLE);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Role", ResourceKey.ROLE_SLOT_WIDGET_LABEL);
    }
    
}
