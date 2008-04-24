package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Tree node for modeling the superslot-subslot relationship.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotSubslotNode extends LazyTreeNode {
    private FrameListener _frameListener = new FrameAdapter() {
        public void browserTextChanged(FrameEvent event) {
            notifyNodeChanged();
        }
        public void ownSlotValueChanged(FrameEvent event) {
            String slotName = event.getSlot().getName();
            if (slotName.equals(Model.Slot.DIRECT_SUBSLOTS)) {
                reload();
            } else if (slotName.equals(Model.Slot.DIRECT_TYPES)) {
                // replace the stale slot reference
                Slot slot = getSlot().getKnowledgeBase().getSlot(getSlot().getName());
                reload(slot);
            }
        }
    };

    public SlotSubslotNode(LazyTreeNode parentNode, Slot parentSlot) {
        super(parentNode, parentSlot);
        parentSlot.addFrameListener(_frameListener);
    }

    protected LazyTreeNode createNode(Object o) {
        return new SlotSubslotNode(this, (Slot) o);
    }

    protected void dispose() {
        super.dispose();
        getSlot().removeFrameListener(_frameListener);
    }

    protected int getChildObjectCount() {
        return getSlot().getDirectSubslotCount();
    }

    protected Collection getChildObjects() {
        return getSlot().getDirectSubslots();
    }

    protected Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }

    protected Slot getSlot() {
        return (Slot) getUserObject();
    }

    protected void notifyNodeChanged() {
        notifyNodeChanged(this);
    }

    public String toString() {
        return "SlotSubslotNode(" + getSlot() + ")";
    }
}
