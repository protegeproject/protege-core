package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class URLWidget extends TextComponentWidget {
    private JEditorPane urlDisplay;

    public void initialize() {
        initialize(true, 2, 2);
    }

    protected JTextComponent createTextComponent() {
        return ComponentFactory.createTextField();
    }

    protected void onCommit() {
        super.onCommit();
        updateURLDisplay();
    }

    protected JComponent createCenterComponent(JTextComponent textComponent) {
        urlDisplay = ComponentFactory.createEditorPane();
        urlDisplay.setEditable(false);
        urlDisplay.addHyperlinkListener(new Hyperactive());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(textComponent, BorderLayout.NORTH);
        panel.add(new JScrollPane(urlDisplay), BorderLayout.CENTER);
        return panel;
    }

    protected Action createHomeAction() {
        return new AbstractAction("Home", Icons.getHomeIcon()) {
            public void actionPerformed(ActionEvent events) {
                updateURLDisplay();
            }
        };
    }

    protected Action createViewAction() {
        return new ViewAction(ResourceKey.URL_VIEW_IN_BROWSER, null) {
            public void onView() {
                URL url = getCurrentURL();
                if (url != null) {
                    SystemUtilities.showHTML(url.toString());
                }
            }
        };
    }

    protected Collection createActions() {
        Collection actions = new ArrayList();
        actions.add(createViewAction());
        actions.add(createHomeAction());
        return actions;
    }

    private URL getCurrentURL() {
        String value = getText();
        return URIUtilities.toURL(value, getProject().getProjectURI());
    }

    private void updateURLDisplay() {
        URL url = getCurrentURL();
        if (url == null) {
            clearUrlDisplay();
        } else {
            if (url.equals(urlDisplay.getPage())) {
                clearUrlDisplay();
            }
            // try {
                setPage(url);
            // } catch (IOException e) {
            //     clearUrlDisplay();
            // }
        }
    }

    private void setPage(final URL url) {
        if (isImage(url)) {
            urlDisplay.setContentType("text/html");
            urlDisplay.setText("<HTML><BODY><IMG src=\"" + url + "\"></BODY></HTML>");
        } else {
            // try not to hang forever on bad pages
            // This really isn't safe since swing is not threadsafe but I don't know
            // how else to do it.
            Thread thread = new Thread() {
                public void run() {
                    try {
                        urlDisplay.setPage(url);
                    } catch (IOException e) {
                        clearUrlDisplay();
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static boolean isImage(URL url) {
        String u = url.toString().toLowerCase();
        return u.endsWith(".gif") || u.endsWith(".png") || u.endsWith(".jpeg") || u.endsWith(".jpg");
    }

    // Amazing that there isn't a method to clear the current URL...  Setting it to null just throws an exception.
    // I figured this out only by looking at the JEditorPane source code.
    private void clearUrlDisplay() {
        urlDisplay.setText(null);
        urlDisplay.getDocument().putProperty(Document.StreamDescriptionProperty, null);
    }

    protected void onSetText(String text) {
        updateURLDisplay();
    }

}

// Straight out of the JEditerPane documentation
class Hyperactive implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) e.getSource();
            if (e instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                HTMLDocument doc = (HTMLDocument) pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            } else {
                try {
                    pane.setPage(e.getURL());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
