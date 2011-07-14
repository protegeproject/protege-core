package edu.stanford.smi.protege.widget;

import java.awt.*;

import edu.stanford.smi.protege.util.*;

/**
 * Standard configuration panel for buttons on SlotWidgets.  Buttons can be configured to be displayed or not and to
 * have some descriptive tool-tip text.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ButtonConfigurationPanel extends AbstractValidatableComponent {
    private static final long serialVersionUID = 3763344736567335495L;
    private PropertyList _propertyList;

    public ButtonConfigurationPanel(PropertyList propertyList) {
        _propertyList = propertyList;
    }

    public void addButton(String name, String defaultDescription, boolean defaultState) {
        add(new ButtonControlPanel(name, defaultDescription, defaultState, _propertyList));
        setLayout(new GridLayout(getComponentCount(), 1, 10, 10));
    }

    public static String getDescriptionPropertyName(String buttonName) {
        return "ButtonDescription-" + buttonName;
    }

    public static String getDisplayPropertyName(String buttonName) {
        return "ButtonDisplayed-" + buttonName;
    }

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, 300);
        return d;
    }

    public void saveContents() {
        int nComponents = getComponentCount();
        for (int i = 0; i < nComponents; ++i) {
            ButtonControlPanel panel = (ButtonControlPanel) getComponent(i);
            panel.saveContents();
        }
    }

    public boolean validateContents() {
        return true;
    }
}
