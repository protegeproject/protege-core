package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * Interface for the component that appears below the combo box in the value-type widget on the slot form.  This component
 * needs a number of different implementations, depending on the current value of the value-type.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ValuesComponent extends Selectable {

    void dispose();

    JComponent getComponent();

    Collection getValues();

    void setEditable(boolean b);

    void setValues(Collection values);
}
