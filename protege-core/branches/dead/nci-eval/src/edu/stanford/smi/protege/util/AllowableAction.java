package edu.stanford.smi.protege.util;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Base class for actions that supports two features: (1) listening to a
 * {@link Selectable}and only enabling if a selection is made, and (2) being
 * disablable manually.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AllowableAction extends StandardAction {
    private Selectable _selectable;
    private boolean _isAllowed = true;

    public AllowableAction(ResourceKey key) {
        super(key);
    }
    
    public AllowableAction(ResourceKey key, Selectable selectable) {
        super(key);
        initializeSelectable(selectable);
    }

    public AllowableAction(String name, Selectable selectable) {
        this(name, name, null, selectable);
    }

    public AllowableAction(String name, Icon icon, Selectable selectable) {
        this(name, name, icon, selectable);
    }

    public AllowableAction(String name, String description, Icon icon, Selectable selectable) {
        super(name, icon);
        putValue(Action.SHORT_DESCRIPTION, description);
        initializeSelectable(selectable);
    }

    private void initializeSelectable(Selectable selectable) {
        _selectable = selectable;
        if (_selectable != null) {
            _selectable.addSelectionListener(new SelectionListener() {
                public void selectionChanged(SelectionEvent event) {
                    onSelectionChange();
                    updateEnabledFlag();
                }
            });
            setEnabled(false);
        }
    }

    public Selectable getSelectable() {
        return _selectable;
    }

    public Collection getSelection() {
        return (_selectable == null) ? Collections.EMPTY_LIST : _selectable.getSelection();
    }

    private boolean hasSelection() {
        return _selectable != null && !getSelection().isEmpty();
    }

    public void onSelectionChange() {
        // do nothing
    }

    public void setAllowed(boolean b) {
        _isAllowed = b;
        updateEnabledFlag();
    }

    public boolean isAllowed() {
        return _isAllowed;
    }

    private void updateEnabledFlag() {
        setEnabled(_isAllowed && (_selectable == null || hasSelection()));
    }
}