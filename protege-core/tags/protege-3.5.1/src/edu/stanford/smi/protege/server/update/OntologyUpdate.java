package edu.stanford.smi.protege.server.update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

  private static final long serialVersionUID = 5997164031043543620L;
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

  /* 
     Unable to figure how to avoid the EOF Exception and 
     the ClassNotFoundException.
     I think  what is happening in the first  case is that the 
     reader is trying to read too fast.
     The second case almost worked but maybe there  was  a problem
     with the classloader?

  
  private void writeObject(ObjectOutputStream out)
  throws IOException {
    GZIPOutputStream zipOut = new GZIPOutputStream(out);
    ObjectOutputStream zipObjectOut = new ObjectOutputStream(zipOut);
    zipObjectOut.writeObject(updates);
    zipObjectOut.flush();
    zipOut.finish();
  }
  
  private void readObject(ObjectInputStream in)
  throws IOException, ClassNotFoundException {
    GZIPInputStream zipIn = new GZIPInputStream(in);
    ObjectInputStream zipObjectIn = new ObjectInputStream(zipIn);
    updates = (List) zipObjectIn.readObject();
  }
    */

}
