package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.nio.charset.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class EncodingsPanel extends JPanel {
    private static final long serialVersionUID = -8201624349440500301L;
    private JComboBox readEncodingComboBox;
    // private JComboBox writeEncodingComboBox;

    public EncodingsPanel() {
        super(new BorderLayout());
        add(createEncodingsPanel(), BorderLayout.CENTER);
        add(createLocalePanel(), BorderLayout.SOUTH);
    }

    private JComponent createEncodingsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Encodings"));
        panel.add(createCurrentEncoding());
        panel.add(createReadEncodingOverride());
        // panel.add(createWriteEncodingOverride());
        return panel;
    }

    private static JComponent createCurrentEncoding() {
        JTextField textField = ComponentFactory.createTextField();
        textField.setEnabled(false);
        textField.setText(SystemUtilities.getFileEncoding());
        return new LabeledComponent("Default", textField);
    }

    private JComponent createReadEncodingOverride() {
        readEncodingComboBox = ComponentFactory.createComboBox();
        readEncodingComboBox.setModel(getEncodingModel());
        readEncodingComboBox.setSelectedItem(getCurrentReadEncodingOverride());
        readEncodingComboBox.setRenderer(new CharsetRenderer());
        return new LabeledComponent("Read Override", readEncodingComboBox);
    }

    private static String getCurrentReadEncodingOverride() {
        String override = FileUtilities.getReadEncodingOverride();
        if (override != null) {
            Charset charset = Charset.forName(override);
            override = charset.name();
        }
        return override;
    }

    private static ComboBoxModel getEncodingModel() {
        Collection charsets = Charset.availableCharsets().keySet();
        DefaultComboBoxModel model = new DefaultComboBoxModel(charsets.toArray());
        model.insertElementAt(null, 0);
        return model;
    }

    private static JComponent createLocalePanel() {
        JTextField field = ComponentFactory.createTextField();
        field.setEnabled(false);
        String text = Locale.getDefault().getDisplayName(Locale.ENGLISH);
        field.setText(text);
        return new LabeledComponent("Locale", field);
    }

    public void commitChanges() {
        String override = (String) readEncodingComboBox.getSelectedItem();
        FileUtilities.setReadEncodingOverride(override);
    }

}

class CharsetRenderer extends DefaultRenderer {
    private static final long serialVersionUID = -1093425151360866124L;

    public void load(Object o) {
        if (o instanceof String) {
            String charsetName = (String) o;
            Charset charset = Charset.forName(charsetName);
            String text = charset.name();
            setMainText(text);
        } else {
            setMainText("<none>");
        }
    }

    public void loadNull() {
        setMainText("");
    }
}
