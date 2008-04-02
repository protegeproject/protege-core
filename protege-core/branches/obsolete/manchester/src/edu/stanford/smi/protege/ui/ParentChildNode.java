package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Tree node that contains the superclass-subclass relations.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ParentChildNode extends LazyTreeNode {
    private ClsListener _clsListener = new ClsAdapter() {
        public void directSubclassAdded(ClsEvent event) {
            if (event.getSubclass().isVisible()) {
                childAdded(event.getSubclass());
            }
        }

        public void directSubclassRemoved(ClsEvent event) {
            if (event.getSubclass().isVisible()) {
                childRemoved(event.getSubclass());
            }
        }

        public void directSubclassMoved(ClsEvent event) {
            Cls subclass = event.getSubclass();
            int index = (new ArrayList(getChildObjects())).indexOf(subclass);
            if (index != -1) {
                childRemoved(subclass);
                childAdded(subclass, index);
            }
        }

        public void directInstanceAdded(ClsEvent event) {
            notifyNodeChanged();
        }

        public void directInstanceRemoved(ClsEvent event) {
            notifyNodeChanged();
        }

        public void templateFacetValueChanged(ClsEvent event) {
            notifyNodeChanged();
        }

        public void directSuperclassAdded(ClsEvent event) {
            notifyNodeChanged();
        }
    };

    private FrameListener _frameListener = new FrameAdapter() {
        public void browserTextChanged(FrameEvent event) {
            notifyNodeChanged();
        }

        public void ownSlotValueChanged(FrameEvent event) {
            if (event.getSlot().getName().equals(Model.Slot.DIRECT_TYPES)) {
                // refresh the stale cls reference
                Cls cls = getCls().getKnowledgeBase().getCls(getCls().getName());
                reload(cls);
            } else {
                notifyNodeChanged();
            }
        }

        public void visibilityChanged(FrameEvent event) {
            notifyNodeChanged();
        }
    };

    public ParentChildNode(LazyTreeNode parentNode, Cls parentCls) {
        super(parentNode, parentCls);
        parentCls.addClsListener(_clsListener);
        parentCls.addFrameListener(_frameListener);
    }

    protected LazyTreeNode createNode(Object o) {
        return new ParentChildNode(this, (Cls) o);
    }

    protected void dispose() {
        super.dispose();
        getCls().removeClsListener(_clsListener);
        getCls().removeFrameListener(_frameListener);
    }

    protected int getChildObjectCount() {
        return (showHidden()) ? getCls().getDirectSubclassCount() : getCls().getVisibleDirectSubclassCount();
    }

    protected Collection getChildObjects() {
        return (showHidden()) ? getCls().getDirectSubclasses() : getCls().getVisibleDirectSubclasses();
    }

    protected Cls getCls() {
        return (Cls) getUserObject();
    }

    protected Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }

    protected void notifyNodeChanged() {
        notifyNodeChanged(this);
    }

    private boolean showHidden() {
        return getCls().getProject().getDisplayHiddenClasses();
    }

    public String toString() {
        return "ParentChildNode(" + getCls() + ")";
    }
}
