package edu.stanford.smi.protege.util;

import java.awt.*;

import javax.swing.*;

/**
 * A component that contains a JTabbedPane and that implements the {@link Validatable} interface.  The Validatable
 * methods are all delegated to each of the tabs in succession.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ValidatableTabComponent extends AbstractValidatableComponent {
    private static final long serialVersionUID = -630654437547399826L;
    private JTabbedPane _tabbedPane;

    public ValidatableTabComponent() {
        setLayout(new BorderLayout());
        _tabbedPane = ComponentFactory.createTabbedPane(true);
        add(_tabbedPane);
    }

    public void addTab(String label, Component c) {
        _tabbedPane.addTab(label, c);
    }

    public void addTab(String label, Component c, String tooltip) {
        _tabbedPane.addTab(label, null, c, tooltip);
    }

    public void addTab(String label, Icon icon, Component c, String tooltip) {
        _tabbedPane.addTab(label, icon, c, tooltip);
    }

    public Component getTab(String label) {
    	int indexOfTab = _tabbedPane.indexOfTab(label);
    	if (indexOfTab >= 0)
    		return _tabbedPane.getComponent(indexOfTab);
    	else return null;    	
    }

    public JTabbedPane getTabbedPane() {
        return _tabbedPane;
    }

    public void saveContents() {
        int nTabs = _tabbedPane.getTabCount();
        for (int i = 0; i < nTabs; ++i) {
            Component c = _tabbedPane.getComponent(i);
            if (c instanceof Validatable) {
                ((Validatable) c).saveContents();
            }
        }
    }

    public boolean validateContents() {
        boolean isValid = true;
        int nTabs = _tabbedPane.getTabCount();
        for (int i = 0; i < nTabs && isValid; ++i) {
            Component c = _tabbedPane.getComponent(i);
            if (c instanceof Validatable) {
                isValid = ((Validatable) c).validateContents();
            }
        }
        return isValid;
    }
}
