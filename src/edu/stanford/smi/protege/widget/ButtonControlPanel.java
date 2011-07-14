package edu.stanford.smi.protege.widget;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * A panel to display the configuration information about a button.  This consists of "existence" and its tooltip text.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class ButtonControlPanel extends JComponent {

    private static final long serialVersionUID = 5881175135211139875L;
    private String _name;
    private PropertyList _propertyList;
    private JCheckBox _enabledComponent;
    private JTextField _textComponent;

    public ButtonControlPanel(
        String name,
        String defaultDescription,
        boolean defaultState,
        PropertyList propertyList) {
        _name = name;
        _propertyList = propertyList;

        _enabledComponent = ComponentFactory.createCheckBox();
        Boolean value = _propertyList.getBoolean(displayStringProperty());
        _enabledComponent.setSelected(value == null ? defaultState : value.booleanValue());

        _enabledComponent.setText("Show " + name + " Button");
        _textComponent = ComponentFactory.createTextField();
        String text = _propertyList.getString(descriptionProperty());
        if (text == null) {
            text = defaultDescription;
        }
        _textComponent.setText(text);

        setLayout(new BorderLayout());
        add(_enabledComponent, BorderLayout.NORTH);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(_textComponent, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
    }

    private String descriptionProperty() {
        return ButtonConfigurationPanel.getDescriptionPropertyName(_name);
    }

    private String displayStringProperty() {
        return ButtonConfigurationPanel.getDisplayPropertyName(_name);
    }

    public void saveContents() {
        boolean isEnabled = _enabledComponent.isSelected();
        _propertyList.setBoolean(displayStringProperty(), new Boolean(isEnabled));
        String text = _textComponent.getText();
        _propertyList.setString(descriptionProperty(), text);
    }
}
