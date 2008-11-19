package edu.stanford.smi.protege.server.update;

import java.io.Serializable;
import java.util.Collection;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheModify;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheRead;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

public class ValueUpdate implements Serializable, Localizable{
    private static final long serialVersionUID = -7753881900765528485L;
    
    private Frame frame;
	private RemoteSession destination;
	private SerializedCacheUpdate<RemoteSession, Sft, Collection> update;
	

	public ValueUpdate(Frame frame,
					   SerializedCacheUpdate<RemoteSession, Sft, Collection> update,
					   RemoteSession destination) {
		this.frame = frame;
		this.update = update;
		this.destination = destination;
	}
	
	public Frame getFrame() {
		return frame;
	}

	public RemoteSession getDestination() {
		return destination;
	}

	public SerializedCacheUpdate<RemoteSession, Sft, Collection> getUpdate() {
		return update;
	}


	public void localize(KnowledgeBase kb) {
	    LocalizeUtils.localize(frame, kb);
	    if (update instanceof CacheRead) {
	        CacheRead<RemoteSession, Sft, Collection> read = (CacheRead<RemoteSession, Sft, Collection>) update;
	        read.getVar().localize(kb);
	        LocalizeUtils.localize(read.getValue().getResult(), kb);
	    }
	    else if (update instanceof CacheModify) {
	        CacheModify<RemoteSession, Sft, Collection> modify = (CacheModify<RemoteSession, Sft, Collection>) update;
	        modify.getVar().localize(kb);
	        LocalizeUtils.localize(modify.getNewValue().getResult(), kb);
	    }
	}
}
