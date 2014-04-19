package edu.stanford.smi.protege.widget;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * Slot widget that is displayed when another slot widget fails to load (either the constructor or the initialize method
 * throw an exception.  This is a fairly common occurance when slot widgets are being developed.  Placing a "dead" 
 * widget on the form instead causes problems whose source is unclear.  When this widget appears it is perfectly clear
 * that something has gone wrong and users know where to look.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class UglySlotWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = -1320896168960476357L;
    private String widgetClassName;

    public UglySlotWidget() {
        
    }
    
    public UglySlotWidget(String widgetClassName) {
        this.widgetClassName = widgetClassName;
    }
    
    public String getLabel() {
        return "Ugly Slot Widget (tm)";
    }

    public void initialize() {
        JComponent c1 = ComponentFactory.createLabel("<html><b>The Ugly Widget\u2122</b></html>", SwingConstants.CENTER);
        String text = "<html>Slot: " + getSlot().getName();
        if (widgetClassName != null) {
            text += "<br>Class: " + widgetClassName;
        }
        text += "</html>";
        JComponent c2 = ComponentFactory.createLabel(text, SwingConstants.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(Color.red);
        panel.setForeground(Color.black);
        panel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.green));
        panel.add(c1, BorderLayout.CENTER);
        panel.add(c2, BorderLayout.SOUTH);
        add(panel);
        setPreferredColumns(2);
        setPreferredRows(2);
    }
}
