package edu.stanford.smi.protege.widget;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel that allows a user to configure the KA Tool tab.  This basically just involves setting the top-level instance.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class KAToolTabConfigurationPanel extends AbstractValidatableComponent {
    private static final long serialVersionUID = 100685654822212554L;
    private InstanceField _topLevelInstanceField;
    private JTextField _labelField;
    private KAToolTab _tab;

    public KAToolTabConfigurationPanel(KAToolTab tab) {
        this._tab = tab;

        _topLevelInstanceField = new InstanceField("Top-Level Instance", tab.getKnowledgeBase().getRootClses());
        _topLevelInstanceField.setInstance(tab.getTopLevelInstance());
        _topLevelInstanceField.createSelectInstanceAction();
        _topLevelInstanceField.createRemoveInstanceAction();

        _labelField = ComponentFactory.createTextField();
        _labelField.setText(tab.getLabel());

        setLayout(new GridLayout(2, 1));
        add(new LabeledComponent("Label", _labelField));
        add(_topLevelInstanceField);
    }

    /**
     * saveContents method comment.
     */
    public void saveContents() {
        _tab.setLabel(_labelField.getText());
        _tab.setTopLevelInstance(_topLevelInstanceField.getInstance());
    }

    /**
     * saveContents method comment.
     */
    public boolean validateContents() {
        // do nothing
        return true;
    }
}
