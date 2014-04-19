package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.util.*;

class CreateProjectPluginRenderer extends DefaultRenderer {
    private static final long serialVersionUID = 2260057867634784812L;

    public void load(Object o) {
        CreateProjectPlugin plugin = (CreateProjectPlugin) o;
        setMainText(plugin.getName());
    }
}
