package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.background.CacheRequestReason;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;

/**
 * Protege Job for getting the own slot values of a frame slot pair
 * and the browser text of the values from the server.
 *
 * Not recommended to be used with slots that do not takes frames as values.
 * To be used with instance or class slots.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class GetOwnSlotValuesBrowserTextJob extends ProtegeJob {

	private static final long serialVersionUID = 2958930580121193877L;

	protected Frame frame;
	protected Slot slot;
	protected boolean directValues = false;

	public GetOwnSlotValuesBrowserTextJob(KnowledgeBase kb, Frame frame, Slot slot, boolean directValues) {
		super(kb);
		this.frame = frame;
		this.slot = slot;
		this.directValues = directValues;
	}

	@Override
	public Collection<FrameWithBrowserText> run() throws ProtegeException {
		List<FrameWithBrowserText> framesWithBrowserText = new ArrayList<FrameWithBrowserText>();
		addRequestsToFrameCalculator(frame);
		Collection values = getValues();
		for (Iterator iterator = values.iterator(); iterator.hasNext();) {
			Object value = iterator.next();
			if (value instanceof Frame) {
				Frame valueFrame = (Frame) value;
				framesWithBrowserText.add(new FrameWithBrowserText(valueFrame,
						valueFrame.getBrowserText(), ((Instance)valueFrame).getDirectTypes()));
				addRequestsToFrameCalculator(valueFrame);
			} else {
				framesWithBrowserText.add(new FrameWithBrowserText(null, value.toString(), null));
			}
		}
		Collections.sort(framesWithBrowserText, new FrameWithBrowserTextComparator());
		return framesWithBrowserText;
	}


	protected Collection getValues() {
		return directValues ? frame.getDirectOwnSlotValues(slot) : frame.getOwnSlotValues(slot);
	}

	private void addRequestsToFrameCalculator(Frame frm) {
		if (!getKnowledgeBase().getProject().isMultiUserServer()) {
			return;
		}
		Server server = Server.getInstance();
        RemoteSession session = ServerFrameStore.getCurrentSession();
        ServerProject serverProject = server.getServerProject(getKnowledgeBase().getProject());
        ServerFrameStore serverFrameStore = (ServerFrameStore) serverProject.getDomainKbFrameStore(session);
        FrameCalculator fc = serverFrameStore.getFrameCalculator();
        
        fc.addRequest(frm, session, CacheRequestReason.USER_REQUESTED_FRAME_VALUES);		
	}
	
	@Override
	public Collection<FrameWithBrowserText> execute() throws ProtegeException {
		return (Collection<FrameWithBrowserText>) super.execute();
	}	
	
	@Override
	public void localize(KnowledgeBase kb) {
		super.localize(kb);
		LocalizeUtils.localize(frame, kb);
		LocalizeUtils.localize(slot, kb);
	}

}
