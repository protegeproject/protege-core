package edu.stanford.smi.protege.util;

import java.io.*;
import java.net.*;

import edu.stanford.smi.protege.model.*;

/**
 * A utility class for working with files.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FileUtilities {
    public final static char EXTENSION_SEPARATOR = '.';
    private static final String TEMP_EXTENSION = ".tmp";
    private static String _readEncodingOverride;
    private static String _writeEncodingOverride;

    private static final int READ_BUFFER_SIZE = 1000000;
    private static final int WRITE_BUFFER_SIZE = READ_BUFFER_SIZE;

    public static File createTempFile(File file) throws IOException {
        File tmpFile = null;
        if (file != null) {
            if (file.exists() && !file.canWrite()) {
                throw new IOException("Cannot write to " + file
                        + ".  Perhaps it is write-protected");
            }
            tmpFile = new File(file.toString() + TEMP_EXTENSION);
        }
        return tmpFile;
    }

    public static String getAbsolutePath(String filename) {
        return new File(filename).getAbsolutePath();
    }

    public static String getAbsolutePath(String filename, Project p) {
        File file;
        URI uri = p.getProjectDirectoryURI();
        if (uri == null) {
            file = new File(filename);
        } else {
            file = new File(new File(uri), filename);
        }
        return file.getAbsolutePath();
    }

    public static String getBaseName(String s) {
        return getBaseName(new File(s));
    }

    public static String getBaseName(File file) {
        String baseName = null;
        if (file != null) {
            String name = file.getName();
            int stop = name.lastIndexOf(EXTENSION_SEPARATOR);
            if (stop == -1) {
                stop = name.length();
            }
            baseName = name.substring(0, stop);
        }
        return baseName;
    }

    public static String replaceExtension(String name, String extension) {
        if (name != null) {
            int index = name.lastIndexOf(EXTENSION_SEPARATOR);
            if (index != -1) {
                name = name.substring(0, index) + extension;
            }
        }
        return name;
    }

    public static String ensureExtension(String path, String extension) {
        if (path != null && !path.endsWith(extension)) {
            path += extension;
        }
        return path;
    }

    public static BufferedReader createBufferedReader(String filename) {
        return createBufferedReader(new File(filename));
    }

    public static BufferedReader createBufferedReader(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(createReader(file), READ_BUFFER_SIZE);
        } catch (IOException e) {
            // do nothing
        }
        return reader;
    }

    public static BufferedWriter createBufferedWriter(File file, boolean append) {
        BufferedWriter writer = null;
        if (file != null) {
            try {
                writer = new BufferedWriter(createWriter(file, append), WRITE_BUFFER_SIZE);
            } catch (IOException e) {
                // do nothing
            }
        }
        return writer;
    }

    public static BufferedWriter createBufferedWriter(String filename) {
        return createBufferedWriter(new File(filename));
    }

    public static BufferedWriter createBufferedWriter(File file) {
        return createBufferedWriter(file, false);
    }

    private static Reader createReader(File file) throws IOException {
        return createInputStreamReader(new FileInputStream(file));
    }

    private static Writer createWriter(File file, boolean append) throws IOException {
        return createOutputStreamWriter(new FileOutputStream(file, append));
    }

    public static BufferedReader createBufferedReader(InputStream is) {
        BufferedReader reader = null;
        if (is != null) {
            try {
                reader = new BufferedReader(createInputStreamReader(is), READ_BUFFER_SIZE);
            } catch (IOException e) {
                // do nothing
            }
        }
        return reader;
    }

    private static OutputStreamWriter createOutputStreamWriter(OutputStream os)
            throws UnsupportedEncodingException {
        return new OutputStreamWriter(os, getWriteEncoding());
    }

    private static InputStreamReader createInputStreamReader(InputStream is)
            throws UnsupportedEncodingException {
        return new InputStreamReader(is, getReadEncoding());
    }

    public static PrintWriter createPrintWriter(File file, boolean autoFlush) {
        PrintWriter printWriter = null;
        if (file != null) {
            Writer writer = createBufferedWriter(file);
            printWriter = new PrintWriter(writer, autoFlush);
        }
        return printWriter;
    }

    public static Writer getWriter(String fileName) {
        return (fileName == null) ? null : createBufferedWriter(new File(fileName), false);
    }

    public static Reader getReader(String fileName) {
        return createBufferedReader(new File(fileName));
    }

    public static Reader getResourceReader(Class clas, String path) {
        InputStream stream = getResourceStream(clas, path);
        Reader reader;
        if (stream == null) {
            reader = null;
        } else {
            reader = new BufferedReader(new InputStreamReader(stream), READ_BUFFER_SIZE);
        }
        return reader;
    }

    public static Reader getResourceReader(Class clas, String directory, String name) {
        return getResourceReader(clas, directory + "/" + name);
    }

    public static InputStream getResourceStream(Class clas, String path) {
        Assert.assertNotNull("class", clas);
        Assert.assertNotNull("path", path);
        return clas.getResourceAsStream(path);
    }

    public static InputStream getResourceStream(Class clas, String directory, String name) {
        return getResourceStream(clas, directory + "/" + name);
    }

    public static void makeTempFilePermanent(File tmpFile) throws IOException {
        if (tmpFile != null) {
            String tmpFileName = tmpFile.toString();
            if (tmpFileName.endsWith(TEMP_EXTENSION)) {
                String fileName = tmpFileName.substring(0, tmpFileName.length()
                        - TEMP_EXTENSION.length());
                replaceFile(tmpFile, new File(fileName));
            } else {
                throw new IOException("Not a temporary file: " + tmpFile);
            }
        }
    }

    public static void replaceFile(File tmpFile, File file) throws IOException {
        if (file.exists()) {
            // The File api lets us delete files which are not writable. This
            // is bad so we test for it.
            if (!file.canWrite()) {
                throw new IOException("Cannot write to file " + file
                        + ".  It may be write-protected.");
            } else if (!file.delete()) {
                throw new IOException("Delete of existing " + file + " failed");
            }
        }
        if (!tmpFile.renameTo(file)) {
            throw new IOException("Rename of " + tmpFile + " to " + file + " failed");
        }
    }

    public static void setReadEncodingOverride(String s) {
        _readEncodingOverride = s;
    }

    public static void setWriteEncodingOverride(String s) {
        _writeEncodingOverride = s;
    }

    public static String getReadEncodingOverride() {
        return _readEncodingOverride;
    }

    public static String getReadEncoding() {
        return (_readEncodingOverride == null) ? getEncoding() : _readEncodingOverride;
    }

    public static String getWriteEncoding() {
        return (_writeEncodingOverride == null) ? getEncoding() : _writeEncodingOverride;
    }

    public static String getEncoding() {
        return SystemUtilities.getFileEncoding();
    }

    public static String replaceFileName(String path, String newName) {
        int index = path.lastIndexOf(File.separatorChar);
        return path.substring(0, index + 1) + newName;
    }

    public static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    public static URL toURL(File file) {
        URL url = null;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            // do nothing
        }
        return url;
    }

    public static String urlEncode(String s) {
        String text;
        try {
            text = URLEncoder.encode(s, getEncoding());
        } catch (UnsupportedEncodingException e) {
            text = s;
        }
        return text;
    }

    public static String urlDecode(String s) {
        String text;
        try {
            text = URLDecoder.decode(s, getEncoding());
        } catch (UnsupportedEncodingException e) {
            text = s;
        }
        return text;
    }

    public static void close(InputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            Log.exception(e, FileUtilities.class, "close", stream);
        }
    }

    public static void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            Log.exception(e, FileUtilities.class, "close", reader);
        }
    }

    public static void close(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            Log.exception(e, FileUtilities.class, "close", writer);
        }
    }

}