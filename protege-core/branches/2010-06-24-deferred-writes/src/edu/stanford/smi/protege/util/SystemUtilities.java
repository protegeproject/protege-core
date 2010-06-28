package edu.stanford.smi.protege.util;

//ESCA*JAVA0267
//ESCA*JAVA0266
//ESCA*JAVA0170

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.resource.Text;

/**
 * A set of utilities for accessing the underlying system and for manipulating system level objects.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 * @author Joe Edelman (jxe@dartmouth.edu)
 */
public class SystemUtilities {
    private static final Logger  log = Log.getLogger(SystemUtilities.class);

    private static final String OLD_PLASTIC_LAF_NAME = "com.jgoodies.plaf.plastic.PlasticLookAndFeel";
    private static final String NEW_PLASTIC_LAF_NAME = "com.jgoodies.looks.plastic.PlasticLookAndFeel";

    private static boolean isMac;
    private static boolean isWindows;
    private static boolean isApplet;
    private static boolean useAntialiasing;
    static {
        init();
    }

    public static void initialize() {
        // just to call the static initializers.
    }

    public static void initGraphics() {
        loadLookAndFeel();
    }

    public static void debugBreak() {
    }

    public static void exit() {
        try {
            System.exit(0);
        } catch (SecurityException e) {
            // do nothing
        }
    }

    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    public static Class forName(String className) {
        return PluginUtilities.forName(className);
    }

    public static Class forName(String className, boolean promiscuous) {
        return PluginUtilities.forName(className, promiscuous);
    }

    public static void gc() {
        //ESCA-JAVA0284
        System.gc();
        System.runFinalization();
        //ESCA-JAVA0284
        System.gc();
    }

    public static String getSystemProperty(String property) {
        String value;
        try {
            value = System.getProperty(property);
        } catch (SecurityException e) {
            value = null;
        }
        return value;
    }

    public static String getSystemProperty(String property, String defaultValue) {
        String value;
        try {
            value = System.getProperty(property, defaultValue);
        } catch (SecurityException e) {
            value = defaultValue;
        }
        return value;
    }

    public static boolean getSystemBooleanProperty(String property) {
        boolean value = false;
        try {
            value = Boolean.getBoolean(property);
        } catch (SecurityException e) {
            //do nothing
        } catch (Throwable t) {
        	//do nothing
        }
        return value;
    }

    public static boolean getSystemBooleanProperty(String property, boolean defaultValue) {
        boolean value = defaultValue;
        try {
            value = Boolean.getBoolean(property);
        } catch (SecurityException e) {
            //do nothing
        } catch (Throwable t) {
        	//do nothing
        }
        return value;
    }

    public static int getSystemIntegerProperty(String property) {
        int value = 0;
        try {
            value = Integer.getInteger(property).intValue();
        } catch (SecurityException e) {
            //do nothing
        } catch (Throwable t) {
        	//do nothing
        }
        return value;
    }

    public static int getSystemIntegerProperty(String property, int defaultValue) {
        int value = defaultValue;
        try {
        	Integer i = Integer.getInteger(property);
        	if (i != null) {
        		value = i.intValue();
        	}
        } catch (SecurityException e) {
            //do nothing
        } catch (Throwable t) {
        	//do nothing
        }
        return value;
    }


    public static String getUserDirectory() {
        return getSystemProperty("user.dir");
    }

    public static String getLineSeparator() {
        return getSystemProperty("line.separator");
    }

    public static String getMachineName() {
        String machineName;
        try {
            machineName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            machineName = "Unknown";
        }
        return machineName;
    }

    public static String getMachineIpAddress() {
        String machineIpAddress;
        try {
            machineIpAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Log.getLogger().warning("Unable to determine ip address");
            machineIpAddress = "127.0.0.1";
        }
        return machineIpAddress;
    }

    public static String getFileEncoding() {
        return getSystemProperty("protege.file.encoding", "UTF-8");
    }

    public static String getUserName() {
        return getSystemProperty("user.name");
    }

    private static void init() {
        try {

            logSystemInfo();
            loadParameters();
            PluginUtilities.initialize();
            loadUseAntialiasing();
            //ESCA-JAVA0170
        } catch (Throwable e) {
            // We explicitly do nothing fancy with writing this output. This
            // method is called on startup
            // and there may be problems with almost anything (i.e. System.out).
            // This is the best chance we
            // have to getting a reasonable error message.
            //ESCA-JAVA0266
            System.out.println(e.getMessage());
            // e.printStackTrace();
        }
    }

    private static void loadParameters() {
        String osName = getSystemProperty("os.name");
        isMac = osName.indexOf("Mac") != -1;
        isWindows = osName.indexOf("Windows") != -1;
    }

    private static void loadLookAndFeel() {
        String lafName = ApplicationProperties.getLookAndFeelClassName();
        if (lafName.equals(OLD_PLASTIC_LAF_NAME)) {
            lafName = NEW_PLASTIC_LAF_NAME;
            ApplicationProperties.setLookAndFeel(lafName);
        }
        setLookAndFeel(lafName);
    }

    public static void setLookAndFeel(String lafName) {
        try {
            LookAndFeel lookAndFeel = (LookAndFeel) Class.forName(lafName).newInstance();
            if (lafName.indexOf("Plastic") != -1) {
               LookAndFeelUtil.setUpPlasticLF();
            } else if (lafName.indexOf("Metal") != -1) {
                MetalLookAndFeel.setCurrentTheme(createDefaultMetalTheme());
            }
            UIManager.put("ClassLoader", lookAndFeel.getClass().getClassLoader());
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException e) {
            Log.getLogger().warning("Look and feel not found: " + lafName);
        } catch (Exception e) {
            Log.getLogger().warning(e.toString());
        }
    }

    public static boolean isWindows() {
        return isWindows;
    }

    public static boolean isMac() {
        return isMac;
    }

    public static boolean modalDialogInDropWorks() {
        // Is this always working now in JDK 1.4?
        return true;
    }

    public static Object newInstance(Class clas, Class[] argumentClasses, Object[] arguments) {
        Object instance = null;
        try {
            Constructor constructor = clas.getConstructor(argumentClasses);
            instance = constructor.newInstance(arguments);
        } catch (Throwable e) {
            Log.getLogger().warning(Log.toString(e));
        }
        return instance;
    }

    public static Object newInstance(String className) {
        Object o = null;
        try {
            Class clas = forName(className);
            if (clas == null) {
                Log.getLogger().warning("no such class: " + className);
            } else {
                o = clas.newInstance();
            }
        } catch (Throwable e) {
            Log.getLogger().warning(Log.toString(e));
        }
        return o;
    }

    public static Object newInstance(Class clas) {
        Object o = null;
        try {
            o = clas.newInstance();
        } catch (Throwable e) {
            Log.getLogger().warning(Log.toString(e));
        }
        return o;
    }

    public static void pause() {
        try {
            System.out.flush();
            System.err.flush();
            System.out.print("Press <Enter> to continue");
            System.in.read();
            while (System.in.available() != 0) {
                System.in.read();
            }
        } catch (Exception e) {
            Log.getLogger().warning(Log.toString(e));
        }
    }

    public static void printMemoryUsage() {
        gc();
        printMemoryUsageNoGC();
    }

    public static void printMemoryUsageNoGC() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        String s = "memory: total=" + total + ", used=" + (total - free);
        Log.getLogger().finest(s);
    }

    public static void logSystemInfo() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Text.getProgramTextName());
        buffer.append(" ");
        buffer.append(Text.getVersion());
        buffer.append(" ");
        buffer.append(Text.getBuildInfo());
        buffer.append(", JVM ");
        buffer.append(getSystemProperty("java.runtime.version"));
        buffer.append(", memory=");
        buffer.append(Runtime.getRuntime().maxMemory() / 1000000);
        buffer.append("M, ");
        buffer.append(getSystemProperty("os.name"));
        buffer.append(", encoding=");
        buffer.append(getFileEncoding());
        buffer.append(", language=");
        buffer.append(Locale.getDefault().getLanguage());
        buffer.append(", country=");
        buffer.append(Locale.getDefault().getCountry());
        Log.getLogger().config(buffer.toString());
    }

    private static class PropertyComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Map.Entry entry1 = (Map.Entry) o1;
            Map.Entry entry2 = (Map.Entry) o2;
            return entry1.getKey().toString().compareToIgnoreCase(entry2.getKey().toString());
        }
    }

    public static void printSystemProperties(PrintStream stream) {
        stream.println("System Properties:");
        List entries = new ArrayList(System.getProperties().entrySet());
        Collections.sort(entries, new PropertyComparator());
        Iterator i = entries.iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = entry.getKey().toString();
            if (!key.startsWith("lax.")) {
                stream.println("\t" + key + "=" + entry.getValue());
            }
        }
    }

    public static void showHTML(String url) {
        try {
            if (!url.startsWith("http:") && !url.startsWith("file:") && !url.startsWith("mailto:")) {
                url = new File(url).toURI().toURL().toString();
            }
            if (log.isLoggable(Level.FINE)) {
              log.fine("showHTML " + url);
            }
            BrowserLauncher.openURL(url);
        } catch (IOException e) {
            Log.getLogger().warning(e.toString());
        }
    }

    public static void sleepMsec(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (Exception e) {
            Log.getLogger().warning(e.toString());
        }
    }

    public static Boolean toBoolean(Object o) {
        Boolean b;
        if (o instanceof Boolean) {
            b = (Boolean) o;
        } else {
            String s = o.toString();
            if (s.equalsIgnoreCase("true")) {
                b = Boolean.TRUE;
            } else if (s.equalsIgnoreCase("false")) {
                b = Boolean.FALSE;
            } else {
                b = null;
            }
        }
        return b;
    }

    public static Float toFloat(Object o) {
        Float f;
        if (o instanceof Float) {
            f = (Float) o;
        } else {
            try {
                f = Float.valueOf(o.toString());
            } catch (Exception e) {
                f = null;
            }
        }
        return f;
    }

    public static Integer toInteger(Object o) {
        Integer i;
        if (o instanceof Integer) {
            i = (Integer) o;
        } else {
            try {
                i = Integer.valueOf(o.toString());
            } catch (Exception e) {
                i = null;
            }
        }
        return i;
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static Collection getClassesWithAttribute(String key, String value) {
        return PluginUtilities.getClassesWithAttribute(key, value);
    }

    public static void setContextClassLoader(Object o) {
        try {
            Thread.currentThread().setContextClassLoader(o.getClass().getClassLoader());
        } catch (Exception e) {
            // Do nothing. This happens in applets
        }
    }

    public static boolean showAlphaFeatures() {
        boolean showAlpha = false;
        String showAlphaString = SystemUtilities.getSystemProperty("protege.alpha");
        if (showAlphaString != null) {
            showAlpha = Boolean.valueOf(showAlphaString).booleanValue();
        }
        return showAlpha;
    }

    public static boolean useAntialiasing() {
        return useAntialiasing;
    }

    private static void loadUseAntialiasing() {
        String property = ApplicationProperties.getApplicationOrSystemProperty("antialiasing.enable");
        if (property == null) {
            useAntialiasing = UIManager.getLookAndFeel().getClass().getName().indexOf("Plastic") != -1 && !isMac();
        } else {
            useAntialiasing = Boolean.valueOf(property).booleanValue();
        }
    }

    private static MetalTheme createDefaultMetalTheme() {
        MetalTheme theme;
        if (isJDK15()) {
            theme = (MetalTheme) newInstance("javax.swing.plaf.metal.OceanTheme");
        } else {
            theme = new DefaultMetalTheme();
        }
        return theme;
    }

    private static boolean isJDK15() {
        String property = getSystemProperty("java.runtime.version");
        return property.startsWith("1.5.");
    }

    public static Locale getSystemLocale() {
        String country = System.getProperty("user.country");
        String language = System.getProperty("user.language");
        return new Locale(language, country);
    }

    public static Locale getProtegeSystemDefaultLocale() {
        return new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());
    }

    public static boolean isApplet() {
        return isApplet;
    }

    public static void setApplet(boolean b) {
        isApplet = b;
    }

}

