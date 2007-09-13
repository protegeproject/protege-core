package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter the works on a particular extension.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ExtensionFilter extends FileFilter {
    private List<String> _extensions;
    private String _description;

    public ExtensionFilter(Iterator<String> extensions, String description) {
        _extensions = new ArrayList<String>();
        while (extensions.hasNext()) {
            String extension = extensions.next();
            if (!extension.startsWith(".")) {
                extension = "." + extension;
            }
            _extensions.add(extension);
        }
        _description = description;
    }


    public ExtensionFilter(String extension, String description) {
        this(Collections.singleton(extension).iterator(), description);
    }


    public boolean accept(java.io.File file) {
        if (file.isDirectory()) {
            return true;
        }
        else {
            String lowerCaseName = file.getName().toLowerCase();
            Iterator<String> it = _extensions.iterator();
            while (it.hasNext()) {
                String s = it.next();
                if (lowerCaseName.endsWith(s)) {
                    return true;
                }
            }
            return false;
        }
    }


    public String getDescription() {
        String text;
        String es = "";
        Iterator<String> it = _extensions.iterator();
        while(it.hasNext()) {
            String s = (String) it.next();
            es += "*" + s;
            if(it.hasNext()) {
                es += ", ";
            }
        }
        if (_description == null) {
            text = es + " files ";
        }
        else {
            text = _description + " (" + es + ")";
        }
        return text;
    }
}
