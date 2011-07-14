package edu.stanford.smi.protege.util;

import javax.swing.*;

/**
 * An adapter that matches the {@link Disposable} interface up to a JComponent.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class DisposableComponent extends JComponent implements Disposable {

    private static final long serialVersionUID = 6987239802937718687L;
}
