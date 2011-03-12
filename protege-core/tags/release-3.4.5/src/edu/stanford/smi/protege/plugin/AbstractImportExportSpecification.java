package edu.stanford.smi.protege.plugin;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractImportExportSpecification {
    private String comment;
    private Plugin plugin;

    protected AbstractImportExportSpecification(Plugin plugin, String comment) {
        this.plugin = plugin;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    protected Plugin getPlugin() {
        return plugin;
    }
}
