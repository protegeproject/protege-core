package edu.stanford.smi.protege.server.update;

import java.io.Serializable;
import java.util.List;

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
	private SerializedCacheUpdate<RemoteSession, Sft, List> update;
	

	public ValueUpdate(Frame frame,
					   SerializedCacheUpdate<RemoteSession, Sft, List> update) {
		this.frame = frame;
		this.update = update;
	}
	
	public Frame getFrame() {
		return frame;
	}
	
	public SerializedCacheUpdate<RemoteSession, Sft, List> getUpdate() {
		return update;
	}


	public void localize(KnowledgeBase kb) {
	    LocalizeUtils.localize(frame, kb);
	    if (update instanceof CacheRead) {
	        CacheRead<RemoteSession, Sft, List> read = (CacheRead<RemoteSession, Sft, List>) update;
	        read.getVar().localize(kb);
	        LocalizeUtils.localize(read.getValue().getResult(), kb);
	    }
	    else if (update instanceof CacheModify) {
	        CacheModify<RemoteSession, Sft, List> modify = (CacheModify<RemoteSession, Sft, List>) update;
	        modify.getVar().localize(kb);
	        LocalizeUtils.localize(modify.getNewValue().getResult(), kb);
	    }
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("{");
		sb.append(update);
		sb.append(" applied to ");
		if (frame != null) {
		    sb.append(frame.getFrameID().getName());
		    sb.append(" cache}");
		}
		else {
		    sb.append("all frame caches");
		}
		return sb.toString();
	}
}
