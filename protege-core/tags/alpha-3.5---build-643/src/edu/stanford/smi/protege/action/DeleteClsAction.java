/*
 * Created on Jul 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.stanford.smi.protege.action;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 *
 * TODO Class Comment
 */
public class DeleteClsAction extends DeleteInstancesAction {
    private static final long serialVersionUID = 2546522958448811596L;

    public DeleteClsAction(Selectable selectable) {
    	super(ResourceKey.CLASS_DELETE, selectable);
    }

}
