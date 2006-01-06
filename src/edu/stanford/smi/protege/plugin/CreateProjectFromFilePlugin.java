package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.model.Project;

import java.io.File;
import java.util.Collection;

/**
 * A plugin that can be used to open files with a certain extension directly.
 * Plugins of this type are used to handle command line arguments into Protege
 * (via the main method), and in the open project dialogs.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public interface CreateProjectFromFilePlugin {


    Project createProject(File file, Collection errors);


    String getDescription();


    String[] getSuffixes();
}
