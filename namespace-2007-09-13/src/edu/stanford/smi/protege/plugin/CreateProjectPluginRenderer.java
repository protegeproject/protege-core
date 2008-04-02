package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.util.*;

class CreateProjectPluginRenderer extends DefaultRenderer {
    public void load(Object o) {
        CreateProjectPlugin plugin = (CreateProjectPlugin) o;
        setMainText(plugin.getName());
    }
}
