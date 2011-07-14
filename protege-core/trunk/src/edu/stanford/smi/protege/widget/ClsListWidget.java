package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * SlotWidget for acquiring and displaying a list of Cls objects.  Note that this widget does not allow you
 * to create a new class, in part because of the need to also specify its parents.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsListWidget extends AbstractListWidget {
    private static final long serialVersionUID = -1098698166996518862L;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;

    private FrameListener _instanceListener = new FrameAdapter() {
        public void ownSlotValueChanged(FrameEvent event) {
            repaint();
        }

        public void replaceFrame(FrameEvent event) {
            repaint();
        }
    };

    protected void addButtons(LabeledComponent c, Action viewAction) {
        addButton(viewAction);
        addButton(getAddClsesAction());
        addButton(getRemoveClsesAction());
    }

    public void dispose() {
        super.dispose();
        removeListener();
    }

    protected Action getAddClsesAction() {
        _addAction = new AddAction(ResourceKey.CLASS_ADD) {
            private static final long serialVersionUID = 727636361504600489L;

            public void onAdd() {
                handleAddAction();
            }
        };
        return _addAction;
    }

    protected Action getRemoveClsesAction() {
        _removeAction = new RemoveAction(ResourceKey.CLASS_REMOVE, this) {
            private static final long serialVersionUID = 7653766881574165449L;

            public void onRemove(Collection clses) {
                handleRemoveAction(clses);
            }
        };
        return _removeAction;
    }

    protected Action getViewInstanceAction() {
        return new ViewAction(ResourceKey.CLASS_VIEW, this) {
            private static final long serialVersionUID = 6962221637991750449L;

            public void onView(Object o) {
                handleViewAction((Cls) o);
            }
        };
    }

    protected void handleAddAction() {
        Collection clses = getCls().getTemplateSlotAllowedParents(getSlot());
        addItems(DisplayUtilities.pickClses(ClsListWidget.this, getKnowledgeBase(), clses));
    }

    protected void handleRemoveAction(Collection clses) {
        removeItems(clses);
    }

    protected void handleViewAction(Cls cls) {
        showInstance(cls);
    }

    public void initialize() {
        super.initialize();
        addButtons(getLabeledComponent(), getViewInstanceAction());
        setRenderer(FrameRenderer.createInstance());
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable;
        if (cls == null || slot == null) {
            isSuitable = false;
        } else {
            ValueType type = cls.getTemplateSlotValueType(slot);
            boolean isCls = type.equals(ValueType.CLS);
            if (type.equals(ValueType.INSTANCE)) {
                Collection clses = cls.getTemplateSlotAllowedClses(slot);
                if (!clses.isEmpty()) {
                    boolean isClsMetaCls = true;
                    Iterator i = clses.iterator();
                    while (i.hasNext() && isClsMetaCls) {
                        Cls allowedCls = (Cls) i.next();
                        isClsMetaCls = allowedCls.isClsMetaCls();
                    }
                    isCls = isClsMetaCls;
                }
            }
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = isCls && isMultiple;
        }
        return isSuitable;
    }

    protected void removeListener() {
        Iterator i = getValues().iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.removeFrameListener(this._instanceListener);
        }
    }

    public void setEditable(boolean b) {
    	b = b && !isReadOnlyConfiguredWidget();
    	
        setAllowed(_addAction, b);
        setAllowed(_removeAction, b);
    }

    public void setValues(Collection values) {
        removeListener();
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.addFrameListener(this._instanceListener);
        }
        super.setValues(values);
    }
}
