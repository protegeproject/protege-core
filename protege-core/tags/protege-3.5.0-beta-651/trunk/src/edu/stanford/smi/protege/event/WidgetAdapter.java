package edu.stanford.smi.protege.event;

/**
 * Adapter class for listeners to widget events.  Subclass this class if you don't want to handle all widget events.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class WidgetAdapter implements WidgetListener {

    public void labelChanged(WidgetEvent event) {
    }

    public void layoutChanged(WidgetEvent event) {
    }
}
