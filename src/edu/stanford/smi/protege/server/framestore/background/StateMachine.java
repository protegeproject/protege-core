package edu.stanford.smi.protege.server.framestore.background;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.util.Log;

public class StateMachine {
  private static transient Logger log = Log.getLogger(StateMachine.class);
  
  private Map<StateAndSlot, State> transitionMap
    = new HashMap<StateAndSlot, State>();
  
  public StateMachine(FrameStore fs) {
    addTransition(fs, State.Start, ":DIRECT-SUPERCLASSES", State.OwlExpr);
    /*
    addTransition(fs, State.Start, "owl:equivalentClass", State.OwlExpr);


    addTransition(fs, State.OwlExpr, "owl:intersectionOf", State.RDFList);
    addTransition(fs, State.OwlExpr, ":DIRECT-SUPERCLASSES", State.End);


    addTransition(fs, State.RDFList, "rdf:rest", State.RDFList);
    addTransition(fs, State.RDFList, "rdf:first", State.OwlExpr);

    */
  }
  
  private void addTransition(FrameStore fs, 
                             State start, String slotName, State end) {
    Slot slot = null;
    try {
      Frame sframe = fs.getFrame(slotName);
      if (sframe == null || !(sframe instanceof Slot)) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("frame found for transition " + 
              start + ", " + slotName + "/" + sframe + " -> " + end + " but not a slot");
          return;
        }
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

  public State nextState(State state, Slot slot) {
    return transitionMap.get(new StateAndSlot(state, slot));
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
