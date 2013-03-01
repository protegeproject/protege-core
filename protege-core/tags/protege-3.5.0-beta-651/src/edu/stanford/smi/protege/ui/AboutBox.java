package edu.stanford.smi.protege.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * Panel to display for the "About Box" menu item.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AboutBox extends JPanel {

    private static final long serialVersionUID = 644756887038597032L;

    public AboutBox(URL url, Dimension preferredSize) {
        setLayout(new BorderLayout());
        JEditorPane pane = ComponentFactory.createHTMLBrowser(url);
        substitute(pane);
        pane.setEditable(false);

        if (preferredSize != null) {
        	setPreferredSize(preferredSize);
        } else {
        	setPreferredSize(new Dimension(535, 550));
        }

        JScrollPane scrollPane = new JScrollPane(pane);
        add(scrollPane);
    }

    private static void substitute(JEditorPane pane) {
        String text = getText(pane);
        text = replace(text, "{0}", Text.getVersion());
        text = replace(text, "{1}", Text.getStatus());
        text = replace(text, "{2}", Text.getBuildInfo());
        text = replace(text, "{3}", Text.getCopyright());
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

    private static String replace(String text, String macro, String replaceString) {
        return StringUtilities.replace(text, macro, replaceString);
    }
}