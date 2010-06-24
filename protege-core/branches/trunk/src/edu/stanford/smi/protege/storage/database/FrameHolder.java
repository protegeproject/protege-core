package edu.stanford.smi.protege.storage.database;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameHolder {
    private Map _frameIdToFrameMap = new HashMap();
    private FrameFactory _frameFactory;

    public FrameHolder(FrameFactory factory) {
        _frameFactory = factory;
    }

    public Frame getFrame(FrameID id, Collection directTypes) {
        Frame frame = (Frame) _frameIdToFrameMap.get(id);
        if (frame == null) {
            frame = createFrame(id, directTypes);
            _frameIdToFrameMap.put(id, frame);
        }
        return frame;
    }

    private Frame createFrame(FrameID id, Collection directTypes) {
        Frame frame;
        Cls firstType = (Cls) CollectionUtilities.getFirstItem(directTypes);
        if (firstType == null || !firstType.isMetaCls()) {
            frame = _frameFactory.createSimpleInstance(id, directTypes);
        } else if (firstType.isClsMetaCls()) {
            frame = _frameFactory.createCls(id, directTypes);
        } else if (firstType.isSlotMetaCls()) {
            frame = _frameFactory.createSlot(id, directTypes);
        } else if (firstType.isFacetMetaCls()) {
            frame = _frameFactory.createFacet(id, directTypes);
        } else {
            throw new IllegalStateException("No metaclass found");
        }
        return frame;
    }
}
