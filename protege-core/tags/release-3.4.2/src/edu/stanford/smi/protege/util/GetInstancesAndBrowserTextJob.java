package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.background.CacheRequestReason;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;

/**
 * Protege Job for getting the instances and their browser text from the server.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class GetInstancesAndBrowserTextJob extends ProtegeJob {

	private static final long serialVersionUID = 2958930580121193877L;

	private Collection<Cls> clses;
	private boolean directInstances = true;

	public GetInstancesAndBrowserTextJob(KnowledgeBase kb, Collection<Cls> clses, boolean directInstances) {
		super(kb);
		this.clses = clses;
		this.directInstances = directInstances;
	}

	@Override
	public Collection<FrameWithBrowserText> run() throws ProtegeException {
		/*
		 * If you want to send the value updates for the getDirectInstances call
		 * together with this protege job, then use the method:
		 * serverFrameStore.cacheValuesReadFromStore(..).
		 * Very important: synchronize on the kb the getDirectInstances and the cacheValueReadFromStore
		 */
		List<FrameWithBrowserText> framesWithBrowserText = new ArrayList<FrameWithBrowserText>();
		for (Cls cls : clses) {
			Collection<Instance> instances = directInstances ? cls.getDirectInstances() : cls.getInstances();
			for (Instance instance : instances) {
				framesWithBrowserText.add(
						new FrameWithBrowserText(instance, instance.getBrowserText(), instance.getDirectTypes()));
			}
			addRequestsToFrameCalculator(instances); //move outside
		}
		Collections.sort(framesWithBrowserText, new FrameWithBrowserTextComparator());
		return framesWithBrowserText;
	}


	private void addRequestsToFrameCalculator(Collection<Instance> instances) {
		if (!getKnowledgeBase().getProject().isMultiUserServer()) {
			return;
		}
		Server server = Server.getInstance();
        RemoteSession session = ServerFrameStore.getCurrentSession();
        ServerProject serverProject = server.getServerProject(getKnowledgeBase().getProject());
        ServerFrameStore serverFrameStore = (ServerFrameStore) serverProject.getDomainKbFrameStore(session);
        FrameCalculator fc = serverFrameStore.getFrameCalculator();

        for (Instance instance : instances) {
        	fc.addRequest(instance, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);
		}
	}

	@Override
	public Collection<FrameWithBrowserText> execute() throws ProtegeException {
		return (Collection<FrameWithBrowserText>) super.execute();
	}

	@Override
	public void localize(KnowledgeBase kb) {
		super.localize(kb);
		LocalizeUtils.localize(clses, kb);
	}

}
