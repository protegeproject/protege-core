package edu.stanford.smi.protege.ui;

import java.util.Collection;
import java.util.Comparator;

import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.LazyTreeNode;

/**
 * Tree node for modeling the superslot-subslot relationship.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotSubslotNode extends LazyTreeNode {
    private FrameListener _frameListener = new FrameAdapter() {
    	@Override
    	public void frameReplaced(FrameEvent event) {
    		Frame oldFrame = event.getFrame();
    		Frame newFrame = event.getNewFrame();
    		Slot slot = getSlot();
    		if (slot != null && slot.equals(oldFrame)) {    			
    			reload(newFrame);
    		}
    	}
    	
        @Override
		public void browserTextChanged(FrameEvent event) {
        	if (event.isReplacementEvent()) return;
            notifyNodeChanged();
        }
        @Override
		public void ownSlotValueChanged(FrameEvent event) {
        	if (event.isReplacementEvent()) return;
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
        super(parentNode, parentSlot, parentNode.isSorted());
        parentSlot.addFrameListener(_frameListener);
    }

    @Override
	protected LazyTreeNode createNode(Object o) {
        return new SlotSubslotNode(this, (Slot) o);
    }

    @Override
	protected void dispose() {
        super.dispose();
        getSlot().removeFrameListener(_frameListener);
    }

    @Override
	protected int getChildObjectCount() {
        return getSlot().getDirectSubslotCount();
    }

    @Override
	protected Collection getChildObjects() {
        return getSlot().getDirectSubslots();
    }

    @Override
	protected Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }

    protected Slot getSlot() {
        return (Slot) getUserObject();
    }

    protected void notifyNodeChanged() {
        notifyNodeChanged(this);
    }

    @Override
	public String toString() {
        return "SlotSubslotNode(" + getSlot() + ")";
    }
}
