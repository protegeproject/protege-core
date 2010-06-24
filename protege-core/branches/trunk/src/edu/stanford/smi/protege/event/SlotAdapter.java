package edu.stanford.smi.protege.event;

/**
 * Adapter for the SlotListener interface.  If you don't want to handle all of the slot events then subclass this class.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class SlotAdapter implements SlotListener {

    public void templateSlotClsAdded(SlotEvent event) {
    }

    public void templateSlotClsRemoved(SlotEvent event) {
    }
    public void directSubslotAdded(SlotEvent event) {
    }
    public void directSubslotRemoved(SlotEvent event) {
    }
    public void directSubslotMoved(SlotEvent event) {
    }
    public void directSuperslotAdded(SlotEvent event) {
    }
    public void directSuperslotRemoved(SlotEvent event) {
    }
}
