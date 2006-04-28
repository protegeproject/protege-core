package edu.stanford.smi.protege.server.update;

import java.io.Serializable;
import java.util.List;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.util.LocalizeUtils;

/**
 * When a client asks for a set of values, this structure includes
 * both the values calculated and the events that tell the client how
 * to update his cache.
 * 
 * @author tredmond
 *
 */
public class OntologyUpdate implements Serializable, Localizable {

  private List<ValueUpdate> updates;
  
  /**
   * 
   * @return the latest events which the client can use to update its cache.
   */
  public List<ValueUpdate> getValueUpdates() {
    return updates;
  }


  /**
   * 
   * @param values the values the user selected
   * @param events the latest events which the client can use to update its cache.
   */
  public OntologyUpdate(List<ValueUpdate> updates) {
    this.updates = updates;
  }


  public void localize(KnowledgeBase kb) {
     LocalizeUtils.localize(updates, kb);
  }
  

}
