package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.server.*;
import edu.stanford.smi.protege.ui.*;

/**
 * This is a really horrible hack of a class. The problem is that the JFileChooser is a 
 * very strange combination of a panel and a dialog. It doesn't appear to be possible 
 * to separate the two cleanly. Thus in order to reuse the
 * JFileChooser UI in another panel I have to subclass it, even though my subclass is 
 * not a strickly correct one. 
 * 
 * All of this depends upon undocumented internal behavior of JFileChooser in order to 
 * work. It appears to me that the only alternative is to write my own file chooser 
 * dialog. I have no intention of doing this. Thus we are left with this gruesome class.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectChooser extends JFileChooser {
    private JTabbedPane pane;
    private static final int FILE_INDEX = 0;
    private static final int URL_INDEX = 1;
    private static final int REMOTE_INDEX = 2;
    private static final String TAB_INDEX_PROPERTY = "project_chooser.selected_tab";
    private JDialog dialog;
    private URLPanel urlPanel;
    private ServerPanel serverPanel;
    private int returnValue = CANCEL_OPTION;

    public ProjectChooser() {
        setControlButtonsAreShown(false);
        setDialogTitle("Open Project");
        String text = Text.getProgramName() + " Project";
        setFileFilter(new ExtensionFilter(".pprj", text));
        setCurrentDirectory(ApplicationProperties.getLastFileDirectory());
    }

    protected JDialog createDialog(Component parent) {
        dialog = super.createDialog(parent);
        Container contentPane = dialog.getContentPane();
        contentPane.remove(this);
        contentPane.setLayout(new BorderLayout());
        pane = ComponentFactory.createTabbedPane(false);
        pane.addTab("File", this);
        pane.addTab("URL", (urlPanel = new URLPanel()));
        pane.addTab("Server", (serverPanel = new ServerPanel()));

        pane.setSelectedIndex(ApplicationProperties.getIntegerProperty(TAB_INDEX_PROPERTY, 0));
        contentPane.add(pane, BorderLayout.CENTER);
        contentPane.add(createButtonPane(), BorderLayout.SOUTH);
        return dialog;
    }

    private void attemptClose(int result) {
        if (canClose(result)) {
            returnValue = result;
            dialog.hide();
        }
    }

    public boolean canClose(int result) {
        boolean canClose = true;
        if (result == APPROVE_OPTION) {
            Component c = pane.getSelectedComponent();
            if (c instanceof Validatable) {
                Validatable v = (Validatable) c;
                canClose = v.validateContents();
            }
        }
        return canClose;
    }

    public int showOpenDialog(Component parent) {
        int dialogReturnValue = super.showOpenDialog(parent);
        if (dialogReturnValue == APPROVE_OPTION) {
            returnValue = APPROVE_OPTION;
        }
        int index = pane.getSelectedIndex();
        ApplicationProperties.setInt(TAB_INDEX_PROPERTY, index);
        return returnValue;
    }

    private JComponent createButtonPane() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 10, 10));
        panel.add(createButton(APPROVE_OPTION, ResourceKey.OK_BUTTON_LABEL));
        panel.add(createButton(CANCEL_OPTION, ResourceKey.CANCEL_BUTTON_LABEL));
        JPanel externalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        externalPanel.add(panel);
        return externalPanel;
    }

    private JButton createButton(final int result, ResourceKey key) {
        Action action = new StandardAction(key) {
            public void actionPerformed(ActionEvent event) {
                attemptClose(result);
            }
        };
        JButton button = ComponentFactory.createButton(action);
        button.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        attemptClose(result);
                        break;
                    case KeyEvent.VK_ESCAPE:
                        attemptClose(CANCEL_OPTION);
                        break;
                    default:
                        // do nothing
                        break;
                }
            }
        });
        return button;
    }

    public Project getProject() {
        Project project = null;
        switch (pane.getSelectedIndex()) {
            case FILE_INDEX: {
                URI uri = getSelectedFile().toURI();
                project = loadProject(uri);
                ApplicationProperties.setLastFileDirectory(getCurrentDirectory());
                break;
            }
            case URL_INDEX: {
                URI uri = urlPanel.getURI();
                ApplicationProperties.setLastLoadedURI(uri);
                project = loadProject(uri);
                break;
            }
            case REMOTE_INDEX: {
                project = getRemoteProject();
                break;
            }
            default:
                Log.getLogger().warning("bad index: " + pane.getSelectedIndex());
                break;
        }
        return project;
    }

    private Project getRemoteProject() {
        RemoteServer server = serverPanel.getServer();
        RemoteSession session = serverPanel.getSession();
        return RemoteProjectManager.getInstance().getServerProject(this, server, session);
    }

    private Project loadProject(URI uri) {
        Project project;
        Collection errors = new ArrayList();
        long t1 = System.currentTimeMillis();
        WaitCursor waitCursor = new WaitCursor(this);
        try {
            project = Project.loadProjectFromURI(uri, errors);
        } finally {
            waitCursor.hide();
        }
        long t2 = System.currentTimeMillis();
        Log.getLogger().info("Project load time for " + uri + ": " + (t2 - t1) / 1000 + " sec");
        ProjectManager.getProjectManager().displayErrors("Load Project Errors", errors);
        return project;
    }
}

class URLPanel extends JPanel implements Validatable {
    private JTextField field;

    public URLPanel() {
        field = ComponentFactory.createTextField();
        setLayout(new BorderLayout());
        add(new LabeledComponent("URL", field), BorderLayout.NORTH);
    }

    public boolean validateContents() {
        boolean isValid = false;
        try {
            String text = field.getText();
            URL url = new URL(text);
            InputStream inputStream = url.openStream();
            inputStream.close();
            if (text.endsWith(".pprj")) {
                isValid = true;
            } else {
                ModalDialog.showMessageDialog(this, "Can only open Protege project (.pprj) files");
            }
        } catch (Exception e) {
            ModalDialog.showMessageDialog(this, "Unreachable URL");
        }
        return isValid;
    }

    public void saveContents() {
        // do nothing
    }

    public URI getURI() {
        String text = field.getText();
        URI uri = null;
        try {
            uri = new URI(text);
        } catch (URISyntaxException e) {

        }
        return uri;
    }
}