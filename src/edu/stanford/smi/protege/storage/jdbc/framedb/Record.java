package edu.stanford.smi.protege.storage.jdbc.framedb;

import java.util.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A record in the FrameDB database.  This is essentially a {Frame, Slot, Facet, Collection{Value}} tuple.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public final class Record {
    private static final int THRESHOLD = 1000;
    private FrameID _frame;
    private FrameID _slot;
    private FrameID _facet;
    private boolean _isTemplate;
    private List _values;
    private int _cachedHashCode;

    public Record() {
    }

    public Record(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        load(frame, slot, facet, isTemplate);
    }

    public void addValue(int position, Object value) {
        if (_values == null) {
            _values = new ArrayList();
        }
        _values.add(position, value);
        checkSize();
    }

    public void addValue(Object value) {
        if (_values == null) {
            _values = new ArrayList();
        }
        _values.add(value);
        checkSize();
    }

    public void addValues(Collection valueToAdd) {
        if (_values == null) {
            _values = new ArrayList();
        }
        _values.addAll(valueToAdd);
        checkSize();
    }

    /*
     * The hash code cannot depend on things that can change like the object or the index
     */
    public void cacheHashCode() {
        int result = _frame.hashCode() ^ _slot.hashCode();
        if (_facet != null) {
            result ^= _facet.hashCode();
        }
        result += _isTemplate ? 0 : 1;
        _cachedHashCode = result;
    }

    private void checkSize() {
        if (_values.size() > THRESHOLD && !(_values instanceof HashList)) {
            _values = new HashList(_values);
            // Log.trace("converting to hashlist", this, "checkSize");
        }
    }

    public int countValues() {
        return (_values == null) ? 0 : _values.size();
    }

    public boolean equals(Object o) {
        Record other = (Record) o;
        return equals(_frame, other._frame)
                && equals(_slot, other._slot)
                && equals(_facet, other._facet)
                && _isTemplate == other._isTemplate;
    }
    
    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public FrameID getFacet() {
        return _facet;
    }

    public FrameID getFrame() {
        return _frame;
    }

    public boolean getIsTemplate() {
        return _isTemplate;
    }

    public FrameID getSlot() {
        return _slot;
    }

    public List getValues() {
        List result;
        if (_values == null) {
            result = CollectionUtilities.EMPTY_ARRAY_LIST;
        } else {
            result = _values;
        }
        return result;
    }

    public int hashCode() {
        return _cachedHashCode;
    }

    public void load(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        _frame = frame;
        _slot = slot;
        _facet = facet;
        _isTemplate = isTemplate;
        _values = null;

        Assert.assertNotNull("frame", frame);
        Assert.assertNotNull("slot", slot);

        cacheHashCode();
    }

    public void removeSingleValue(Object o) {
        _values.remove(o);
    }

    List removeValues() {
        List oldValues = getValues();
        _values = null;
        return oldValues;
    }

    public List setValues(Collection values) {
        List oldValues = getValues();
        _values = new ArrayList(values);
        return oldValues;
    }

    public String toString() {
        return "Record(" + _frame + ", " + _slot + ", " + _facet + ")";
    }
}
