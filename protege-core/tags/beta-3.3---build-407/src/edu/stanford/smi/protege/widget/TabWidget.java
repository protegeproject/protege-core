package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;

/**
 * Basic interface for all tab widgets.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface TabWidget extends Widget {

    /**
     * Called when the user attempts to close a project.  Return 'true' you tab is in such a state that a close is 
     * possible.  If for some reason you must prevent a close then you must pop up a dialog letting the user know why
     * the close is not allowed.  You should then return 'false'.  Most tabs will always return true.
     */
    boolean canClose();

    /**
     * Called when the user attempts to save a project.  Return 'true' you tab is in such a state that a save is 
     * possible.  If for some reason you must prevent a save then you must pop up a dialog letting the user know why
     * the save is not allowed.  You should then return 'false'.  Most tabs will always return true.
     */
    boolean canSave();

    /**
     * Called when the user attempts to close a project and all tabs return 'true' from their #canClose() methods.  
     * Do any tab specific close operations in this method.  Most tabs will do nothing.
     */
    void close();

    Icon getIcon();

    String getShortDescription();

    /**
     * Called when the user attempts to save a project and all tabs return 'true' from their #canSave() methods.  
     * Do any tab specific save operations in this method.  Most tabs will do nothing.
     */
    void save();

    void setup(WidgetDescriptor descriptor, Project project);

    void synchronizeClsTree(Collection clsPath);

    void synchronizeToInstances(Collection instances);

    Collection getSelectedInstances();
}