package edu.stanford.smi.protege.util;

import java.io.*;
import java.net.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class URIUtilities {

    private static boolean isFile(URI uri) {
        return "file".equals(uri.getScheme()) || !uri.isAbsolute();
    }

    public static String getDisplayText(URI uri) {
        String text = null;
        if (uri != null) {
            if (isFile(uri)) {
                File file = new File(uri.toString());
                text = file.getPath();
            } else {
                text = uri.toString();
            }
        }
        return text;
    }

    /**
     * Attempts to improve on the built-in resolution by resolving non-hierarchical URI's (such as jar uri's)
     * We also handle passing a name that can be a file (an thus possibly not a valid URI).
     * 
     * @param projectURI
     * @param name
     * @return the resolved uri
     */
    public static URI resolve(URI projectURI, String name) {
        URI uri = null;
        if (projectURI == null) {
            uri = createURI(name);
        } else {
            String uriText = replaceURIName(projectURI.toString(), name);
            uri = createURI(uriText);
        }
        return uri;
    }

    private static String replaceURIName(String path, String newName) {
        String newPath = null;
        int index = path.lastIndexOf('/');
        if (index != -1) {
            newPath = path.substring(0, index + 1) + newName;
        }
        return newPath;
    }

    public static String getName(URI uri) {
        String name = null;
        if (uri != null) {
            String path = uri.getPath();
            if (path != null) {
                int index = path.lastIndexOf('/');
                name = path.substring(index + 1);
            }
        }
        return name;
    }

    public static String getBaseName(URI uri) {
        String baseName = getName(uri);
        if (baseName != null) {
            int index = baseName.lastIndexOf(FileUtilities.EXTENSION_SEPARATOR);
            if (index != -1) {
                baseName = baseName.substring(0, index);
            }
        }
        return baseName;
    }

    public static URI getParentURI(URI uri) {
        URI parent = null;
        if (uri != null) {
            String text = uri.toString();
            int index = text.lastIndexOf('/');
            if (index != -1) {
                text = text.substring(0, index);
                parent = URI.create(text);
            }
        }
        return parent;
    }

    public static String getExtension(URI uri) {
        String extension = null;
        String name = getName(uri);
        if (name != null) {
            int index = name.lastIndexOf(FileUtilities.EXTENSION_SEPARATOR);
            if (index != -1) {
                extension = name.substring(index + 1);
            }
        }
        return extension;
    }

    // ???
    public static URL toURL(String text, URI baseURI) {
        URL url = null;
        if (text != null) {
            URI uri = null;
            try {
                uri = new URI(text);
            } catch (URISyntaxException e) {
                // do nothing
            }
            if (uri == null || !uri.isAbsolute()) {
                File file = new File(text);
                if (file.isAbsolute()) {
                    uri = file.toURI();
                } else {
                    uri = URIUtilities.resolve(baseURI, text);
                }
            }
            if (uri != null && uri.isAbsolute()) {
                try {
                    url = uri.toURL();
                } catch (MalformedURLException e) {
                    // do nothing
                }
            }
        }
        return url;
    }

    /*
     * This is a start at a reasonable relativization algorithm.  The built in one doesn't even remove files.
     */
    public static URI relativize(URI baseURI, URI uriToRelativize) {
        URI relativizedURI;
        if (baseURI == null) {
            relativizedURI = uriToRelativize;
        } else {
            try {
                File file = new File(baseURI);
                if (file.isFile() || file.getName().indexOf('.') != -1) {
                    File parent = file.getParentFile();
                    baseURI = parent.toURI();
                }
            } catch (IllegalArgumentException e) {
                // do nothing, not a file uri
            }
            relativizedURI = baseURI.relativize(uriToRelativize);
        }
        return relativizedURI;
    }

    public static URI replaceExtension(URI uri, String extension) {
        URI modifiedURI = null;
        if (uri != null) {
            String uriString = uri.toString();
            int index = uriString.lastIndexOf(FileUtilities.EXTENSION_SEPARATOR);
            if (index != -1) {
                String modifiedURIString = uriString.substring(0, index) + extension;
                modifiedURI = URIUtilities.createURI(modifiedURIString);
            }
        }
        return modifiedURI;
    }

    public static URI ensureExtension(URI uri, String string) {
        URI goodURI = null;
        if (uri.toString().endsWith(string)) {
            goodURI = uri;
        } else {
            try {
                goodURI = new URI(uri.toString() + string);
            } catch (URISyntaxException e) {
                Log.getLogger().warning(e.toString());
            }
        }
        return goodURI;
    }

    public static Writer createBufferedWriter(URI uri, boolean append) {
        return FileUtilities.createBufferedWriter(new File(uri), append);
    }

    public static BufferedReader createBufferedReader(URI uri) {
        BufferedReader reader = null;
        if (uri != null) {
            try {
                File file = new File(uri);
                reader = FileUtilities.createBufferedReader(file);
            } catch (Exception e) {
                try {
                    URL url = uri.toURL();
                    InputStream inputStream = url.openStream();
                    reader = FileUtilities.createBufferedReader(inputStream);
                } catch (MalformedURLException urlException) {
                    Log.getLogger().warning(urlException.toString());
                } catch (IOException ioException) {
                    Log.getLogger().warning(ioException.toString());
                }
            }
        }
        return reader;
    }

    public static URI createURI(String s) {
        URI uri = null;
        if (s != null) {
            s = s.trim();
            if (s.length() != 0) {
                try {
                    if (hasSchemePart(s)) {
                        uri = new URI(s);
                    } else {
                        uri = createURIFromFileString(s);
                    }
                } catch (URISyntaxException e) {
                    uri = createURIFromFileString(s);
                }
            }
        }
        return uri;
    }

    private static boolean hasSchemePart(String s) {
        int index = s.indexOf(':');
        return index > 1;
    }

    private static URI createURIFromFileString(String s) {
        URI uri = null;
        try {
            uri = new File(s).toURI();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        return uri;
    }
}
