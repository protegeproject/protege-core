package edu.stanford.smi.protege.model.framestore.cleandispatch;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
abstract class AbstractRedirectingDispatch implements Dispatch {
    public List getDirectOwnSlotValues(FrameStore delegate, Frame frame, Slot slot) {
        List values;
        if (isCorrectFrameType(frame)) {
            values = getCurrentValues(delegate, frame);
        } else {
            Log.warning("Invalid frame type", this, "getDirectOwnSlotValues", frame, slot);
            values = delegate.getDirectOwnSlotValues(frame, slot);
        }
        return values;
    }

    public void setDirectOwnSlotValues(FrameStore delegate, Frame frame, Slot slot, Collection newValues) {
        if (isCorrectFrameType(frame)) {
            Collection currentValues = new LinkedHashSet(getCurrentValues(delegate, frame));
            addValues(delegate, frame, currentValues, newValues);
            removeValues(delegate, frame, currentValues, newValues);
        } else {
            Log.warning("Invalid frame type", this, "setDirectOwnSlotValues", frame, slot, newValues);
            delegate.setDirectOwnSlotValues(frame, slot, newValues);
        }
    }

    private void addValues(FrameStore delegate, Frame frame, Collection currentValues, Collection newValues) {
        Collection valuesToAdd = new LinkedHashSet(newValues);
        valuesToAdd.removeAll(currentValues);
        Iterator i = valuesToAdd.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            if (isCorrectValueType(value)) {
                addValue(delegate, frame, value);
            } else {
                Log.error("Ignoring invalid value: " + value, this, "addValues", frame, currentValues, newValues);
            }
        }
    }

    private void removeValues(FrameStore delegate, Frame frame, Collection currentValues, Collection newValues) {
        Collection valuesToRemove = new LinkedHashSet(currentValues);
        valuesToRemove.removeAll(newValues);
        Iterator i = valuesToRemove.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            if (isCorrectValueType(value)) {
                removeValue(delegate, frame, value);
            } else {
                Log.error("Ignoring invalid value: " + value, this, "removeValues", frame, currentValues, newValues);
            }
        }
    }

    abstract List getCurrentValues(FrameStore delegate, Frame frame);
    abstract void addValue(FrameStore delegate, Frame frame, Object value);
    abstract void removeValue(FrameStore delegate, Frame frame, Object value);
    abstract boolean isCorrectFrameType(Frame frame);
    abstract boolean isCorrectValueType(Object o);
}
