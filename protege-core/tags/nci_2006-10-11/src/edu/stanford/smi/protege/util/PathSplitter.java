package edu.stanford.smi.protege.util;


import java.io.*;

/**
 *  Description of the class
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PathSplitter {
    private String _path;

    public PathSplitter(String path) {
        _path = path;
    }

    public String getAbsoluteDirectory() {
        return new File(new File(_path).getAbsolutePath()).getParent();
    }

    public String getExtension() {
        String extension = null;
        String fullName = getFullName();
        int index = fullName.lastIndexOf(".");
        if (index != -1) {
            extension = fullName.substring(index);
        }
        return extension;
    }

    public String getFullName() {
        return new File(_path).getName();
    }

    public String getRelativeDirectory() {
        return new File(_path).getParent();
    }

    public String getSimpleName() {
        String simpleName;
        String fullName = getFullName();
        int index = fullName.lastIndexOf(".");
        if (index == -1) {
            simpleName = fullName;
        } else {
            simpleName = fullName.substring(0, index);
        }
        return simpleName;
    }
}
