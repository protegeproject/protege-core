package edu.stanford.smi.protege.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protege.util.ViewAction;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class URLWidget extends TextComponentWidget {
  
    private static final long serialVersionUID = -6468403747924085772L;
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
            private static final long serialVersionUID = -3490306473291374592L;

            public void actionPerformed(ActionEvent events) {
                updateURLDisplay();
            }
        };
    }

    protected Action createViewAction() {
        return new ViewAction(ResourceKey.URL_VIEW_IN_BROWSER, null) {
            private static final long serialVersionUID = 4535338964946498034L;

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
            setPage(url);
        }
    }

    private void setPage(final URL url) {
        if (isImage(url)) {
            urlDisplay.setContentType("text/html");
            urlDisplay.setText("<HTML><BODY><IMG src=\"" + url + "\"></BODY></HTML>");
        } else {
            urlDisplay.setContentType("text/html");
            urlDisplay.setText("<HTML><BODY><H3>Loading...</H3></HTML>");
            asyncLoadURL(url);
        }
    }

    private void asyncLoadURL(final URL url) {
        Thread thread = new Thread() {
            public void run() {
              try {
                getText(url);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                          urlDisplay.setPage(url);
                        } catch (IOException e) {
                          urlDisplay.setText("<HTML><BODY><H3>Cannot Find This Webpage</H3></HTML>");
                        } catch (Throwable t) {
                          Log.getLogger().log(Level.INFO, "Exception caught", t);
                        }
                    }
                });
              } catch (Throwable t) {
                Log.getLogger().log(Level.INFO, "Exception caught", t);
              }
            }
        };
        thread.start();
    }

    private String getText(URL url) {
        StringBuffer buffer = new StringBuffer();
        try {
            char[] cbuf = new char[100000];
            InputStream is = url.openStream();
            Reader reader = new InputStreamReader(is);
            int nchars;
            while ((nchars = reader.read(cbuf)) != -1) {
                buffer.append(cbuf, 0, nchars);
            }
        } catch (IOException e) {
            buffer.append("*** Load failed ***");
        }
        return buffer.toString();
    }

    private static boolean isImage(URL url) {
        String u = url.toString().toLowerCase();
        return u.endsWith(".gif") || u.endsWith(".png") || u.endsWith(".jpeg") || u.endsWith(".jpg");
    }

    // Amazing that there isn't a method to clear the current URL... Setting it
    // to null just throws an exception.
    // I figured this out only by looking at the JEditorPane source code.
    private void clearUrlDisplay() {
        urlDisplay.setText(null);
        urlDisplay.getDocument().putProperty(Document.StreamDescriptionProperty, null);
    }

    protected void onSetText(String text) {
        updateURLDisplay();
    }
}

// Copied straight out of the JEditerPane documentation

class Hyperactive implements HyperlinkListener {
    private static Logger log = Log.getLogger(URLWidget.class);

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
                  log.log(Level.SEVERE, "Exception caught", t);
                }
            }
        }
    }
}