package edu.stanford.smi.protege.action;

import java.awt.event.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Description of the class
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ReferencersAction extends AllowableAction {

    private static final long serialVersionUID = 3044159910986113793L;

    public ReferencersAction(Selectable s) {
        super(ResourceKey.VALUE_VIEW_REFERENCES, s);
    }
    
    public ReferencersAction(ResourceKey key, Selectable s) {
        super(key, s);
    }
    
    public void actionPerformed(ActionEvent event) {
        Collection selection = getSelection();
        if (isAllowed() && selection.size() == 1) {

            Instance instance = (Instance) CollectionUtilities.getFirstItem(selection);
            ReferencersPanel panel = new ReferencersPanel(instance);
            ComponentFactory.showInFrame(panel, "References to " + instance.getBrowserText());
        }
    }
}
