package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Tree root for the superslot-subslot relationship tree nodes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotSubslotRoot extends LazyTreeRoot {
    private KnowledgeBase _knowledgeBase;

    private KnowledgeBaseListener _listener = new KnowledgeBaseAdapter() {
        public void slotCreated(KnowledgeBaseEvent event) {
        	if (event.isReplacementEvent()) return;
            super.slotCreated(event);
            Slot slot = event.getSlot();
            if (slot.getDirectSuperslots().isEmpty()) {
                List slots = (List) getUserObject();
                int index = 0;
                slots.add(index, slot);
                childAdded(slot, index);
                // This probably shouldn't be needed but is.  If the tree is empty and you add a node it doesn't
                // get displayed if you don't send the structure changed event.
                if (getChildCount() == 1) {
                    notifyNodeStructureChanged(SlotSubslotRoot.this);
                }
            }
        }

        public void slotDeleted(KnowledgeBaseEvent event) {
        	if (event.isReplacementEvent()) return;
            super.slotDeleted(event);
            Slot slot = event.getSlot();
            List slots = (List) getUserObject();
            boolean changed = slots.remove(slot);
            if (changed) {
                childRemoved(slot);
            }
        }
    };

    private SlotListener _slotListener = new SlotAdapter() {
        public void directSuperslotAdded(SlotEvent event) {
        	if (event.isReplacementEvent()) return;
            Slot slot = event.getSlot();
            if (slot.getDirectSuperslotCount() == 1) {
                removeChild(slot);
            }
        }

        public void directSuperslotRemoved(SlotEvent event) {
        	if (event.isReplacementEvent()) return;
            Slot slot = event.getSlot();
            if (slot.getDirectSuperslotCount() == 0) {
                addChild(slot);
            }
        }
    };

    private void removeChild(Slot slot) {
        List slots = (List) getUserObject();
        slots.remove(slot);
        childRemoved(slot);
    }

    private void addChild(Slot slot) {
        List slots = (List) getUserObject();
        slots.add(slot);
        childAdded(slot);
    }

    public SlotSubslotRoot(KnowledgeBase kb) {
    	this(kb, getSlots(kb));
    }
    
    public SlotSubslotRoot(KnowledgeBase kb, Collection slots) {
    	this(kb, slots, ApplicationProperties.getSortSlotTreeOption());
    }

    public SlotSubslotRoot(KnowledgeBase kb, Collection slots, boolean isSorted) {
        super(slots);
        kb.addKnowledgeBaseListener(_listener);
        kb.addSlotListener(_slotListener);
        _knowledgeBase = kb;
    }

    public LazyTreeNode createNode(Object o) {
        return new SlotSubslotNode(this, (Slot) o);
    }

    public void dispose() {
        super.dispose();
        _knowledgeBase.removeKnowledgeBaseListener(_listener);
        _knowledgeBase.removeSlotListener(_slotListener);
    }

    public Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }

    private static Collection getSlots(KnowledgeBase kb) {
        List results = new ArrayList(kb.getSlots());
        Iterator i = results.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (slot.getDirectSuperslotCount() > 0) {
                i.remove();
            }
        }
        
        if (ApplicationProperties.getSortSlotTreeOption()) {
        	Collections.sort(results, new FrameComparator());
        }
        return results;
    }
}
