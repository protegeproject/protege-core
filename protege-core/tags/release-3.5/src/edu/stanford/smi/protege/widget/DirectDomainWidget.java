package edu.stanford.smi.protege.widget;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DirectDomainWidget extends ClsListWidget {
    
    private static final long serialVersionUID = 3076537248100213883L;

    public void initialize() {
        super.initialize();
    }
    
    protected void handleAddAction() {
        Collection clses = getAllowedParents();
        Collection selectedClses = DisplayUtilities.pickClses(DirectDomainWidget.this, getKnowledgeBase(), clses);
        Set classSet = new HashSet(selectedClses);
        classSet.removeAll(getValues());
        addItems(classSet);
    }
    
    protected boolean canRemove(Collection values) {
        return true;
    }
    
    private Collection getAllowedParents() {
        Collection domain = new ArrayList();
        Slot slot = (Slot) getInstance();
        Iterator i = slot.getSuperslots().iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            Collection superslotDomain = superslot.getDirectDomain();
            domain = resolveValues(domain, superslotDomain);
        }
        if (domain.isEmpty()) {
            domain.add(getKnowledgeBase().getRootCls());
        }
        return domain;
    }
    
    private Collection resolveValues(Collection existingDomain, Collection superslotDomain) {
        return (existingDomain.isEmpty()) ? superslotDomain : existingDomain;
    }
    
    public void setValues(Collection values) {
        Slot slot = (Slot) getInstance();
        Collection domain = new ArrayList(slot.getDirectDomain());
        if (domain.isEmpty()) {
            Iterator i = slot.getSuperslots().iterator();
            while (i.hasNext()) {
                Slot superslot = (Slot) i.next();
                Collection superslotDomain = superslot.getDirectDomain();
                domain = resolveValues(domain, superslotDomain);
            }
        }
        super.setValues(domain);
    }
    
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Domain", ResourceKey.DOMAIN_SLOT_WIDGET_LABEL);
    }

}
