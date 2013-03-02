package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * A list model where elements are stored in a {@link java.util.List}.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SimpleListModel extends AbstractListModel {
    private static final long serialVersionUID = 4610768415608528873L;
    private List _list;

    public SimpleListModel() {
        _list = makeList();
    }

    public SimpleListModel(Collection values) {
        _list = makeList();
        _list.addAll(values);
    }
    
    protected List makeList() {
        return new ArrayList();
    }
    
    protected List getList() {
        return _list;
    }

    public int addValue(Object o) {
        int index = _list.size();
        _list.add(index, o);
        fireIntervalAdded(this, index, index);
        return index;
    }

    public void addValue(Object o, int index) {
        _list.add(index, o);
        fireIntervalAdded(this, index, index);
    }

    public int addValues(Collection values) {
        int startIndex = _list.size();
        int endIndex = startIndex + values.size();
        _list.addAll(values);
        fireIntervalAdded(this, startIndex, endIndex);
        return startIndex;
    }

    public void clear() {
        if (!_list.isEmpty()) {
            int index = _list.size() - 1;
            _list.clear();
            fireIntervalRemoved(this, 0, index);
        }
    }

    public boolean contains(Object o) {
        return _list.contains(o);
    }

    public Object getElementAt(int i) {
        return (i >= 0 && i < _list.size()) ? _list.get(i) : null;
    }

    public int getSize() {
        return _list.size();
    }

    public List getValues() {
        return Collections.unmodifiableList(_list);
    }

    public int indexOf(Object o) {
        return _list.indexOf(o);
    }

    public void moveValue(int start, int end) {
        if (start != end) {
            Object o = _list.remove(start);
            if (end > start) {
                --end;
            }
            _list.add(end, o);
            fireContentsChanged(this, start, end);
        }
    }

    public int removeValue(Object o) {
        int index = _list.indexOf(o);
        if (index == -1) {
            // Log.stack("not in list", this, "removeValue", o);
        } else {
            _list.remove(index);
            fireIntervalRemoved(this, index, index);
        }
        return index;
    }

    /*
     * public int removeValues(Collection values) { int startIndex = -1;
     * Iterator i = values.iterator(); while (i.hasNext()) { Object o =
     * i.next(); if (startIndex == -1) { startIndex = indexOf(o); }
     * removeValue(o); }
     * 
     * return startIndex; }
     */

    public int removeValues(Collection values) {
        int startIndex = -1;
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (startIndex == -1) {
                startIndex = indexOf(o);
            }
            _list.remove(o);
        }
        // this isn't correct but it is "better than correct".
        fireIntervalRemoved(this, startIndex, startIndex);

        return startIndex;
    }

    public void setValue(int index, Object o) {
        _list.set(index, o);
        fireContentsChanged(this, index, index);
    }

    public void setValues(Collection values) {
        clear();
        if (!values.isEmpty()) {
            _list = new ArrayList(values);
            fireIntervalAdded(this, 0, values.size() - 1);
        }
    }
    
    public List toList() {
        return Collections.unmodifiableList(_list);
    }

    public String toString() {
        return "SimpleListModel";
    }
}