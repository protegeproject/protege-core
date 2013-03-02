package edu.stanford.smi.protege.model.framestore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.DeletionHook;
import edu.stanford.smi.protege.util.Log;


public class DeletionHookFrameStore extends FrameStoreAdapter {
    private Logger log = Log.getLogger(DeletionHookFrameStore.class);
    
    private Set<DeletionHook> hooks = new HashSet<DeletionHook>();
    
    public void addHook(DeletionHook hook) {
        hooks.add(hook);
    }
    
    public void removeHook(DeletionHook hook) {
        hooks.remove(hook);
    }
    
    public Set<DeletionHook> getHooks() {
        return Collections.unmodifiableSet(hooks);
    }
    
    public void handleDelete(Frame frame) {
        for (DeletionHook hook : hooks) {
            try {
                hook.delete(frame);
            } catch (Throwable t) {
                log.log(Level.WARNING, "hook (" + hook + ") on delete operation failed", t);
            }
        }
    }
    
 
    
    /* *********************************************************************
     * FrameStore implementations.
     *     
     */

    public void deleteCls(Cls cls) {
        handleDelete(cls);
        super.deleteCls(cls);
    }

    public void deleteSlot(Slot slot) {
        handleDelete(slot);
        super.deleteSlot(slot);
    }

    public void deleteFacet(Facet facet) {
        handleDelete(facet);
        super.deleteFacet(facet);
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        handleDelete(simpleInstance);
        super.deleteSimpleInstance(simpleInstance);
    }
}
