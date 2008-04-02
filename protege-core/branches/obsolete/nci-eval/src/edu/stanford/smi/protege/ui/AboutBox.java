package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.net.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to display for the "About Box" menu item.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AboutBox extends JPanel {

    public AboutBox() {
        setLayout(new BorderLayout());
        URL url = Text.getAboutURL();
        JEditorPane pane = ComponentFactory.createHTMLBrowser(url);
        substitute(pane);
        
        add(new JScrollPane(pane));
        pane.setEditable(false);
        setPreferredSize(new Dimension(535, 550));
    }

    private void substitute(JEditorPane pane) {
        String text = getText(pane);
        text = replace(text, "{0}", Text.getVersion());
        text = replace(text, "{1}", Text.getBuildInfo());
        pane.setText(text);
    }

    // The pane loads asynchronously so we have to wait...
    private static String getText(JEditorPane pane) {
        String text = null;
        for (int i = 0; i < 100; ++i) {
            text = pane.getText();
            if (text.indexOf("Stanford") != -1 && text.indexOf("</html>") != -1) {
                break;
            }
            SystemUtilities.sleepMsec(100);
        }
        SystemUtilities.sleepMsec(100);
        return text;
    }

    private String replace(String text, String macro, String replaceString) {
        return StringUtilities.replace(text, macro, replaceString);
    }
}