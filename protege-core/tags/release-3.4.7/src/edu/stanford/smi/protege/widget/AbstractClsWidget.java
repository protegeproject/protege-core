package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Base class for all ClsWidgets.  For the moment all of the implementation for this is in _AbstractWidget.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

public abstract class AbstractClsWidget extends AbstractWidget implements ClsWidget {
    private static final long serialVersionUID = -3827881889241261768L;
    private Cls _cls;
    private Cls _associatedCls;
    private Instance _instance;
    private ListenerCollection _widgetListeners = new ListenerList(new WidgetEventDispatcher());

    private FrameListener _instanceListener = new FrameAdapter() {
        public void ownSlotValueChanged(FrameEvent event) {
            handleOwnSlotValueChanged(event.getSlot());
        }
        public void browserTextChanged(FrameEvent event) {
            handleBrowserTextChanged();
        }
    };

    public void dispose() {
        if (_instance != null) {
            _instance.removeFrameListener(_instanceListener);
        }
    }

    public Instance getInstance() {
        return _instance;
    }
    public void postWidgetEvent(int type) {
        _widgetListeners.postEvent(this, type);
    }
    public void addWidgetListener(WidgetListener listener) {
        _widgetListeners.add(this, listener);
    }
    public void removeWidgetListener(WidgetListener listener) {
        _widgetListeners.remove(this, listener);
    }

    public void setup(WidgetDescriptor d, boolean isDesignTime, Project p, Cls cls) {
        super.setup(d, isDesignTime, p);
        _cls = cls;
    }
    protected void handleBrowserTextChanged() {
        postWidgetEvent(WidgetEvent.LABEL_CHANGED);
    }

    protected void handleFrameNameChanged() {
        postWidgetEvent(WidgetEvent.LABEL_CHANGED);
    }

    public Cls getCls() {
        return _cls;
    }

    public Cls getAssociatedCls() {
        return _associatedCls;
    }

    public void setAssociatedCls(Cls cls) {
        _associatedCls = cls;
    }

    public void setInstance(Instance instance) {
        if (_instance != null) {
            _instance.removeFrameListener(_instanceListener);
        }
        _instance = instance;
        if (_instance != null) {
            _instance.addFrameListener(_instanceListener);
        }
    }

    protected void handleOwnSlotValueChanged(Slot slot) {
        AbstractSlotWidget widget = (AbstractSlotWidget) getSlotWidget(slot);
        if (widget != null) {
            widget.loadValues();
        }
    }
}
