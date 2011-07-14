package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Abstract class that implements many of the common "widget" methods.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractWidget extends JPanel {
    private static final long serialVersionUID = -1105419878466335322L;
    private WidgetDescriptor _descriptor;
    private boolean _isDesignTime;
    private Project _project;
    private ListenerCollection _selectionListeners = new ListenerList(new SelectionEventDispatcher());

    public AbstractWidget() {
        setLayout(new BorderLayout());
        setName(StringUtilities.getClassName(this));
    }

    public void dispose() {
        // do nothing
    }

    public void setup(WidgetDescriptor descriptor, boolean isDesignTime, Project project) {
        _descriptor = descriptor;
        _isDesignTime = isDesignTime;
        _project = project;
    }

    public WidgetDescriptor getDescriptor() {
        return _descriptor;
    }

    public KnowledgeBase getKnowledgeBase() {
        return _project.getKnowledgeBase();
    }

    public Project getProject() {
        return _project;
    }

    public PropertyList getPropertyList() {
        return _descriptor.getPropertyList();
    }

    public void setPropertyList(PropertyList list) {
        Assert.assertTrue("design time", isDesignTime());
        _descriptor.setPropertyList(list);
    }

    public String getStringProperty(String name, String defaultString) {
        String property = getPropertyList().getString(name);
        if (property == null) {
            property = defaultString;
        }
        return property;
    }

    public boolean isDesignTime() {
        return _isDesignTime;
    }

    public boolean isRuntime() {
        return !_isDesignTime;
    }

    public void show(Cls cls, Slot slot) {
        getProject().show(cls, slot);
    }

    public void showInstance(Instance instance) {
        getProject().show(instance);
    }

    public String getLabel() {
        return _descriptor.getLabel();
    }

    public void setLabel(String label) {
        _descriptor.setLabel(label);
    }

    public void addSelectionListener(SelectionListener listener) {
        _selectionListeners.add(this, listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        _selectionListeners.remove(this, listener);
    }

    public void notifySelectionListeners() {
        _selectionListeners.postEvent(this, SelectionEvent.SELECTION_CHANGED);
    }

    public void clearSelection() {
        // do nothing
    }

    public Collection getSelection() {
        // do nothing
        return Collections.EMPTY_LIST;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    protected void beginTransaction(String name) {
        getKnowledgeBase().beginTransaction(name);
    }

    protected void beginTransaction(String name, String appliedToFrameName) {
        getKnowledgeBase().beginTransaction(name, appliedToFrameName);
    }

    /**
     * @deprecated Use commitTranscation() or rollbackTransaction()
     */
    protected void endTransaction() {
        getKnowledgeBase().commitTransaction();
    }

    protected void commitTransaction() {
        getKnowledgeBase().commitTransaction();
    }

    protected void rollbackTransaction() {
        getKnowledgeBase().rollbackTransaction();
    }

    public void paint(Graphics g) {
        ComponentUtilities.enableTextAntialiasing(g);
        super.paint(g);
    }
}
