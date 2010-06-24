package edu.stanford.smi.protege.util;

import java.util.Set;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.DeletionHookFrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;

public class DeletionHookUtil {
    public static void addDeletionHook(KnowledgeBase kb, DeletionHook hook) {
        DeletionHookFrameStore frameStore = getFrameStore(kb, true);
        frameStore.addHook(hook);
    }
    
    public static void removeDeletionHook(KnowledgeBase kb, DeletionHook hook) {
        DeletionHookFrameStore frameStore = getFrameStore(kb, false);
        if (frameStore != null) {
            frameStore.removeHook(hook);
        }
    }
    
    public static Set<DeletionHook> getHooks(KnowledgeBase kb) {
        DeletionHookFrameStore frameStore = getFrameStore(kb, false);
        if (frameStore == null) {
            return null;
        }
        else {
            return frameStore.getHooks();
        }
    }
    
    private static DeletionHookFrameStore getFrameStore(KnowledgeBase kb, boolean create) {
        FrameStoreManager fsm = kb.getFrameStoreManager();
        DeletionHookFrameStore frameStore = (DeletionHookFrameStore) fsm.getFrameStoreFromClass(DeletionHookFrameStore.class);
        if (frameStore == null) {
            int pos = kb.getProject().isMultiUserServer() ? 1 : 0; // needs to go under the LocalizeFrameStoreHandler on a server
            frameStore = new DeletionHookFrameStore();
            fsm.insertFrameStore(frameStore, pos);
        }
        return frameStore;
    }
}
