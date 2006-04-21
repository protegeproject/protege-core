package edu.stanford.smi.protege.server.framestore;

import java.util.EventObject;
import java.util.List;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.util.LocalizeUtils;

public class RemoteResponse<X> extends ValueUpdate implements Localizable {
  private X response;
  
  /**
   * 
   * @param response the response the user selected
   * @param events the latest events which the client can use to update its cache.
   */
  public RemoteResponse(X response, List<EventObject> events) {
    super(events);
    this.response = response;
  }
  /**
   * @return the response the user selected
   */
  public X getResponse() {
    return response;
  }
  
  public void localize(KnowledgeBase kb) {
    super.localize(kb);
    LocalizeUtils.localize(response, kb);
  }
	
}
