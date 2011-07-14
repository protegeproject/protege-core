package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FactoryRenderer extends DefaultRenderer {
    private static final long serialVersionUID = 6960422841371553844L;

    public void load(Object o) {
        KnowledgeBaseFactory f = (KnowledgeBaseFactory) o;
        setMainText(f.getDescription());
    }
}

