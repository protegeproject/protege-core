package edu.stanford.smi.protege.plugin;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractExportPlugin implements ExportProjectPlugin {
    private String name;

    public AbstractExportPlugin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void dispose() {
    }

    protected void handleErrors(Collection errors) {
        if (!errors.isEmpty()) {
            Log.error("Errors!", this, "handleErrors", errors);
        }
    }

    public boolean canExport(Project project) {
        return true;
    }
}