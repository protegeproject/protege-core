package edu.stanford.smi.protege.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;

/*
 * There is a temptation to define Protege using a generic here.  But this
 * creates a problem.  If we define a class Foo extending Protege<String>
 * then the class Foo will include a defintion of 
 *      String execute();
 * Unfortunately code expecting a Protege object will need a definition of 
 *      Object execute()
 * and will not find it.  This can lead to AbstractMethodError even when the 
 * sources are in sync and everything compiles (on at least some jvm).  It is possible
 * that the jvm generating the exception and the jvm that compiled the code
 * have different treatment of this situation. 
 * 
 * There may be a good solution to this problem (generic method declarations) but 
 * there are several places where this can go wrong (e.g. the rmi implementation?)
 * so I have decided not to mess with it.
 * 
 * This problem is jvm specific.  On the OS X jvm, there appears to be no
 * problem but on other jvm's there is.  From my reading of the generics 
 * documentation, this should not happen.  This case is explicitly  considered
 * and a solution is explained.  Is this related to RMI and is it a java bug?
 * 
 * Still under investigation...
 */

/**
 * This class defines a unit of work to be executed in the 
 * context of a knowledge base.  The purpose of this class is 
 * to allow a caller to define a unit of work that will be performed
 * on the server if the jvm defining the job is a client accessing a
 * Protege server.  If the jvm is executing the knowledge base locally, 
 * this unit of work is executed directly on the local knowledge base.
 * 
 * Most developers will need to override both the run() and localize() methods.
 *
 * @author tredmond
 */
public abstract class ProtegeJob extends RemoteJob implements Localizable, Serializable {
  private static final long serialVersionUID = -3178401391384077767L;

  private static transient Logger log = Log.getLogger(ProtegeJob.class);

  private transient KnowledgeBase kb;
  private transient RemoteClientFrameStore clientFrameStore = null;
  
  /**
   * The main constructor for ProtegeJob's.
   * @param kb - the knowledge base to be used when executing this job.
   */
  public ProtegeJob(KnowledgeBase kb) {
    this.kb = kb;
    DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
    FrameStore terminalFrameStore = dkb.getTerminalFrameStore();
    if (terminalFrameStore instanceof RemoteClientFrameStore) {
      clientFrameStore = (RemoteClientFrameStore) terminalFrameStore;
    }
  }
  
  
  
  /**
   * This method will either execute the job.run() method or - in the
   * case that the caller is a client to a Protege server - it will copy
   * this job to the server so that it can execute there.
   * 
   * @return the object returned by the execution of this job.
   * @throws ProtegeException the exception thrown by this job during its execution.
   */
  @SuppressWarnings("unchecked")
  public Object execute() throws ProtegeException {
    if (clientFrameStore == null) {
      return run();
    } else {
      return clientFrameStore.executeProtegeJob(this);
    }
  }

  /**
   * Returns the knowledge base associated with this job.
   * 
   * @return the knowledge base associated with this job.
   */
  public KnowledgeBase getKnowledgeBase() {
    return kb;
  }
  
  public void fixLoader() {
	  ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
	  ClassLoader jobLoader = getClass().getClassLoader();
	  ClassLoader kbLoader = kb.getClass().getClassLoader();
	  ClassLoader correctLoader;
	  if (jobLoader == kbLoader) {
		  correctLoader = jobLoader;
	  }
	  else {
		  Collection<ClassLoader> loaders = new ArrayList<ClassLoader>();
		  loaders.add(kbLoader);
		  loaders.add(jobLoader);
		  correctLoader = new MultiplexingClassLoader(loaders);
	  }
	  if (currentLoader != correctLoader) {
		  if (log.isLoggable(Level.FINEST)) {
			  Log.getLogger().finest("Changing loader from " + currentLoader + " to " + correctLoader);
		  }
		  Thread.currentThread().setContextClassLoader(correctLoader);
	  }
  }
  
  /**
   * This is a utility for dealing with the metaproject instance when it is known that we are executing
   * on the server side.
   * 
   * This utility makes it easier for developers to access and work with the meta-ontology in 
   * server-client mode.
   * 
   * @return The ProjectInstance for this project.
   */
  public ProjectInstance getMetaProjectInstance() {
    RemoteSession session = ServerFrameStore.getCurrentSession();
    ServerProject serverProject = Server.getInstance().getServerProject(getKnowledgeBase().getProject());
    ServerFrameStore serverFs = (ServerFrameStore) serverProject.getDomainKbFrameStore(session);
    return serverFs.getMetaProjectInstance();
  }
  
  public boolean serverSideCheckOperationAllowed(Operation op) {
      return serverSideCheckOperationAllowed(op, getMetaProjectInstance());
  }
  
  /**
   * This call is used to make needed adjustments to objects after they have
   * been serialized and then deserialized.  The main adjustment needed is to 
   * set the knowledge base in all the frames.  It is likely that all sub-classes
   * will need to implement this method.
   * 
   * @param kb The knowledge base of running on the jvm that deserialized this job.
   */
  public void localize(KnowledgeBase kb) {
    this.kb = kb;
  }
  
}
