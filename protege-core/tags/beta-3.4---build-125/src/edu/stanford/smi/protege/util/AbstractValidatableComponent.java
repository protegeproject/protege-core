package edu.stanford.smi.protege.util;

import java.awt.*;

import javax.swing.*;

/**
 * Base class to match up the {@link Validatable} interface to a swing JComponent
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractValidatableComponent extends JComponent implements Validatable {

    public Component getComponent() {
        return this;
    }
}
