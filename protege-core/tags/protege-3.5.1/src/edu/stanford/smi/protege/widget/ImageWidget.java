package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.net.*;

import javax.swing.*;
import javax.swing.text.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ImageWidget extends TextComponentWidget {
    private static final long serialVersionUID = -6002846064117335408L;
    private JLabel imageDisplay;

    public void initialize() {
        super.initialize(true, 2, 2);
    }

    protected JTextComponent createTextComponent() {
        return ComponentFactory.createTextField();
    }

    protected JComponent createCenterComponent(JTextComponent textComponent) {
        imageDisplay = ComponentFactory.createLabel();
        imageDisplay.setVerticalAlignment(SwingConstants.TOP);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(textComponent, BorderLayout.NORTH);
        panel.add(new JScrollPane(imageDisplay), BorderLayout.CENTER);
        return panel;
    }

    protected void onCommit() {
        super.onCommit();
        updateImage();
    }

    private void updateImage() {
        String value = getText();
        Icon icon = getIcon(value);
        imageDisplay.setIcon(icon);
    }

    private Icon getIcon(String text) {
        URL url = URIUtilities.toURL(text, getProject().getProjectURI());
        return (url == null) ? null : new ImageIcon(url);
    }

    protected void onSetText(String text) {
        updateImage();
    }
}
