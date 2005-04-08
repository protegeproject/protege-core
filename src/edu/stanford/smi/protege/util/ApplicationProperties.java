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
    public final static String FILE_NAME = "protege.properties";
    public final static String NEXT_FRAME_NUMBER = "next_frame_number";
    public final static String APPLICATION_INSTALL_DIRECTORY = "protege.dir";
    public final static String LAST_FILE_DIRECTORY = "filechooser.last_directory";
    public final static String LAST_LOADED_URI = "projectchooser.last_uri";
    public final static String CURRENT_WORKING_DIRECTORY = "user.dir";
    public final static String USERS_HOME_DIRECTORY = "user.home";
    public final static String PROPERTIES_IN_USER_HOME = "protege.properties.in.user.home";
    public final static String EXTRA_MANIFEST_PATH = PluginUtilities.EXTRA_MANIFEST_PATH;
    public final static String MRU_PROJECTS = "history.projects.reopen";
    public final static String WELCOME_DIALOG = "ui.welcomedialog.show";
    public final static String MAIN_FRAME_RECTANGLE = "mainframe.rectangle";
    public final static String LOOK_AND_FEEL = "swing.defaultlaf";
    public final static String BROWSER = "browser.html";
    private static final String AUTOSYNCHRONIZE_PROPERTY = "trees.autosynchronize";
    private static final String PRETTY_PRINT_SLOT_WIDGET_LABELS = "labels.pretty_print";

    private final static Properties _properties = new Properties();
    private static File _propertyFile;

    private final static int num_MRUProjects = 10;
    private static List _mruProjectList = new ArrayList(num_MRUProjects);

    static {
        try {
            _propertyFile = new File(getPropertiesDirectory(), FILE_NAME);
            InputStream is = new FileInputStream(_propertyFile);
            _properties.load(is);
            is.close();
            loadMRUProjectList();

        } catch (IOException e) {
            // Log.exception(e, ApplicationProperties.class, "<static>");
        } catch (SecurityException e) {
        }
    }

    public static void setLookAndFeel(String lookAndFeelName) {
        setProperty(LOOK_AND_FEEL, lookAndFeelName);
    }

    public static String getLookAndFeelClassName() {
        String name = getApplicationOrSystemProperty(LOOK_AND_FEEL);
        if (name == null) {
            name = "com.jgoodies.plaf.plastic.PlasticLookAndFeel";
        }
        return name;
    }

    private static void loadMRUProjectList() {
        String projectNames = _properties.getProperty(MRU_PROJECTS);
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
                    // Not a file so just add it anyway.
                    _mruProjectList.add(uri);
                }
            }
        } else {
            // If there are none, use some example projects provided
            // with the protege installation.
            char sep = java.io.File.separatorChar;
            String exampleProjectName = getApplicationDirectory().getPath() + sep + "examples" + sep + "newspaper"
                    + sep + "newspaper.pprj";
            URI uri = URIUtilities.createURI(exampleProjectName);
            addProjectToMRUList(uri);
        }
    }

    public static void addProjectToMRUList(URI uri) {
        if (uri != null) {
            uri = uri.normalize();
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
                _properties.store(os, "Protege Properties");
                os.close();
            }
        } catch (IOException e) {
            Log.getLogger().warning(e.toString());
        } catch (SecurityException e) {
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
        String propString = _properties.getProperty(name);
        if (propString != null) {
            try {
                value = Integer.valueOf(propString).intValue();
            } catch (Exception e) {
                // do nothing
            }
        }
        return value;
    }

    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        boolean value = defaultValue;
        String propString = _properties.getProperty(name);
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
        String nextInstanceString = _properties.getProperty(NEXT_FRAME_NUMBER, "0");
        int nextInstance = Integer.parseInt(nextInstanceString);
        // properties.setProperty(NEXT_FRAME_NUMBER,
        // String.valueOf(nextInstance+1));
        return nextInstance;
    }

    public static String getBrowser() {
        String property = _properties.getProperty(BROWSER);
        if (property != null && property.length() == 0) {
            property = null;
        }
        return property;
    }

    private static Rectangle getRectangle(String name) {
        Rectangle rectangle = null;
        String property = _properties.getProperty(name);
        if (property != null) {
            rectangle = parseRectangle(property);
        }
        return rectangle;
    }

    public static String getApplicationOrSystemProperty(String name) {
        return getApplicationOrSystemProperty(name, null);
    }

    public static String getApplicationOrSystemProperty(String name, String defaultValue) {
        String value = _properties.getProperty(name);
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
        return _properties.getProperty(name, defaultValue);
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
                _properties.remove(property);
            } else {
                _properties.setProperty(property, value);
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
        return "http://protege.stanford.edu/doc/tutorial/get_started";
    }

    public static String getFAQURLString() {
        return "http://protege.stanford.edu/doc/faq.html";
    }

    public static String getUsersGuideURLString() {
        return "http://protege.stanford.edu/doc/users_guide";
    }

    public static String getAllHelpURLString() {
        return "http://protege.stanford.edu/doc/users.html";
    }

    public static String getOntology101URLString() {
        return "http://protege.stanford.edu/publications/ontology_development/ontology101.html";
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
}