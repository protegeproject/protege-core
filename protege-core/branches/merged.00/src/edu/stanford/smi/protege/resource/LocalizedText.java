package edu.stanford.smi.protege.resource;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public final class LocalizedText {

    private static BundleHelper helper;
    
    static {
        helper = new BundleHelper("protege_text", LocalizedText.class);
        if (!helper.isValid()) {
            helper = new BundleHelper("edu.stanford.smi.protege.resource.files.protege_text", LocalizedText.class);
        }
    }

    public static boolean hasText(ResourceKey key) {
        return helper.hasText(key);
    }
    public static String getText(ResourceKey key) {
        return helper.getText(key);
    }
    
    public static String getText(ResourceKey key, String replacement) {
        return helper.getText(key, replacement);
    }
    
    public static String getText(ResourceKey key, String replacement1, String replacement2) {
        return helper.getText(key, replacement1, replacement2);
    }
    
    public static int getMnemonic(ResourceKey key) {
        return helper.getChar(new ResourceKey(key.toString() + ".mnemonic"));
    }
    
    public static int getShortcut(ResourceKey key) {
        return helper.getChar(new ResourceKey(key.toString() + ".shortcut"));
    }
    
}
