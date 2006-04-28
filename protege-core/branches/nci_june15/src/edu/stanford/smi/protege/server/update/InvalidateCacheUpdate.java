package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;


/**
 * This update means that there may or may not be a value for 
 * this frame, slot, facet, isTemplate group.  That is to say
 * the client must call the server to determine the value for this
 * frame, slot, facet, isTemplate combination.  If you want to tell
 * the client that there is definitely no value for this frame, slot, facet
 * isTemplate combination, see RemoveCacheUpdate
 * 
 * @author tredmond
 *
 */
public class InvalidateCacheUpdate extends SftUpdate {

  public InvalidateCacheUpdate(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
    super(frame, slot, facet, isTemplate);
  }

}
