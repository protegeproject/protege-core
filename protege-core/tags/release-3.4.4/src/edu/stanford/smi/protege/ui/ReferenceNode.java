package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Tree node that models a "reference" between two frames.  The reference is either through a single slot or though all
 * slots.
 */

public class ReferenceNode extends LazyTreeNode {
    private Slot _slot;

    private ClsListener _clsListener = new ClsAdapter() {
        public void templateFacetValueChanged(ClsEvent event) {
            reload();
        }
    };
    private FrameListener _frameListener = new FrameAdapter() {
        public void replaceFrame(FrameEvent event) {
            notifyNodeChanged();
        }

        public void ownSlotValueChanged(FrameEvent event) {
            reload();
        }

        public void visibilityChanged(FrameEvent event) {
            notifyNodeChanged();
        }
    };

    public ReferenceNode(LazyTreeNode parent, Frame frame, Slot slot) {
        super(parent, frame);
        _slot = slot;
        frame.addFrameListener(_frameListener);
        if (frame instanceof Cls) {
            ((Cls) frame).addClsListener(_clsListener);
        }
    }

    private static void addOwnSlotReferences(Frame frame, Slot slot, Collection references) {
        if (frame.hasOwnSlot(slot)) {
            ValueType type = frame.getOwnSlotValueType(slot);
            if (type.equals(ValueType.INSTANCE) || type.equals(ValueType.CLS)) {
                references.addAll(frame.getOwnSlotValues(slot));
            }
        }
    }

    private static void addReferences(Frame frame, Slot slot, Collection references) {
        if (frame instanceof Cls) {
            Cls cls = (Cls) frame;
            addTemplateSlotReferences(cls, slot, references);
        }
        addOwnSlotReferences(frame, slot, references);
    }

    private static void addTemplateSlotReferences(Cls cls, Slot slot, Collection references) {
        if (cls.hasTemplateSlot(slot)) {
            ValueType type = cls.getTemplateSlotValueType(slot);
            if (type.equals(ValueType.INSTANCE)) {
                references.addAll(cls.getTemplateSlotAllowedClses(slot));
            } else if (type.equals(ValueType.CLS)) {
                references.addAll(cls.getTemplateSlotAllowedParents(slot));
            }
        }
    }

    public LazyTreeNode createNode(Object o) {
        return new ReferenceNode(this, (Frame) o, _slot);
    }

    public int getChildObjectCount() {
        return getChildObjects().size();
    }

    public Collection getChildObjects() {
        Collection references = new ArrayList();
        Frame frame = (Frame) getUserObject();
        if (_slot == null) {
            if (frame instanceof Cls) {
                Cls cls = (Cls) frame;
                Iterator i = cls.getTemplateSlots().iterator();
                while (i.hasNext()) {
                    Slot slot = (Slot) i.next();
                    addTemplateSlotReferences(cls, slot, references);
                }
            }
            Iterator j = frame.getOwnSlots().iterator();
            while (j.hasNext()) {
                Slot slot = (Slot) j.next();
                if (!slot.isSystem()) {
                    addOwnSlotReferences(frame, slot, references);
                }
            }
        } else {
            addReferences(frame, _slot, references);
        }
        return references;
    }

    public Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }

    protected void notifyNodeChanged() {
        notifyNodeChanged(this);
    }
}
