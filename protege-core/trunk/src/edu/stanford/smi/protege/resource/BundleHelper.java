package edu.stanford.smi.protege.resource;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class BundleHelper {
    private ResourceBundle resourceBundle;
    private static boolean colorLookup;

    static {
        try {
            colorLookup = Boolean.getBoolean("protege.text.colorlookup");
        } catch (SecurityException e) {
            // happens in applets
        }
    }

    public BundleHelper(String bundleName, Class clas) {
        try {
            ClassLoader loader = clas.getClassLoader();
            loader = fixLoader(loader);
            Locale locale = Locale.getDefault();
            resourceBundle = ResourceBundle.getBundle(bundleName, locale, loader);
        } catch (MissingResourceException e) {
            Log.getLogger().warning("missing bundle: " + bundleName);
        }
    }

    /*
     * This is weird but apparently necessary. On MS Windows the "getResource"
     * method on the default ClassLoader searches the jar directory. On Unix and
     * Mac is does not. I'm not sure why it is only searched on Windows but this
     * is certainly desirable behavior.
     * 
     * We want the directory searched because we want to load the user's bundles
     * from there. For plugins this is handled by the custom subclass of
     * URLClassLoader that searches the plugin directory. We cannot replace the
     * system class loader but we can make up a fake class loader that searches
     * where we want to. This classloader isn't actually used for anything other
     * than loading the resource bundle. This seems really strange but does
     * appear to work.
     */
    private static ClassLoader fixLoader(ClassLoader loader) {
        if (loader == BundleHelper.class.getClassLoader()) {
            File applicationDirectory = ApplicationProperties.getApplicationDirectory();
            if (applicationDirectory != null) {
                loader = new DirectoryClassLoader(applicationDirectory, loader);
            }
        }
        return loader;
    }

    public boolean hasText(ResourceKey key) {
        return internalGetText(key) != null;
    }

    public String getText(ResourceKey key) {
        String text = internalGetText(key);
        if (colorLookup) {
            if (text == null) {
                text = "<html><font color=ff0000>" + key + "</font></html>";
            } else {
                text = "<html><font color=0000ff>" + text + "</font></html>";
            }
        }
        return text == null ? key.toString() : text;
    }

    private String internalGetText(ResourceKey key) {
        String text;
        if (resourceBundle == null) {
            Log.getLogger().warning("no resource bundle: " + key);
            text = "Missing resource bundle for " + key;
        } else {
            try {
                text = resourceBundle.getString(key.toString());
            } catch (MissingResourceException e) {
                text = null;
            }
        }
        return text;
    }

    public String getText(ResourceKey text, String macroReplacement) {
        String s = getText(text);
        s = StringUtilities.replace(s, "{0}", macroReplacement);
        return s;
    }

    public String getText(ResourceKey text, String macroReplacement1, String macroReplacement2) {
        String s = getText(text, macroReplacement1);
        s = StringUtilities.replace(s, "{1}", macroReplacement2);
        return s;
    }

    public int getChar(ResourceKey key) {
        String s = internalGetText(key);
        return (s == null || s.length() == 0) ? 0 : s.charAt(0);
    }

    public boolean isValid() {
        return resourceBundle != null;
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }

}