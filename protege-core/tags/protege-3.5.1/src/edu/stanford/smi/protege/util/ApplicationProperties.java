package edu.stanford.smi.protege.util;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.List;

import edu.stanford.smi.protege.plugin.*;

/**
 * Utility class for accessing system properties and properties from the application properties file.
 *
 * @author Ray Fergerson
 * @author Jennifer Vendetti
 */
public class ApplicationProperties {
    public static final String FILE_NAME = "protege.properties";
    public static final String NEXT_FRAME_NUMBER = "next_frame_number";
    public static final String APPLICATION_INSTALL_DIRECTORY = "protege.dir";
    public static final String LAST_FILE_DIRECTORY = "filechooser.last_directory";
    public static final String LAST_LOADED_URI = "projectchooser.last_uri";
    public static final String CURRENT_WORKING_DIRECTORY = "user.dir";
    public static final String USERS_HOME_DIRECTORY = "user.home";
    public static final String PROPERTIES_IN_USER_HOME = "protege.properties.in.user.home";
    public static final String EXTRA_MANIFEST_PATH = PluginUtilities.EXTRA_MANIFEST_PATH;
    public static final String MRU_PROJECTS = "history.projects.reopen";
    public static final String WELCOME_DIALOG = "ui.welcomedialog.show";
    public static final String WELCOME_DIALOG_START_IN_SERVER_PANEL = "ui.welcomedialog.start.in.server.panel";
    
    public static final String MAIN_FRAME_RECTANGLE = "mainframe.rectangle";
    public static final String LOOK_AND_FEEL = "swing.defaultlaf";
    public static final String BROWSER = "browser.html";
    private static final String AUTOSYNCHRONIZE_PROPERTY = "trees.autosynchronize";
    private static final String PRETTY_PRINT_SLOT_WIDGET_LABELS = "labels.pretty_print";

    public static final String LOG_FILE_PROPERTY = "java.util.logging.config.file";
    public static final String LOG_DEBUG_PROPERTY = "log.config.debug";

    public static final String REMOTE_CLIENT_PRELOAD = "remote.client.preload";
    
    public static final String URL_CONNECT_TIMEOUT = "url.connect.timeout";
    public static final String URL_CONNECT_READ_TIMEOUT = "url.connect.read.timeout";
    
    public static final String SORT_CLASS_TREE = "ui.sort.class.tree";    
    public static final String SORT_SLOTS_TREE = "ui.sort.slot.tree";
    
    private static final Properties PROPERTIES = new Properties();
    private static File _propertyFile;

    private static final int num_MRUProjects = 10;
    private static List _mruProjectList = new ArrayList(num_MRUProjects);

    static {
        try {
            _propertyFile = new File(getPropertiesDirectory(), FILE_NAME);
            InputStream is = new FileInputStream(_propertyFile);
            PROPERTIES.load(is);
            is.close();
            loadMRUProjectList();

        } catch (IOException e) {
            // Log.exception(e, ApplicationProperties.class, "<static>");
        } catch (SecurityException e) {
            // do nothing -- expected in applets
        }
    }

    public static void setLookAndFeel(String lookAndFeelName) {
        setProperty(LOOK_AND_FEEL, lookAndFeelName);
    }

    public static String getLookAndFeelClassName() {
        String name = getApplicationOrSystemProperty(LOOK_AND_FEEL);
        if (name == null) {
            name = "com.jgoodies.looks.plastic.PlasticLookAndFeel";
        }
        return name;
    }

    private static void loadMRUProjectList() {
        String projectNames = PROPERTIES.getProperty(MRU_PROJECTS);
        if (projectNames != null) {
            StringTokenizer st = new StringTokenizer(projectNames, ",");
            for (int i = 0; ((i < num_MRUProjects) && (st.hasMoreElements())); i++) {
                String projectString = (String) st.nextElement();
                URI uri = URIUtilities.createURI(projectString);
                try {
                    // If its a file then check that it still exists
                    File file = new File(uri);
                    if (file.exists()) {
                        _mruProjectList.add(uri);
                    }
                } catch (IllegalArgumentException e) {
                    if (!uri.getScheme().equals("rmi")) {
                        // Not a file so just add it anyway.
                        _mruProjectList.add(uri);
                    }
                }
            }
        } else {
            // If there are none, use some example projects provided with the
            // protege installation.
            char sep = java.io.File.separatorChar;
            String exampleProjectName;
            URI uri;
            
            if (PluginUtilities.isPluginAvailable("edu.stanford.smi.protege.collab.projectPlugin.ProtegeCollabGUIProjectPlugin")) {
                exampleProjectName = getApplicationDirectory().getPath() +
                sep + "examples" + sep + "collaborativePizza" + sep + "collaborativePizza.owl.pprj";
            uri = URIUtilities.createURI(exampleProjectName);
            addProjectToMRUList(uri);
            }
            
            if (PluginUtilities.isOWLAvailable()) {
            	exampleProjectName = getApplicationDirectory().getPath() + sep + "examples" + sep + "pizza" + sep + "pizza.owl.pprj";
                uri = URIUtilities.createURI(exampleProjectName);
                addProjectToMRUList(uri);
                
                exampleProjectName = getApplicationDirectory().getPath() + sep + "examples" + sep + "sqwrl" + sep + "SQWRLCollectionExamples.pprj";
                uri = URIUtilities.createURI(exampleProjectName);
                addProjectToMRUList(uri);

                exampleProjectName = getApplicationDirectory().getPath() + sep + "examples" + sep + "sqwrl" + sep + "SQWRLCoreExamples.pprj";
                uri = URIUtilities.createURI(exampleProjectName);
                addProjectToMRUList(uri);
            }

            exampleProjectName = getApplicationDirectory().getPath() +
                sep + "examples" + sep + "newspaper" + sep + "newspaper.pprj";
            uri = URIUtilities.createURI(exampleProjectName);
            addProjectToMRUList(uri);            

            
        }
    }

    public static void addProjectToMRUList(URI uri) {
        if (uri != null && !uri.getScheme().equals("rmi")) {
            uri = URIUtilities.normalize(uri);
            _mruProjectList.remove(uri);
            _mruProjectList.add(0, uri);

            // Trim off the last element if necessary.
            if (_mruProjectList.size() > num_MRUProjects) {
                _mruProjectList.remove(num_MRUProjects);
            }
            saveMRUProjectList();
        }
    }

    public static void flush() {
        try {
            if (_propertyFile != null) {
                OutputStream os = new FileOutputStream(_propertyFile);
                PROPERTIES.store(os, "Protege Properties");
                os.close();
            }
        } catch (IOException e) {
            Log.getLogger().warning(e.toString());
        } catch (SecurityException e) {
            // do nothing -- expected in applets
        }
    }

    private static File getPropertiesDirectory() {
        boolean useUserHome = Boolean.getBoolean(PROPERTIES_IN_USER_HOME);
        File dir;
        if (useUserHome) {
            String s = SystemUtilities.getSystemProperty(USERS_HOME_DIRECTORY);
            dir = (s == null) ? null : new File(s);
        } else {
            dir = getApplicationDirectory();
        }
        return dir;
    }

    public static File getLogFileDirectory() {
        File file = getPropertiesDirectory();
        if (file != null) {
            file = new File(file, "logs");
            file.mkdir();
        }
        return file;
    }

    public static File getApplicationDirectory() {
        String dir = SystemUtilities.getSystemProperty(APPLICATION_INSTALL_DIRECTORY);
        if (dir == null) {
            dir = SystemUtilities.getSystemProperty(CURRENT_WORKING_DIRECTORY);
        }
        return dir == null ? null : new File(dir);
    }

    public static String getExtraManifestPath() {
        String s = SystemUtilities.getSystemProperty(EXTRA_MANIFEST_PATH);
        if (s != null && s.length() > 1 && s.charAt(0) == '"') {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    public static int getIntegerProperty(String name, int defaultValue) {
        int value = defaultValue;
        String propString = PROPERTIES.getProperty(name);
        if (propString != null) {
            try {
                value = Integer.parseInt(propString);
            } catch (Exception e) {
                // do nothing
            }
        }
        return value;
    }

    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        boolean value = defaultValue;
        String propString = PROPERTIES.getProperty(name);
        if (propString != null) {
            try {
                value = Boolean.valueOf(propString).booleanValue();
            } catch (Exception e) {
                // do nothing
            }
        }
        return value;
    }

    /**
     * @return List of URI's for MRU projects
     */
    public static List getMRUProjectList() {
        return new ArrayList(_mruProjectList);
    }

    public static int getOldNextFrameNumber() {
        String nextInstanceString = PROPERTIES.getProperty(NEXT_FRAME_NUMBER, "0");
        int nextInstance = Integer.parseInt(nextInstanceString);
        // properties.setProperty(NEXT_FRAME_NUMBER,
        // String.valueOf(nextInstance+1));
        return nextInstance;
    }

    public static String getBrowser() {
        String property = PROPERTIES.getProperty(BROWSER);
        if (property != null && property.length() == 0) {
            property = null;
        }
        return property;
    }

    private static Rectangle getRectangle(String name) {
        Rectangle rectangle = null;
        String property = PROPERTIES.getProperty(name);
        if (property != null) {
            rectangle = parseRectangle(property);
        }
        return rectangle;
    }
    
    public static Properties getApplicationProperties() {
    	return PROPERTIES;
    }
    
    public static String getApplicationOrSystemProperty(String name) {
        return getApplicationOrSystemProperty(name, null);
    }

    public static String getApplicationOrSystemProperty(String name, String defaultValue) {
        String value = PROPERTIES.getProperty(name);
        if (value == null) {
            try {
                value = System.getProperty(name);
            } catch (AccessControlException e) {
                // do nothing, happens in applets
            }
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public static String getString(String name) {
        return getString(name, null);
    }

    public static String getString(String name, String defaultValue) {
        return PROPERTIES.getProperty(name, defaultValue);
    }

    private static Rectangle parseRectangle(String text) {
        int[] numbers = new int[4];
        int index = 0;
        StringTokenizer st = new StringTokenizer(text);
        while (st.hasMoreTokens() && index < numbers.length) {
            String token = st.nextToken();
            numbers[index] = Integer.parseInt(token);
            ++index;
        }
        return new Rectangle(numbers[0], numbers[1], numbers[2], numbers[3]);
    }

    public static void recordMainFrameProperties(Frame mainFrame) {
        saveRectangle(MAIN_FRAME_RECTANGLE, mainFrame.getBounds());
    }

    public static void restoreMainFrameProperties(Frame mainFrame) {
        Rectangle r = getRectangle(MAIN_FRAME_RECTANGLE);
        if (r == null) {
            mainFrame.setSize(ComponentUtilities.getDefaultMainFrameSize());
            ComponentUtilities.center(mainFrame);
        } else {
            mainFrame.setBounds(r);
        }
    }

    private static void saveMRUProjectList() {
        StringBuffer buf = new StringBuffer();
        int size = _mruProjectList.size();
        for (int i = 0; i < size; i++) {
            buf.append(_mruProjectList.get(i));
            buf.append(",");
        }
        // Get rid of the comma on the end.
        buf.setLength(buf.length() - 1);
        setProperty(MRU_PROJECTS, buf.toString());
    }

    private static void setProperty(String property, String value) {
        try {
            if (value == null) {
                PROPERTIES.remove(property);
            } else {
                PROPERTIES.setProperty(property, value);
            }
            flush();
        } catch (Exception e) {
            Log.getLogger().warning(Log.toString(e));
        }
    }

    private static void saveRectangle(String name, Rectangle r) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.valueOf(r.x));
        buffer.append(" ");
        buffer.append(String.valueOf(r.y));
        buffer.append(" ");
        buffer.append(String.valueOf(r.width));
        buffer.append(" ");
        buffer.append(String.valueOf(r.height));
        setProperty(name, buffer.toString());
    }

    public static void setInt(String name, int value) {
        setProperty(name, String.valueOf(value));
    }

    public static void setBoolean(String name, boolean value) {
        setProperty(name, String.valueOf(value));
    }

    public static void setString(String name, String value) {
        setProperty(name, value);
    }

    public static boolean getWelcomeDialogShow() {
        String s = getString(WELCOME_DIALOG, "true");
        return s.equalsIgnoreCase("true");
    }

    public static void setUserName(String name) {
        setProperty("user.name", name);
    }

    public static void setWelcomeDialogShow(boolean b) {
        setBoolean(WELCOME_DIALOG, b);
    }

    public static boolean isAutosynchronizingClsTrees() {
        return getBooleanProperty(AUTOSYNCHRONIZE_PROPERTY, true);
    }

    public static void setAutosynchronizingClsTrees(boolean b) {
        setBoolean(AUTOSYNCHRONIZE_PROPERTY, b);
    }

    public static String getGettingStartedURLString() {
        return "http://protege.stanford.edu/doc/tutorial/get_started/table_of_content.html";
    }

    public static String getFAQURLString() {
        return "http://protege.stanford.edu/doc/faq.html";
    }

    public static String getUsersGuideURLString() {
        return "http://protegewiki.stanford.edu/index.php/PrF_UG";
    }

    public static String getAllHelpURLString() {
        return "http://protege.stanford.edu/doc/users.html";
    }

    public static String getOntology101URLString() {
        return "http://protege.stanford.edu/publications/ontology_development/ontology101.html";
    }
    
    public static String getHowToCiteProtegeURLString() {
    	return "http://protegewiki.stanford.edu/wiki/HowToCiteProtege";
    }

    public static String getUserName() {
        return getApplicationOrSystemProperty("user.name");
    }

    public static Locale getLocale() {
        String language = getApplicationOrSystemProperty("user.language");
        String country = getApplicationOrSystemProperty("user.country");
        return new Locale(language, country);
    }

    public static void setLocale(Locale locale) {
        setProperty("user.language", locale.getLanguage());
        setProperty("user.country", locale.getCountry());
    }

    public static boolean getPrettyPrintSlotWidgetLabels() {
        return getBooleanProperty(PRETTY_PRINT_SLOT_WIDGET_LABELS, true);
    }

    public static void setPrettyPrintSlotWidgetLabels(boolean b) {
        setBoolean(PRETTY_PRINT_SLOT_WIDGET_LABELS, b);
    }

    public static File getLastFileDirectory() {
        String directory = getString(LAST_FILE_DIRECTORY);
        if (directory == null) {
            directory = getApplicationOrSystemProperty(USERS_HOME_DIRECTORY);
        }
        return new File(directory);
    }

    public static void setLastFileDirectory(File directory) {
        setString(LAST_FILE_DIRECTORY, directory.getPath());
    }

    public static URI getLastLoadeURI() {
        URI uri = null;
        String uriString = getString(LAST_LOADED_URI);
        if (uriString != null) {
            try {
                uri = new URI(uriString);
            } catch (URISyntaxException e) {
                // do nothing
            }
        }
        return uri;
    }

    public static void setLastLoadedURI(URI uri) {
        setString(LAST_LOADED_URI, uri.toString());
    }
    
    /**
     * @return The URL connect timeout in seconds as set in the protege.properties file for the property url.connect.timeout.
     *  It returns the default value 15 seconds, if the url connect property is not set in protege.properties.
     */
    public static int getUrlConnectTimeout() {
    	int timeout = 15;
    		
    	String timeoutString = getApplicationOrSystemProperty(URL_CONNECT_TIMEOUT, "15");
    	
    	try {
    		timeout = Integer.parseInt(timeoutString);
		} catch (NumberFormatException e) {
			Log.getLogger().warning("Error parsing " + timeoutString + " to an int. Cannot set URL connect timeout. Use default value (15 sec).");
		}
    	    	
    	return timeout;
    }
    
    /**
     * Sets the connect timeout. 
     * This value is written to the protege.properties file when the project is saved as:
     * <p> url.connect.timeout=timeout
     * @param timeout in seconds.
     */
    public static void setUrlConnectTimeout(int timeout) {
    	setInt(URL_CONNECT_TIMEOUT, timeout);
    }
    
    /**
     * @return The URL connect read timeout in seconds as set in the protege.properties file for the property url.connect.read.timeout.
     *  It returns the default value 15 seconds, if the url connect read property is not set in protege.properties.
     */
    public static int getUrlConnectReadTimeout() {
    	int timeout = 15;
    		
    	String timeoutString = getApplicationOrSystemProperty(URL_CONNECT_READ_TIMEOUT, "15");
    	
    	try {
    		timeout = Integer.parseInt(timeoutString);
		} catch (NumberFormatException e) {
			Log.getLogger().warning("Error parsing " + timeoutString + " to an int. Cannot set URL read connect timeout. Use default value (15 sec).");
		}
    	    	
    	return timeout;
    }

    /**
     * Sets the connect read timeout.
      * This value is written to the protege.properties file when the project is saved as:
     * <p> url.connect.read.timeout=timeout
     * @param timeout in seconds.
     */
    public static void setUrlConnectReadTimeout(int timeout) {
    	setInt(URL_CONNECT_READ_TIMEOUT, timeout);
    }
    
    public static void setSortClassTreeOption(boolean classTreeSorted) {
    	setBoolean(SORT_CLASS_TREE, classTreeSorted);
    }
    
    public static void setSortSlotTreeOption(boolean propertiesTreeSorted) {
    	setBoolean(SORT_SLOTS_TREE, propertiesTreeSorted);
    }
    
    public static boolean getSortClassTreeOption() {
    	return getBooleanProperty(SORT_CLASS_TREE, true);
    }

    public static boolean getSortSlotTreeOption() {
    	return getBooleanProperty(SORT_SLOTS_TREE, true);
    }
    
}
