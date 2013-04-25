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
public class ClsReferencersAction extends ReferencersAction {
    private static final long serialVersionUID = 6840978476653688771L;

    public ClsReferencersAction(Selectable selectable) {
        super(ResourceKey.CLASS_VIEW_REFERENCES, selectable);
    }
}
