package edu.stanford.smi.protege.server.framestore;

import java.util.Collection;
import java.util.List;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStoreAdapter;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.background.CacheRequestReason;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;


/**
 * This class informs the frame calculator of items that the client may want cached during
 * normal knowledge base operations.  By making this a frame store we allow for the possiblity
 * this component can be replaced depending on the application.
 */
public class FrameCalculatorFrameStore extends FrameStoreAdapter {
    
    /*
     * The  !frameCalculator.inFrameCalculatorThread() calls are actually unneccessary at 
     * the moment but I think that it is hacky to rely on this fact.
     */
    
    private FrameCalculator frameCalculator;
    
    public FrameCalculatorFrameStore(FrameCalculator frameCalculator) {
        this.frameCalculator = frameCalculator;
    }
    
    public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            frameCalculator.addRequest(cls, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
        }
        return getDelegate().getDirectTemplateSlotValues(cls, slot);
    }

    
    public List getDirectTemplateFacetValues(Cls cls, 
                                             Slot slot, 
                                             Facet facet) {
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            frameCalculator.addRequest(cls, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
        }
        return getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
    }
    
    
    public Frame getFrame(String name) {
        Frame frame = getDelegate().getFrame(name);
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            if (frame != null) {
                frameCalculator.addRequest(frame, session, CacheRequestReason.USER_NAME_REQUEST);
            }
        }
        return frame;
    }
    
    public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            if (!slot.getFrameID().equals(Model.SlotID.DIRECT_INSTANCES)) {
                frameCalculator.addRequest(frame, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
            }
        }
        return getDelegate().getDirectOwnSlotValuesCount(frame, slot);
    }
    
    public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        List values = getDelegate().getDirectOwnSlotValues(frame, slot);
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();

            if (!slot.getFrameID().equals(Model.SlotID.DIRECT_INSTANCES)) {
                frameCalculator.addRequest(frame, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
            }
            if (slot.getFrameID().equals(Model.SlotID.DIRECT_SUBCLASSES)) {
                for (Object o : values) {
                    if (o instanceof Frame) {
                        frameCalculator.addRequest((Frame) o, session, CacheRequestReason.SUBCLASS);
                    }
                }
            }
        }
        return  values;
    }
    
    public Facet createFacet(FrameID id, 
                             String name, 
                             Collection directTypes, 
                             boolean loadDefaults) {
        Facet facet = getDelegate().createFacet(id, name, directTypes, loadDefaults);
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            frameCalculator.addRequest(facet, session,  CacheRequestReason.NEW_FRAME);
        }
        return facet;
    }
    
    public Slot createSlot(FrameID id, String name, Collection directTypes,
                           Collection directSuperslots,
                           boolean loadDefaults) {
        Slot slot = getDelegate().createSlot(id, name, directTypes, directSuperslots, loadDefaults);
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            frameCalculator.addRequest(slot,  session, CacheRequestReason.NEW_FRAME);
        }
        return slot;
    }
    
    public Cls createCls(FrameID id, 
                         String name, 
                         Collection directTypes, 
                         Collection directSuperclasses,
                         boolean loadDefaults) {
        Cls cls = getDelegate().createCls(id, name, directTypes, directSuperclasses, loadDefaults);
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            frameCalculator.addRequest(cls,  session, CacheRequestReason.NEW_FRAME);
        }
        return cls;
    }
    
    public SimpleInstance createSimpleInstance(FrameID id, 
                                               String name, 
                                               Collection directTypes,
                                               boolean loadDefaults) {
        SimpleInstance si = getDelegate().createSimpleInstance(id, name, directTypes, loadDefaults);
        if (!frameCalculator.inFrameCalculatorThread()) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            frameCalculator.addRequest(si,  session, CacheRequestReason.NEW_FRAME);
        }
        return si;
    }
}
