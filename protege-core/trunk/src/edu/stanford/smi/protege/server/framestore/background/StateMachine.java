package edu.stanford.smi.protege.server.framestore.background;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.util.Log;

public class StateMachine {
  private static transient Logger log = Log.getLogger(StateMachine.class);
  
  private FrameStore fs;
  private final Object kbLock;
  
  private Map<StateAndSlot, State> transitionMap
    = new HashMap<StateAndSlot, State>();
  
  public StateMachine(FrameStore fs, Object kbLock) {
    this.fs = fs;
    this.kbLock = kbLock;
    synchronized (kbLock) {
      addTransition(State.Start, Model.Slot.DIRECT_SUPERCLASSES, State.OwlExpr);
      
      addTransition(State.Start, "owl:equivalentClass", State.OwlExpr);
      
      
      addTransition(State.OwlExpr, "owl:intersectionOf", State.RDFList);
      addTransition(State.OwlExpr, Model.Slot.DIRECT_SUPERCLASSES, State.End);
      addTransition(State.OwlExpr, "owl:someValuesFrom", State.End);
      
      addTransition(State.RDFList, "rdf:rest", State.RDFList);
      addTransition(State.RDFList, "rdf:first", State.OwlExpr);

    }
  }
  
  private void addTransition(State start, String slotName, State end) {
    Slot slot = null;
    try {
      Frame sframe = fs.getFrame(slotName);
      if (sframe == null || !(sframe instanceof Slot)) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("frame found for transition " + 
              start + ", " + slotName + "/" + sframe + " -> " + end + " but not a slot");
        }
        return;
      }
      slot = (Slot) sframe;
      transitionMap.put(new StateAndSlot(start, slot), end);
    } catch (Exception e) {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Exception caught creating transition " + 
            start + ", " + slotName+ " -> " + end + ": " + e);
        log.log(Level.FINER, "Exception = ", e);
      }
    }
  }
  
  public State nextState(State state, Slot slot, Frame endingFrame) {
    State endState = transitionMap.get(new StateAndSlot(state, slot));
    synchronized (kbLock) {
      if (endState != null && endState.entryCondition(fs, endingFrame)) {
        return endState;
      }
    }
    return null;
  }

  private class StateAndSlot {
    private State state;
    private Slot slot;
    
    public Slot getSlot() {
      return slot;
    }

    public State getState() {
      return state;
    }

    public StateAndSlot(State state, Slot slot) {
      this.state = state;
      this.slot = slot;
    }
    
    public int hashCode() {
      return state.ordinal() + slot.hashCode();
    }
    
    public boolean equals(Object o) {
      if (o == null || !(o instanceof StateAndSlot)) {
        return false;
      }
      StateAndSlot other = (StateAndSlot) o;
      return other.state.equals(state) && other.slot.equals(slot);
    }
    
  }
}
