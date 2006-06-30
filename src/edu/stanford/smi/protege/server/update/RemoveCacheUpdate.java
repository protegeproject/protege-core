package edu.stanford.smi.protege.server.update;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;

/**
 * This update means that the cache should indicate that there is no value
 * for this frame, slot, facet, isTemplate group.  That is the client does 
 * not need to call the server to obtain a value for this set.  These updates
 * should never be generated while a transation is in progress.
 * 
 * @author tredmond
 *
 */
public class RemoveCacheUpdate extends SftUpdate {

  public RemoveCacheUpdate(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
    super(frame, slot, facet, isTemplate);
  }

}
