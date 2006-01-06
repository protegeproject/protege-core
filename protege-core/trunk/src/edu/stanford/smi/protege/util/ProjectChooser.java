package edu.stanford.smi.protege.util;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.CreateProjectFromFilePlugin;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.ServerPanel;
import edu.stanford.smi.protege.ui.ProjectManager;

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
    private JPanel pane;
    private CardLayout layout = new CardLayout();
    public static final String FILE_CARD = "File";
    public static final String URL_CARD = "URL";
    public static final String SERVER_CARD = "Server";
    private ButtonGroup buttonGroup = new ButtonGroup();
    private static final String CARD_NAME_PROPERTY = "project_chooser.selected_card";
    private JDialog dialog;
    private URLPanel urlPanel;
    private ServerPanel serverPanel;
    private int returnValue = CANCEL_OPTION;

    public ProjectChooser() {
        setControlButtonsAreShown(false);
        setDialogTitle("Open Project");
        String text = Text.getProgramName() + " Project";
        ArrayList extensions = new ArrayList();
        extensions.add(".pprj");
        Iterator it = PluginUtilities.getAvailableCreateProjectFromFilePluginClassNames().iterator();
        if(it.hasNext()) {
            text = "Supported Files";
        }
        while (it.hasNext()) {
            Class pluginClass = (Class) it.next();
            try {
                CreateProjectFromFilePlugin plugin = (CreateProjectFromFilePlugin) pluginClass.newInstance();
                String[] es = plugin.getSuffixes();
                for (int i = 0; i < es.length; i++) {
                    String e = es[i];
                    extensions.add(e);
                }
            }
            catch(Exception ex) {
                Log.getLogger().log(Level.INFO, "Exception caught", ex);
            }
        }
        setFileFilter(new ExtensionFilter(extensions.iterator(), text));
        setCurrentDirectory(ApplicationProperties.getLastFileDirectory());
        setName(FILE_CARD);
    }

    protected JDialog createDialog(Component parent) {
        dialog = super.createDialog(parent);
        Container contentPane = dialog.getContentPane();
        contentPane.remove(this);
        contentPane.setLayout(new BorderLayout());
        pane = new JPanel(layout);
        pane.add(FILE_CARD, this);
        pane.add(URL_CARD, (urlPanel = new URLPanel()));
        pane.add(SERVER_CARD, (serverPanel = new ServerPanel()));
        urlPanel.setBorder(getBorder());
        serverPanel.setBorder(getBorder());
        serverPanel.setName(SERVER_CARD);

        layout.show(pane, ApplicationProperties.getString(CARD_NAME_PROPERTY, FILE_CARD));
        contentPane.add(createSelectionButtonPane(), BorderLayout.WEST);
        contentPane.add(pane, BorderLayout.CENTER);
        contentPane.add(createOKCancelButtonPane(), BorderLayout.SOUTH);
        return dialog;
    }

    private Component getActiveCard() {
        Component activeCard = null;
        for (int i = 0; i < pane.getComponentCount(); ++i) {
            Component c = pane.getComponent(i);
            if (c.isVisible()) {
                activeCard = c;
                break;
            }
        }
        return activeCard;
    }

    private void attemptClose(int result) {
        if (canClose(result)) {
            returnValue = result;
            dialog.setVisible(false);
        }
    }

    public boolean canClose(int result) {
        boolean canClose = true;
        if (result == APPROVE_OPTION) {
            Component c = getActiveCard();
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
        Component c = getActiveCard();
        ApplicationProperties.setString(CARD_NAME_PROPERTY, c.getName());
        return returnValue;
    }

    private JPanel createSelectionButtonPane() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(createSelectionButton(FILE_CARD));
        panel.add(createSelectionButton(URL_CARD));
        panel.add(createSelectionButton(SERVER_CARD));
        JPanel externalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        externalPanel.add(panel);
        return externalPanel;
    }

    private AbstractButton createSelectionButton(final String name) {
        Action action = new AbstractAction(name) {
            public void actionPerformed(ActionEvent event) {
                layout.show(pane, name);
            }
        };
        JToggleButton button = new JToggleButton(action);
        int size = 75;
        button.setPreferredSize(new Dimension(size, size));
        buttonGroup.add(button);
        if (getActiveCard().getName().equals(name)) {
            button.setSelected(true);
        }
        return button;
    }

    private JComponent createOKCancelButtonPane() {
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
        Component c = getActiveCard();
        if (c == this) {
            File selectedFile = getSelectedFile();
            String fileName = selectedFile.toString();
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
                String suffix = fileName.substring(lastDotIndex + 1);
                Iterator<Class> it = PluginUtilities.getAvailableCreateProjectFromFilePluginClassNames().iterator();
                while (it.hasNext() && project == null) {
                    Class pluginClass = (Class) it.next();
                    project = useCreateProjectFromFilePlugin(pluginClass, suffix, fileName);
                    if (project != null) {
                        String projectFilePath = fileName.substring(0, lastDotIndex) + ".pprj";
                        project.setProjectFilePath(projectFilePath);
                        //ProjectManager.getProjectManager().setCurrentProject(project, false);
                    }
                }
            }
            if (project == null) {  
                URI uri = getSelectedFile().toURI();
                project = loadProject(uri);
            }
            ApplicationProperties.setLastFileDirectory(getCurrentDirectory());

        } else if (c == urlPanel) {
            URI uri = urlPanel.getURI();
            ApplicationProperties.setLastLoadedURI(uri);
            project = loadProject(uri);

        } else if (c == serverPanel) {
            project = getRemoteProject();

        } else {
            Log.getLogger().warning("bad component: " + c);
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
    
    public Project useCreateProjectFromFilePlugin(Class pluginClass, String suffix, String fileName) {
        if (pluginClass != null) {
            try {
                Object plugin = pluginClass.newInstance();
                if (plugin instanceof CreateProjectFromFilePlugin) {
                    CreateProjectFromFilePlugin p = (CreateProjectFromFilePlugin) plugin;
                    if (PluginUtilities.isSuitableCreateProjectFromFilePlugin(p, suffix)) {
                        File file = new File(fileName);
                        Collection errors = new ArrayList();
                        Project project = p.createProject(file, errors);
                        if (!errors.isEmpty()) {
                            Iterator it = errors.iterator();
                            while (it.hasNext()) {
                                Object error = it.next();
                                System.err.println("Error with file " + fileName + ": " + error);
                            }
                        }
                        return project;
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Warning: Failed handle argument with " + pluginClass + ": " + ex);
            }
        }
        return null;
    }
    
}

class URLPanel extends JPanel implements Validatable {
    private JTextField field;

    URLPanel() {
        field = ComponentFactory.createTextField();
        setLayout(new BorderLayout());
        add(new LabeledComponent("URL", field), BorderLayout.NORTH);
        setName(ProjectChooser.URL_CARD);
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
            // do nothing
        }
        return uri;
    }
}