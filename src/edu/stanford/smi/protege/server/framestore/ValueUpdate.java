package edu.stanford.smi.protege.server.framestore;

import java.io.Serializable;
import java.util.EventObject;
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
public class ValueUpdate implements Serializable, Localizable {

  private List<EventObject> events;
  
  /**
   * 
   * @return the latest events which the client can use to update its cache.
   */
  public List<EventObject> getEvents() {
    return events;
  }


  /**
   * 
   * @param values the values the user selected
   * @param events the latest events which the client can use to update its cache.
   */
  public ValueUpdate(List<EventObject> events) {
    this.events = events;
  }


  public void localize(KnowledgeBase kb) {
     LocalizeUtils.localize(events, kb);
  }
  

}
