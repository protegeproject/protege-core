/**
 * 
 */
package edu.stanford.smi.protege.server.framestore.background;


import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.framestore.FrameStore;

/*
 * This is a little bit of a hack at the moment because unfortunately this 
 * class is encoding some OWL knowledge...  A better solution would be to have
 * the OWL knowledge bases enter some of this state information.
 */

public enum State {
  Start, SubClass, OwlExpr, RDFList, End;
  
  public boolean entryCondition(FrameStore fs, Frame f) {
    switch (this) {
    case OwlExpr:
      return isOWLAnonymous(fs, f);
    case SubClass:
      return !isOWLAnonymous(fs, f);
    default:
      return true;
    }
  }
  
  private boolean isOWLAnonymous(FrameStore fs, Frame f) {
    return fs.getFrameName(f).startsWith("@");
  }
}