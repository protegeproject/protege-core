package edu.stanford.smi.protege.server.metaproject.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MessageError;

public class MetaProjectImpl implements MetaProject {
  private KnowledgeBase kb;
  private Policy policy;
  
  public MetaProjectImpl(URI metaprojectURI) {
    Collection errors = new ArrayList();
    Project project = Project.loadProjectFromURI(metaprojectURI, errors);
    if (!errors.isEmpty()) {
        throw new RuntimeException(errors.iterator().next().toString());
    }
    kb = project.getKnowledgeBase();
  }

  
  public Cls getCls(ClsEnum cls) throws OntologyException {
    Cls frameCls = kb.getCls(cls.toString());
    if (frameCls == null) {
      throw new OntologyException("Metaproject Ontology should contain a class " + cls);
    }
    return frameCls;
  }
  
  public Slot getSlot(SlotEnum slot) throws OntologyException {
    Slot frameSlot = kb.getSlot(slot.toString());
    if (frameSlot == null) {
      throw new OntologyException("Metaproject Ontology should contain a slot " + slot);
    }
    return frameSlot;
  }
  
  protected WrappedProtegeInstanceImpl wrapInstance(ClsEnum cls, Instance i) {
    if (!i.hasType(getCls(cls))) {
      throw new IllegalArgumentException("" + i + " should be a " + cls + " instance");
    }
    switch (cls) {
    case GroupOperation:
      return new GroupAndOperationImpl(this, i);
    case Group:
      return new GroupInstanceImpl(this, i);
    case Project:
      return new MetaProjectInstanceImpl(this, i);
    case Operation:
      return new OperationImpl(this, i);
    case User:
      return new UserInstanceImpl(this, i);
    default:
      throw new UnsupportedOperationException("Unexpected cls " + cls);
    }
  }
  
  @SuppressWarnings("unchecked")
  protected Set getWrappedInstances(ClsEnum cls) {
    Set instances = new HashSet();
    for (Instance i : kb.getInstances(getCls(cls))) {
      instances.add(wrapInstance(cls, i));
    }
    return instances;
  }
  
  @SuppressWarnings("unchecked")
  public Set<MetaProjectInstance> getProjectInstances() {
    return (Set<MetaProjectInstance>) getWrappedInstances(ClsEnum.Project);
  }
  
  public MetaProjectInstance getProjectInstance(String name) {
    Collection frames = kb.getFramesWithValue(getSlot(SlotEnum.name), null, false, name);
    if (frames == null || frames.isEmpty()) {
      return null;
    }
    Frame frame = (Frame) frames.iterator().next();
    if (!(frame instanceof Frame)) {
      return null;
    }
    return new MetaProjectInstanceImpl(this, (Instance) frame);
  }
 
  @SuppressWarnings("unchecked")
  public Set<UserInstance> getUserInstances() {
    return (Set<UserInstance>) getWrappedInstances(ClsEnum.User);
  }
  
  @SuppressWarnings("unchecked")
  public Set<Operation> getOperations() {
    return (Set<Operation>) getWrappedInstances(ClsEnum.Operation);
  }
  
  public Policy getPolicy() {
    if (policy == null) {
      policy = new  PolicyImpl(this);
    }
    return policy;
  }
  
  public KnowledgeBase getKnowledgeBase() {
      return kb;
  }
  
	public boolean save(Collection errors) {
		ArrayList saveErrors = new ArrayList();
		
		try {			
			kb.getProject().save(saveErrors);

			if (saveErrors.size() > 0) {
				Log.getLogger().warning("Server: Errors at saving metaproject. Error messages: " + saveErrors);
				errors.addAll(saveErrors);
				return false;
			}

		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Server: Errors at saving metaproject. Error message: " + e.getMessage(), e);
			errors.add(e);
			return false;
		}
		
		Log.getLogger().info("SERVER: Saved metaproject.");
		return true;
	}

	//TT: Tim, can you please implement this method? Thanks!
	public MetaProjectInstance createMetaProjectInstance(String name) {
		// TODO Auto-generated method stub
		return null;
	}
 
}
