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
import java.util.logging.Logger;

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
    private static final long serialVersionUID = 33858734669640699L;

    private static Logger log = Log.getLogger(ProjectChooser.class);
  
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
        ArrayList<String> extensions = new ArrayList<String>();
        extensions.add(".pprj");

        Collection classNames = PluginUtilities.getAvailableCreateProjectFromFilePluginClassNames();
        Iterator iterator = classNames.iterator();

        if(iterator.hasNext()) {
            text = "Supported Files";
        }

        while (iterator.hasNext()) {
            Class pluginClass = (Class) iterator.next();
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

    @Override
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

    @Override
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
            private static final long serialVersionUID = 2317162047521094349L;

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
            private static final long serialVersionUID = -3338082593317941941L;

            public void actionPerformed(ActionEvent event) {
                attemptClose(result);
            }
        };
        JButton button = ComponentFactory.createButton(action);
        button.addKeyListener(new KeyAdapter() {
            @Override
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
            project = getProjectFromFile();
            ApplicationProperties.setLastFileDirectory(getCurrentDirectory());

        } else if (c == urlPanel) {
            URI uri = urlPanel.getURI();
            if (uri != null) {
	            ApplicationProperties.setLastLoadedURI(uri);
	            project = loadProject(uri);
	        }

        } else if (c == serverPanel) {
            RemoteServer server = serverPanel.getServer();
            RemoteSession session = serverPanel.getSession();
        	if (server != null || session != null) {
        		if (serverPanel.isAdminsterServerActivated()) {
        			RemoteProjectManager.getInstance().showServerAdminWindow(server, session);
        		} else { //show projects only if administer server is not activated
        			project = getRemoteProject(server, session);
        		}
        	}
        } else {
            Log.getLogger().warning("bad component: " + c);
        }
        return project;
    }

    
	private Project getProjectFromFile() {
		Project project = null;
		ArrayList errors = new ArrayList();
		
		File selectedFile = getSelectedFile();
		if (selectedFile == null) {
			return null;
		}
		
		String fileName = selectedFile.toString();
		int lastDotIndex = fileName.lastIndexOf('.');
		
		// If the file has a suffix, use it to find the suitable project file create plugins
		// and try to load the file with the plugins 
		if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
		    String suffix = fileName.substring(lastDotIndex + 1);
		    
		    Collection availablePlugins = PluginUtilities.getAvailableCreateProjectFromFilePluginClassNames();
		    
		    Iterator<Class> it = availablePlugins.iterator();
		    
		    while (it.hasNext() && project == null) {
		    	Class pluginClass = it.next();
		        project = useCreateProjectFromFilePlugin(pluginClass, suffix, fileName, errors);
		        
		        if (project != null) {
		            String projectFilePath = fileName.substring(0, lastDotIndex) + ".pprj";
		            project.setProjectFilePath(projectFilePath);
		            break;
		            //ProjectManager.getProjectManager().setCurrentProject(project, false);
		        }
		    }
		    
		    if (errors.size() > 0) {		    	
		    	ProjectManager.getProjectManager().displayErrors("File load warnings and errors", errors);
		    }
		    
		    if (project != null)
		    	return project;
		    
		    //if (availablePlugins.size() > 0 && project == null) 
		    //	Log.getLogger().warning("Could not load the file with the available CreateProjectFromFile plugins. Trying the default loader...");
		}
		
		// If it got here, it means that it could not load the file using the existing plugins		
		URI uri = getSelectedFile().toURI();
		project = loadProject(uri);
				
		return project;
	}

	
    private Project getRemoteProject(RemoteServer server, RemoteSession session) {
        return RemoteProjectManager.getInstance().getServerProject(this, server, session);
    }

    //this method should be unified with the same method in ProjectManager
    private Project loadProject(URI uri) {
        Project project = null;
        Collection errors = new ArrayList();
        long t1 = System.currentTimeMillis();
        WaitCursor waitCursor = new WaitCursor(this);
        try {
        	
            project = Project.loadProjectFromURI(uri, errors);
            
        } catch (Exception e) {         
            errors.add(new MessageError(e));
            Log.getLogger().log(Level.SEVERE, "Error loading project", e);
        }  finally {
            waitCursor.hide();
        }
        long t2 = System.currentTimeMillis();
        
        Log.getLogger().info("Project load time for " + uri + ": " + (t2 - t1) / 1000 + " sec");
        
        //TODO: reimplement this when exception handling is improved. Handle here invalid project files 
        if (project != null && project.getProjectInstance() == null) {
        	String errorMsg = "Unable to load file: " + uri
					+ "\nPossible reasons:\n- The file has an unsupported file format\n- The file is not well-formed\n- The project file is corrupt";
        	
        	Log.getLogger().severe(errorMsg);
        	
        	errors.add(new MessageError(null,errorMsg));        	
        }
        
        ProjectManager.getProjectManager().displayErrors("Load Project Errors", errors);
        return project;
    }

    
    /**    
     * @deprecated Use: useCreateProjectFromFilePlugin(Class pluginClass, String suffix, String fileName, Collection errors)
     */
    @Deprecated
	public Project useCreateProjectFromFilePlugin(Class pluginClass, String suffix, String fileName) {    	
    	return useCreateProjectFromFilePlugin(pluginClass, suffix, fileName, new ArrayList());    
    }
    
    
    public Project useCreateProjectFromFilePlugin(Class pluginClass, String suffix, String fileName, Collection errors) {
    	CreateProjectFromFilePlugin plugin = getCreateProjectFromFilePluginFromClass(pluginClass);
    	
    	if (plugin == null || !PluginUtilities.isSuitableCreateProjectFromFilePlugin(plugin, suffix))
    		return null;
    	
    	File file = new File(fileName);
    	
    	if (!file.exists()) {
    		errors.add(new MessageError(null, "File: " + fileName + "does not exist."));
    		return null;
    	}
    	
    	Project project = plugin.createProject(file, errors);   	
        
        return project;
    }

    
	private CreateProjectFromFilePlugin getCreateProjectFromFilePluginFromClass(Class pluginClass) {		
        if (pluginClass == null)
        	return null;
        
        Object plugin = null;
               
		try {
			plugin = pluginClass.newInstance();
			return (CreateProjectFromFilePlugin) plugin;
		} catch (InstantiationException e) {		
			return null;
		} catch (IllegalAccessException e) {		
			return null;
		} catch (Exception e) {
			return null;
		}
	}
		

}

class URLPanel extends JPanel implements Validatable {
    private static final long serialVersionUID = -3336694646619661586L;
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
