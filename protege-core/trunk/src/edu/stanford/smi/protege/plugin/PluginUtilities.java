package edu.stanford.smi.protege.plugin;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PluginUtilities {
    private static final String TAB_WIDGET = "Tab-Widget";
    private static final String SLOT_WIDGET = "Slot-Widget";
    private static final String CLS_WIDGET = "Cls-Widget";
    private static final String IMPORT_PLUGIN = "Import-Plugin";
    private static final String CREATE_PROJECT_PLUGIN = "Create-Project-Plugin";
    private static final String EXPORT_PLUGIN = "Export-Plugin";
    private static final String EXPORT_PROJECT_PLUGIN = "Export-Project-Plugin";
    private static final String PROJECT_PLUGIN = "Project-Plugin";
    private static final String FACTORY_PLUGIN = "Storage-Factory";

    private static Collection _manifestURLs = new HashSet();
    private static Map pluginToNameMap = new HashMap();
    private static Map _pluginClassNameToClassLoaderMap = new HashMap();
    private static List _factories;
    private static Map _defaultSlotWidgetNames = new HashMap();
    private static Set _pluginComponentNames = new HashSet();
    private static Map _pluginComponentNameToAboutURLMap = new HashMap();
    private static Map _pluginComponentNameToDocURLMap = new HashMap();
    private static Map _pluginPackageToClassLoaderMap = new HashMap();
    private static String defaultFactoryClassName;
    private static Map cachedClsesWithAttributeMap = new HashMap();

    private static File pluginsDir;

    private static final String PROPERTIES_FILE_NAME = "plugin.properties";
    private static final String PLUGIN_COUNT_PROPERTY = "plugin.component.count";
    private static final String PLUGIN_NAME_PROPERTY = "plugin.component.name";
    private static final String ABOUT_PROPERTY = "plugin.component.about";
    private static final String DOC_PROPERTY = "plugin.component.doc";
    private static final String DEPENDENCY_COUNT_PROPERTY = "plugin.dependency.count";
    private static final String DEPENDENCY_PROPERTY = "plugin.dependency";

    public final static String EXTRA_MANIFEST_PATH = "protege.plugin.manifest";

    private static FilenameFilter _pluginPackageFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return new File(dir, name).isDirectory() && !name.equalsIgnoreCase("meta-inf") && !name.startsWith(".");
        }
    };

    static {
        init();
    }

    public static File getInstallationDirectory(String pluginClassName) {
        File directory = null;
        ClassLoader loader = (ClassLoader) _pluginClassNameToClassLoaderMap.get(pluginClassName);
        if (loader instanceof DirectoryClassLoader) {
            directory = ((DirectoryClassLoader) loader).getDirectory();
        }
        return directory;
    }

    public static void initialize() {
        // do nothing except ensure that the static initalizer has run
    }

    private static String attributeNameToClassName(String attributeName) {
        String className;
        if (attributeName.endsWith(".class")) {
            className = attributeName.substring(0, attributeName.length() - 6);
        } else {
            className = attributeName;
        }
        className = className.replace('/', '.');
        return className;
    }

    private static ClassLoader createClassLoader(File directory, ClassLoader parentLoader) {
        // return new DirectoryClassLoader(directory, parentLoader);
        return (directory == null) ? parentLoader : new DirectoryClassLoader(directory, parentLoader);
    }

    public static Class forName(String className) {
        return forName(className, false);
    }

    /**
     * Load a class with the given name. The system first tries to load the
     * class using the appropriate class loader for a class with that name. If
     * this fails and promiscous=true then the system will try to load the class
     * with all available class loaders in no particular order until it finds
     * one that works.
     */
    public static Class forName(String className, boolean promiscuous) {
        Class clas = null;
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader loader = getClassLoader(className);
            setContextClassLoader(loader);
            clas = Class.forName(className, true, loader);
        } catch (ClassNotFoundException e) {
            if (promiscuous) {
                clas = promiscuousForName(className);
            }
        } catch (Throwable e) {
            Log.getLogger().warning(e.getMessage());
        }
        Thread.currentThread().setContextClassLoader(oldLoader);
        return clas;
    }

    private static Class promiscuousForName(String className) {
        Class clas = null;
        Iterator i = getClassLoaders().iterator();
        while (i.hasNext() && clas == null) {
            ClassLoader loader = (ClassLoader) i.next();
            setContextClassLoader(loader);
            try {
                clas = Class.forName(className, true, loader);
            } catch (ClassNotFoundException e) {
                // do nothing
            } catch (NoClassDefFoundError error) {
                // Log.error(error.getMessage(), PluginUtilities.class,
                // "promiscuousForName", className, loader);
            }
        }
        return clas;
    }

    private static Collection getClassLoaders() {
        return new HashSet(_pluginClassNameToClassLoaderMap.values());
    }

    private static ClassLoader getClassLoader(String name) {
        ClassLoader loader = (ClassLoader) _pluginClassNameToClassLoaderMap.get(name);
        if (loader == null) {
            loader = PluginUtilities.class.getClassLoader();
        }
        return loader;
    }

    public static Collection getAvailableFactories() {
        if (_factories == null) {
            _factories = new ArrayList();
            Iterator i = getAvailableFactoryClassNames().iterator();
            while (i.hasNext()) {
                String name = (String) i.next();
                if (name.equals(defaultFactoryClassName)) {
                    _factories.add(0, SystemUtilities.newInstance(name));
                } else {
                    _factories.add(SystemUtilities.newInstance(name));
                }
            }
        }
        return _factories;
    }

    private static Collection getPluginNames(String pluginType) {
        Collection names = (Collection) pluginToNameMap.get(pluginType);
        return (names == null) ? Collections.EMPTY_LIST : Collections.unmodifiableCollection(names);
    }

    public static Collection getAvailableFactoryClassNames() {
        return getPluginNames(FACTORY_PLUGIN);
    }

    public static Collection getAvailableSlotWidgetClassNames() {
        return getPluginNames(SLOT_WIDGET);
    }

    public static Collection getAvailableTabWidgetClassNames() {
        return getPluginNames(TAB_WIDGET);
    }

    public static Collection getAvailableImportPluginClassNames() {
        return getPluginNames(IMPORT_PLUGIN);
    }

    public static Collection getAvailableCreateProjectPluginClassNames() {
        return getPluginNames(CREATE_PROJECT_PLUGIN);
    }

    public static Collection getAvailableExportProjectPluginClassNames() {
        return getPluginNames(EXPORT_PROJECT_PLUGIN);
    }

    public static Collection getAvailableExportPluginClassNames() {
        return getPluginNames(EXPORT_PLUGIN);
    }

    public static Collection getAvailableProjectPluginClassNames() {
        return getPluginNames(PROJECT_PLUGIN);
    }

    public static Collection getPluginComponentNames() {
        List list = new ArrayList(_pluginComponentNames);
        Collections.sort(list);
        return list;
    }

    public static URL getPluginComponentAboutURL(String pluginComponentName) {
        return (URL) _pluginComponentNameToAboutURLMap.get(pluginComponentName);
    }

    public static URL getPluginComponentDocURL(String pluginComponentName) {
        return (URL) _pluginComponentNameToDocURLMap.get(pluginComponentName);
    }

    public static String getDefaultWidgetClassName(Slot slot) {
        DefaultEntry entry = new DefaultEntry(slot);
        return (String) _defaultSlotWidgetNames.get(entry);
    }

    public static String getDefaultWidgetClassName(boolean cardinality, ValueType type, Cls allowedCls) {
        DefaultEntry entry = new DefaultEntry(cardinality, type, allowedCls);
        String name = (String) _defaultSlotWidgetNames.get(entry);
        if (name == null && type.equals(ValueType.INSTANCE) && allowedCls != null) {
            // now match on any allowed class
            entry = new DefaultEntry(cardinality, type, null);
            name = (String) _defaultSlotWidgetNames.get(entry);
        }
        return name;
    }

    public static String getDefaultWidgetClassName(boolean cardinality, ValueType type, Cls allowedCls, Slot slot) {
        String name = null;
        if (slot != null) {
            name = getDefaultWidgetClassName(slot);
        }
        if (name == null) {
            name = getDefaultWidgetClassName(cardinality, type, allowedCls);
        }
        return name;
    }

    public static boolean isLoadableClass(String className) {
        return forName(className) != null;
    }

    private static boolean isLoadableClass(String className, ClassLoader loader, Class interfac) {
        boolean loadable = false;
        try {
            if (loader == null) {
                loader = SystemUtilities.class.getClassLoader();
            }
            Class clas = Class.forName(className, true, loader);
            if (clas != null) {
                loadable = interfac.isAssignableFrom(clas);
                if (!loadable) {
                    Log.getLogger().warning(className + " does not implement " + interfac);
                }
            }
        } catch (Throwable e) {
            Log.getLogger().warning(e.toString());
        }
        return loadable;
    }

    private static boolean isSet(Attributes attributes, String name) {
        boolean isSet = false;
        String s = attributes.getValue(name);
        if (s != null) {
            isSet = s.equalsIgnoreCase("true");
        }
        return isSet;
    }

    private static void loadPluginsWithClassLoader(File file, ClassLoader classLoader) {
        logClassLoaderInformation(file, classLoader);
        Collection manifests = getNewManifests(classLoader);
        if (file == null) {
            Manifest extraManifest = loadExtraManifest();
            if (extraManifest != null) {
                manifests.add(extraManifest);
            }
        }
        Iterator i = manifests.iterator();
        while (i.hasNext()) {
            Manifest manifest = (Manifest) i.next();
            processManifest(manifest, classLoader);
        }
    }

    private static Collection getNewManifests(ClassLoader loader) {
        Collection manifests = new ArrayList();
        try {
            Enumeration e = loader.getResources("META-INF/MANIFEST.MF");
            while (e.hasMoreElements()) {
                URL url = (URL) e.nextElement();
                addNew(url, manifests);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return manifests;
    }

    private static void addNew(URL manifestURL, Collection newManifests) throws IOException {
        if (manifestURL != null && _manifestURLs.add(manifestURL)) {
            newManifests.add(new Manifest(manifestURL.openStream()));
        }
    }

    private static void logClassLoaderInformation(File file, ClassLoader loader) {
        if (file != null && !file.equals(pluginsDir)) {
            String line = "Loaded plugin " + file.getName() + getPluginsString(file);
            Log.getLogger().config(line);
        }
    }

    public static void processManifest(Manifest manifest, ClassLoader loader) {
        Iterator i = manifest.getEntries().keySet().iterator();
        while (i.hasNext()) {
            String attributeName = (String) i.next();
            Attributes attributes = manifest.getAttributes(attributeName);
            String className = attributeNameToClassName(attributeName);
            checkPlugins(loader, attributes, className);
        }
    }

    private static void checkPlugins(ClassLoader loader, Attributes attributes, String className) {
        checkPlugin(loader, attributes, className, TAB_WIDGET, TabWidget.class);
        checkPlugin(loader, attributes, className, CLS_WIDGET, ClsWidget.class);
        boolean added = checkPlugin(loader, attributes, className, FACTORY_PLUGIN, KnowledgeBaseFactory.class);
        if (added) {
            recordFactoryDefault(className, attributes);
        }
        checkPlugin(loader, attributes, className, IMPORT_PLUGIN, ImportPlugin.class);
        checkPlugin(loader, attributes, className, CREATE_PROJECT_PLUGIN, CreateProjectPlugin.class);
        checkPlugin(loader, attributes, className, EXPORT_PLUGIN, ExportPlugin.class);
        checkPlugin(loader, attributes, className, EXPORT_PROJECT_PLUGIN, ExportProjectPlugin.class);
        checkPlugin(loader, attributes, className, PROJECT_PLUGIN, ProjectPlugin.class);

        added = checkPlugin(loader, attributes, className, SLOT_WIDGET, SlotWidget.class);
        if (added) {
            recordSlotWidgetDefaults(className, attributes);
        }
    }

    private static Collection getOrCreatePluginNames(String pluginType) {
        Collection c = (Collection) pluginToNameMap.get(pluginType);
        if (c == null) {
            c = new ArrayList();
            pluginToNameMap.put(pluginType, c);
        }
        return c;
    }

    private static boolean checkPlugin(ClassLoader loader, Attributes attributes, String className,
            String attributeName, Class pluginClass) {
        boolean added = false;
        if (isSet(attributes, attributeName)) {
            if (isLoadableClass(className, loader, pluginClass)) {
                Collection names = getOrCreatePluginNames(attributeName);
                names.add(className);
                added = true;
                Object o = _pluginClassNameToClassLoaderMap.put(className, loader);
                if (o != null && o != loader) {
                    Log.getLogger().warning("Duplicate plugin: " + className);
                }
            }
        }
        return added;
    }

    private static void recordFactoryDefault(String className, Attributes attributes) {
        if (isSet(attributes, "Default-Factory")) {
            defaultFactoryClassName = className;
        }
    }

    private static void recordSlotWidgetDefaults(String className, Attributes attributes) {
        if (isSet(attributes, "Default-Widget")) {
            String cardinality = attributes.getValue("Default-Widget-For-Cardinality");
            String type = attributes.getValue("Default-Widget-For-Type");
            String allowed_class = attributes.getValue("Default-Widget-For-Allowed-Class");
            String slot = attributes.getValue("Default-Widget-For-Slot");
            DefaultEntry entry = new DefaultEntry(cardinality, type, allowed_class, slot);
            _defaultSlotWidgetNames.put(entry, className);
        }

    }

    public static void findPluginsDirectory() {
        File dir = ApplicationProperties.getApplicationDirectory();
        if (dir == null) {
            Log.getLogger().warning("Application directory not specified");
        } else {
            File file = new File(dir, "plugins");
            if (file.exists()) {
                pluginsDir = file;
            } else {
                Log.getLogger().warning("Plugins directory not found: " + file);
            }
        }
    }

    private static void init() {
        try {
            // setContextClassLoader(null);
            loadPlugins();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setContextClassLoader(ClassLoader loader) {
        try {
            Thread.currentThread().setContextClassLoader(loader);
        } catch (SecurityException e) {
            // fails in applets, but plugins don't work there anyway
        }
    }

    private static void loadPlugins() {
        findPluginsDirectory();
        loadSystemPlugins();

        if (pluginsDir != null) {
            loadLegacyPlugins();
            loadPluginPackages();
        }
        // loadExtraManifestPlugins(pluginsDir);
    }

    private static void loadSystemPlugins() {
        loadPlugins(null);
    }

    private static void loadPlugins(File dir) {
        createClassLoaderAndLoadPlugins(dir, PluginUtilities.class.getClassLoader());
    }

    private static void createClassLoaderAndLoadPlugins(File dir, ClassLoader parentLoader) {
        ClassLoader loader = createClassLoader(dir, parentLoader);
        _pluginPackageToClassLoaderMap.put(dir, loader);
        loadPluginsWithClassLoader(dir, loader);
    }

    private static void loadLegacyPlugins() {
        loadPlugins(pluginsDir);
    }

    private static ClassLoader getParentLoader(File packageDir) {
        ClassLoader parentLoader;
        Collection dependentLoaders = getDependentLoaders(packageDir);
        int count = dependentLoaders.size();
        switch (count) {
            case 0:
                parentLoader = PluginUtilities.class.getClassLoader();
                break;
            case 1:
                parentLoader = (ClassLoader) CollectionUtilities.getFirstItem(dependentLoaders);
                break;
            default:
                parentLoader = new MultiplexingClassLoader(dependentLoaders);
                break;
        }
        return parentLoader;
    }

    private static Collection getDependentLoaders(File packageDir) {
        Collection dependentLoaders = new ArrayList();
        Properties properties = getProperties(packageDir);
        int count = getInt(properties, DEPENDENCY_COUNT_PROPERTY);
        for (int i = 0; i < count; ++i) {
            File dependentPackage = getDependentPackage(properties, i);
            if (dependentPackage != null) {
                ClassLoader packageLoader = getClassLoaderForPackage(dependentPackage);
                if (packageLoader != null) {
                    dependentLoaders.add(packageLoader);
                }
            }
        }
        return dependentLoaders;
    }

    private static ClassLoader getClassLoaderForPackage(File file) {
        ClassLoader loader = (ClassLoader) _pluginPackageToClassLoaderMap.get(file);
        if (loader == null) {
            loadPluginPackage(file);
            loader = (ClassLoader) _pluginPackageToClassLoaderMap.get(file);
        }
        return loader;
    }

    private static File getDependentPackage(Properties properties, int i) {
        File dependentPackage = null;
        String pack = properties.getProperty(DEPENDENCY_PROPERTY + "." + i);
        if (pack != null) {
            File testPackage = new File(pluginsDir, pack);
            if (testPackage.isDirectory()) {
                dependentPackage = testPackage;
            }
        }
        return dependentPackage;
    }

    private static void loadPluginPackages() {
        File[] packages = pluginsDir.listFiles(_pluginPackageFilter);
        Collection list = orderPackages(packages);
        Iterator i = list.iterator();
        while (i.hasNext()) {
            File packageDir = (File) i.next();
            if (!isLoaded(packageDir)) {
                loadPluginPackage(packageDir);
            }
        }
    }

    private static boolean isLoaded(File packageDir) {
        return _pluginPackageToClassLoaderMap.get(packageDir) != null;
    }

    private static Collection orderPackages(File[] packages) {
        return Arrays.asList(packages);
    }

    private static void loadPluginPackage(File packageDir) {
        createClassLoaderAndLoadPlugins(packageDir, getParentLoader(packageDir));
        loadURLs(packageDir);
    }

    private static Manifest loadExtraManifest() {
        Manifest manifest = null;
        String fileName = ApplicationProperties.getExtraManifestPath();
        if (fileName != null) {
            try {
                InputStream is = new FileInputStream(fileName);
                manifest = new Manifest(is);
            } catch (IOException e) {
                Log.getLogger().warning(e.getMessage());
            }
        }
        return manifest;
    }

    private static int getInt(Properties properties, String property) {
        int count = 0;
        if (properties != null) {
            String countString = properties.getProperty(property);
            if (countString != null) {
                count = Integer.parseInt(countString);
            }
        }
        return count;
    }

    private static String getPluginName(Properties properties, int i, File dir) {
        String pluginName = PLUGIN_NAME_PROPERTY + "." + i;
        return properties.getProperty(pluginName, dir.getName());
    }

    private static void loadURL(String name, Properties properties, String property, int i, File dir, Map map) {
        String filePropertyName = property + "." + i;
        String fileString = properties.getProperty(filePropertyName);
        if (fileString != null) {
            URL url = getURL(dir, fileString);
            if (url == null) {
                Log.getLogger().warning("missing file: " + fileString);
            } else {
                map.put(name, url);
            }
        }
    }

    private static final char STANDARD_SEPARATOR_CHAR = '/';

    private static String localizePath(String path) {
        char replacement = File.separatorChar;
        if (replacement != STANDARD_SEPARATOR_CHAR) {
            path = path.replace(STANDARD_SEPARATOR_CHAR, replacement);
        }
        return path;
    }

    private static URL getURL(File dir, String path) {
        URL url = null;
        URI uri = getAbsoluteURI(path);
        if (uri == null) {
            uri = getAbsoluteURI(dir, path);
        }
        if (uri != null) {
            try {
                url = uri.toURL();
            } catch (MalformedURLException e) {
            }
        }
        return url;
    }

    private static URI getAbsoluteURI(File dir, String path) {
        URI uri = null;
        path = localizePath(path);
        File file = new File(dir, path);
        if (file.exists()) {
            uri = file.toURI();
        }
        return uri;
    }

    private static URI getAbsoluteURI(String path) {
        URI uri = null;
        try {
            uri = new URI(path);
            if (!uri.isAbsolute()) {
                uri = null;
            }
        } catch (URISyntaxException e) {
            // do nothing
        }
        return uri;
    }

    private static Properties getProperties(File dir) {
        Properties properties = null;
        File file = new File(dir, PROPERTIES_FILE_NAME);
        if (file.exists()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                properties = new Properties();
                properties.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                Log.getLogger().warning(e.toString());
            }
        }
        return properties;
    }

    private static void loadURLs(File dir) {
        Properties properties = getProperties(dir);
        if (properties != null) {
            int count = getInt(properties, PLUGIN_COUNT_PROPERTY);
            for (int i = 0; i < count; ++i) {
                String pluginName = getPluginName(properties, i, dir);
                _pluginComponentNames.add(pluginName);
                loadURL(pluginName, properties, ABOUT_PROPERTY, i, dir, _pluginComponentNameToAboutURLMap);
                loadURL(pluginName, properties, DOC_PROPERTY, i, dir, _pluginComponentNameToDocURLMap);
            }
        }
    }

    private static String getPluginsString(File dir) {
        StringBuffer buffer = new StringBuffer();
        Properties properties = getProperties(dir);
        if (properties != null) {
            int count = getInt(properties, PLUGIN_COUNT_PROPERTY);
            for (int i = 0; i < count; ++i) {
                String pluginName = getPluginName(properties, i, dir);
                buffer.append((i == 0) ? " - " : ", ");
                buffer.append(pluginName);
            }
        }
        return buffer.toString();
    }

    /**
     * Does a search of the available manifests entries for the specified
     * attribute key and does a case insensitive match on the specified
     * attribute value. If there is a match the associated java Class object is
     * loaded.
     */
    public static Collection getClassesWithAttribute(String key, String value) {
        // Log.enter(SystemUtilities.class, "getManifestClasses", key, value);
        Collection classes = getCachedClsesWithAttribute(key, value);
        if (classes == null) {
            classes = new HashSet();
            Iterator i = _manifestURLs.iterator();
            while (i.hasNext()) {
                URL url = (URL) i.next();
                try {
                    Manifest manifest = new Manifest(url.openStream());
                    classes.addAll(getManifestClasses(manifest, key, value));
                } catch (IOException e) {
                    Log.getLogger().warning(e.getMessage());
                }
            }
            saveCachedClsesWithAttribute(key, value, classes);
        }
        return new ArrayList(classes);
    }

    private static Collection getCachedClsesWithAttribute(String key, String value) {
        return (Collection) cachedClsesWithAttributeMap.get(getKey(key, value));
    }

    private static Object getKey(String key, String value) {
        return key + "=" + value;
    }

    private static void saveCachedClsesWithAttribute(String key, String value, Collection classes) {
        cachedClsesWithAttributeMap.put(getKey(key, value), classes);
    }

    private static Collection getManifestClasses(Manifest manifest, String key, String value) {
        Collection classes = new HashSet();
        Iterator i = manifest.getEntries().keySet().iterator();
        while (i.hasNext()) {
            String attributeName = (String) i.next();
            Attributes attributes = manifest.getAttributes(attributeName);
            String attributeValue = attributes.getValue(key);
            if (equalsAttributes(attributeValue, value)) {
                String className = attributeNameToClassName(attributeName);
                Class clas = forName(className, true);
                if (clas != null) {
                    classes.add(clas);
                }
            }
        }
        return classes;
    }

    private static boolean equalsAttributes(String value1, String value2) {
        return (value1 == null) ? (value2 == null) : value1.equalsIgnoreCase(value2);
    }

    public static File getPluginsDirectory() {
        return pluginsDir;
    }
}