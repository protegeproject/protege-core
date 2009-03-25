package edu.stanford.smi.protege;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import edu.stanford.smi.protege.exception.ModificationException;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.CreateProjectFromFilePlugin;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.SplashScreen;
import edu.stanford.smi.protege.ui.WelcomeDialog;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.URIUtilities;

/**
 * Main class for the Protege application (as opposed to an applet).
 *
 * @author Ray Fergerson
 */
public class Application {
    private static transient Logger log = Log.getLogger(Application.class);

    private static JFrame _mainFrame;
    private static SplashScreen _splashScreen;
    private static WelcomeDialog _welcome;
    private static final String projectFileExtension = ".pprj";
    private static final String propertyOptionsPrefix = "-prop";
    private static final String serverPropertyOptionsPrefix = "-server";
    private static final String serverUserProperty = serverPropertyOptionsPrefix + ".user";
    private static final String serverPasswordProperty = serverPropertyOptionsPrefix + ".password";
    private static final String serverHostProperty = serverPropertyOptionsPrefix + ".host";
    private static final String serverprojectProperty = serverPropertyOptionsPrefix + ".project";
    
    private static void initialize() {
        try {
            SystemUtilities.initGraphics();
        } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "failed to initialize", e);
        }
    }

    public static Component getMainWindow() {
        Component result;
        if (_splashScreen != null) {
            result = _splashScreen;
        } else {
            result = _mainFrame;
        }
        return result;
    }

    private static URI getProjectURI(String[] args) {
        URI uri = null;
        if (args.length > 0) {
            String projectString = args[0];
            if (projectString.startsWith(propertyOptionsPrefix) ||
            	projectString.startsWith(serverPropertyOptionsPrefix)) {
            	//this is a property, ignore it
            	return null;
            }
            
            if (!projectString.endsWith(projectFileExtension)) {
                projectString += projectFileExtension;
            }
            uri = URIUtilities.createURI(projectString);            
        }

        return uri;
    }

    private static void init(String[] args) {
        parseOptions(args);

        // Construct the application's main frame.
        _mainFrame = ComponentFactory.createMainFrame();
        _mainFrame.addWindowListener(new WindowAdapter() {
            @Override
			public void windowClosing(WindowEvent event) {
                ProjectManager.getProjectManager().exitApplicationRequest();
            }
        });

        restoreMainframeRectangle();
        ProjectManager.getProjectManager().setRootPane(_mainFrame.getRootPane());

        // Check for matching files among arguments
        Project project = null;
        if (args.length > 0) {
            String possibleFilename = args[0];
            boolean isFile = false;            
            try {
            	isFile = new File(possibleFilename).exists();
			} catch (Exception e) {
				//Fine to have empty catch block here				
			}            
			if (isFile) {
				int lastDotIndex = possibleFilename.lastIndexOf('.');
				if (lastDotIndex > 0 && lastDotIndex < possibleFilename.length() - 1) {
					if (!possibleFilename.endsWith(projectFileExtension)){
						String suffix = possibleFilename.substring(lastDotIndex + 1);
						Iterator it = PluginUtilities.getAvailableCreateProjectFromFilePluginClassNames().iterator();
						while (it.hasNext() && project == null) {
							Class pluginClass = (Class) it.next();
							project = useCreateProjectFromFilePlugin(pluginClass, suffix, possibleFilename);
							if (project != null) {
								String projectFilePath = possibleFilename.substring(0, lastDotIndex) + projectFileExtension;
								project.setProjectFilePath(projectFilePath);
								ProjectManager.getProjectManager().setCurrentProject(project, false);
							}
						}
					}
				}
			} else {
				//try to load it as a remote server project (if args are right)
				boolean continueLoading = false;
				try {
					project = getRemoteProject(args);					
				} catch (IllegalArgumentException e) { //insufficient arguments passed - continue processsing
					continueLoading = true;
				}				
				if (project == null && !continueLoading) {
					return;
				}
			}
        }
        
        if (project != null) {
            showMainFrame();
        }
        else {
            // Find out if a filename was passed in on the command-line.
            URI projectURI = getProjectURI(args);

            if (projectURI != null) {
                // Load the project and show the main frame.
            	try {
            		ProjectManager.getProjectManager().loadProject(projectURI);
            	} finally {
            		showMainFrame();
            	}
            }
            else {
                showMainFrame();
                if (!SystemUtilities.isApplet()) {
                	String startInServerPanel = ApplicationProperties.getString(ApplicationProperties.WELCOME_DIALOG_START_IN_SERVER_PANEL, "false");
                	if (startInServerPanel.equalsIgnoreCase("true")) {
                		ProjectManager.getProjectManager().openRemoteProjectRequest();
                	} else if (ApplicationProperties.getWelcomeDialogShow()) {
                		// Load the main frame and show the welcome dialog.                	                	
                		_welcome = new WelcomeDialog(_mainFrame, "Welcome to " + Text.getProgramName(), true);
                		_welcome.setLocationRelativeTo(_mainFrame);
                		_welcome.setVisible(true);                	
                	}
                } else {//is applet
                	ProjectManager.getProjectManager().openRemoteProjectRequest();
                }
            }
        }
    }

    private static Project getRemoteProject(String[] args) {
    	Project project = null;
    	String host = null;
    	String user = null;
    	String pass = null;
    	String prjName = null;
    	for (int i = 0; i < args.length; i++) {
    		String option = args[i];
			try {				
				if (option.startsWith(serverHostProperty)) {
					host = extractValue(option);
				} else if (option.startsWith(serverUserProperty)) {
					user = extractValue(option);
				} else if (option.startsWith(serverPasswordProperty)) {
					pass = extractValue(option);
				} else if (option.startsWith(serverprojectProperty)) {
					prjName = extractValue(option);
				}
			} catch (Throwable t) {
				Log.getLogger().log(Level.WARNING, "Error at parsing argument: " + option +
						". This argument will be ignored.", t);
			}
		}    	
    	if (host == null || user == null || pass == null || prjName == null) {
    		throw new IllegalArgumentException("Insufficient arguments");
    	}
    	project = ProjectManager.getProjectManager().openRemoteProjectRequest(host, user, pass, prjName);
    	if (project == null) {
			showMainFrame();
			MessageError messageError = new MessageError(
					"Loading remote project " + prjName + " from server " + host + " failed.\n" +
					"Please check your username and password and try again.");
			ProjectManager.getProjectManager().displayErrors("Could not load remote project " + prjName,
					CollectionUtilities.createCollection(messageError));
    	}
		return project;
	}
    
    
    private static String extractValue(String option) {
		int index = option.indexOf("=");
		if (index != -1) {			
			String value = option.substring(index+1);
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length()-1);
			}
			return value;
		}
		return null;
    }

	public static Project useCreateProjectFromFilePlugin(Class pluginClass, String suffix, String arg) {
        if (pluginClass != null) {
            try {
                Object plugin = pluginClass.newInstance();
                if (plugin instanceof CreateProjectFromFilePlugin) {
                    CreateProjectFromFilePlugin p = (CreateProjectFromFilePlugin) plugin;
                    if (PluginUtilities.isSuitableCreateProjectFromFilePlugin(p, suffix)) {
                        File file = new File(arg);
                        Collection errors = new ArrayList();
                        Project project = p.createProject(file, errors);
                        Log.handleErrors(log, Level.WARNING, errors);
                        return project;
                    }
                }
            }
            catch (Exception ex) {
              log.log(Level.SEVERE,
                      "Warning: Failed handle argument with " + pluginClass,
                      ex);
            }
        }
        return null;
    }


    public static WelcomeDialog getWelcomeDialog() {
        return _welcome;
    }

    private static void restoreMainframeRectangle() {
        ApplicationProperties.restoreMainFrameProperties(_mainFrame);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle r = gc.getBounds();

        // make sure that the size is smaller than the current screen size
        Dimension frameSize = _mainFrame.getSize();
        frameSize.width = Math.min(frameSize.width, r.width);
        frameSize.height = Math.min(frameSize.height, r.height);
        _mainFrame.setSize(frameSize);

        // make sure that the position appears on the screen -- some "virtual" windowing systems mess this up.
        Point location = _mainFrame.getLocation();
        location.x = Math.max(r.x, location.x);
        location.y = Math.max(r.y, location.y);
        _mainFrame.setLocation(location);
    }

    public static void main(final String[] args) {
        // This bit of sleight of hand causes uncaught exceptions to get logged.
        // There is a better way to do this in JDK 1.5.
        ThreadGroup group = new ThreadGroup(Thread.currentThread().getThreadGroup(), "Safe Thread Group") {
            @Override
			public void uncaughtException(Thread thread, Throwable throwable) {
                Log.getLogger().log(Level.SEVERE, "Uncaught Exception", throwable);
                handleModificationException(throwable);
            }
        };
        Thread thread = new Thread(group, "Safe Main Thread") {
            @Override
			public void run() {
              try {
                realmain(args);
              } catch (Throwable t) {
                Log.getLogger().log(Level.INFO, "Exception caught", t);
              }
            }
        };
        thread.setDaemon(true);
        thread.start();
        // We have to give the thread time to get going or the entire application will exit!
        SystemUtilities.sleepMsec(5000);
    }

    public static void realmain(String[] args) {
        initialize();
        _splashScreen = new SplashScreen();
        try {
            init(args);
        } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "failed to initialize", e);
        }
        _splashScreen.dispose();
        _splashScreen = null;
    }

    private static void parseOptions(String[] args) {
    	for (int i = 0; i < args.length; i++) {
    		String option = args[i];
			try {				
				if (option.startsWith(propertyOptionsPrefix)) {
					int index = option.indexOf("=");
					if (index != -1) {
						String propName = option.substring(5, index);
						String value = option.substring(index+1);
						ApplicationProperties.setString(propName, value);
					}
				}
			} catch (Throwable t) {
				Log.getLogger().log(Level.WARNING, "Error at parsing or setting application property: " + option +
						". This option will be ignored.", t);
			}
		}
    }

    public static void repaint() {
        if (_mainFrame != null) {
            _mainFrame.repaint();
        }
    }

    private static void showMainFrame() {
        _splashScreen.setVisible(false);
        _mainFrame.setVisible(true);
        ProjectManager.getProjectManager().bringErrorFrameToFront();
    }

    public static void shutdown() {
        _mainFrame.dispatchEvent(new WindowEvent(_mainFrame, WindowEvent.WINDOW_CLOSING));
    }
    
    private static boolean handleModificationException(Throwable t) {  
    	Throwable exception = t;
    	boolean foundModEx = false;
    	while (!foundModEx && exception != null) {
    		if (exception instanceof ModificationException) {
    			if (_mainFrame != null) { //in UI mode
    				ModalDialog.showMessageDialog(_mainFrame, 
    					"You do not have permission to make this modification.",
    					"Write error");  
    			}
    			foundModEx = true;
    			break;
    		}
    		exception = exception.getCause();
    	}
    	return foundModEx;	
    }

}
