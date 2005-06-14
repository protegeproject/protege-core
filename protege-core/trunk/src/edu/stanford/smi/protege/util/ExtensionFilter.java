package edu.stanford.smi.protege.util;

import javax.swing.filechooser.*;

/**
 * A file filter the works on a particular extension.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ExtensionFilter extends FileFilter {
    private String _extension;
    private String _description;

    public ExtensionFilter(String extension, String description) {
        if (extension.startsWith(".")) {
            _extension = extension;
        } else {
            _extension = "." + extension;
        }
        _description = description;
    }

    public boolean accept(java.io.File file) {
        return file.isDirectory() || file.getName().toLowerCase().endsWith(_extension);
    }

    public String getDescription() {
        String text;
        if (_description == null) {
            text = "*" + _extension + " files ";
        } else {
            text = _description + " (*" + _extension + ")";
        }
        return text;
    }
}
