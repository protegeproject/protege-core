package edu.stanford.smi.protege.plugin;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractExportPlugin implements ExportProjectPlugin {
    private static transient Logger log = Log.getLogger(AbstractExportPlugin.class);
    private String name;

    protected AbstractExportPlugin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void dispose() {
    }

    //ESCA-JAVA0130 
    protected void handleErrors(Collection errors) {
        Log.handleErrors(log, Level.WARNING, errors);
    }

    public boolean canExport(Project project) {
        return true;
    }
}