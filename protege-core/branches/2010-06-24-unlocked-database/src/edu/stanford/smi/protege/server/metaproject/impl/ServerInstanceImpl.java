package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.ServerInstance;

public class ServerInstanceImpl extends PolicyControlledObjectImpl implements ServerInstance, Serializable {
	private static final long serialVersionUID = 1306812430518384699L;

	protected ServerInstanceImpl(MetaProjectImpl mp, Instance pi) {
		super(mp, pi, MetaProjectImpl.ClsEnum.Server);
	}
		
	public String getHostName() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(MetaProjectImpl.SlotEnum.hostName));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + MetaProjectImpl.SlotEnum.hostName + " slot should take on string values");
		}
		return (String) value;		
	}
		
	public void setHostName(String hostName) {
		setSlotValue(MetaProjectImpl.SlotEnum.hostName, hostName);
	}
}
