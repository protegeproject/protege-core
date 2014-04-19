package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A TreeNode that will display a relationship (other than inheritance) between two classes.  The slot representing
 * the relationship is passed into the constructor. 
 * 
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsClsReferenceNode extends LazyTreeNode {
    private Slot _slot;

    public ClsClsReferenceNode(LazyTreeNode parent, Cls cls, Slot slot) {
        super(parent, cls);
        _slot = slot;
    }

    private static void addClsReferences(Cls cls, Slot slot, Collection references) {
        ValueType type = cls.getTemplateSlotValueType(slot);
        if (equals(type, ValueType.INSTANCE)) {
            references.addAll(cls.getTemplateSlotAllowedClses(slot));
        } else if (equals(type, ValueType.CLS)) {
            references.addAll(cls.getTemplateSlotAllowedParents(slot));
        }
    }

    public LazyTreeNode createNode(Object o) {
        return new ClsClsReferenceNode(this, (Cls) o, _slot);
    }

    public int getChildObjectCount() {
        return getChildObjects().size();
    }

    public Collection getChildObjects() {
        Collection childClses = new HashSet();
        Cls cls = (Cls) getUserObject();
        if (_slot == null) {
            Iterator i = cls.getTemplateSlots().iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                addClsReferences(cls, slot, childClses);
            }
        } else {
            if (cls.hasTemplateSlot(_slot)) {
                addClsReferences(cls, _slot, childClses);
            }
        }
        return childClses;
    }

    public Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }
}
