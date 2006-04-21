package edu.stanford.smi.protege.widget;

import javax.swing.*;

/**
 * A base class for slot widget for acquiring integers or floating point numbers.
 *
 * @author Ray Fergerson
 */
public abstract class NumberFieldWidget extends TextFieldWidget {

    public void initialize() {
        super.initialize();
        getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
        setPreferredColumns(1);
        setPreferredRows(1);
    }
}
