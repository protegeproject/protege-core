package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;

public class OperationImpl extends WrappedProtegeInstanceImpl implements Operation, Serializable {
	private static final long serialVersionUID = 3175714463454087306L;

	private String name;

	protected OperationImpl(MetaProjectImpl mp, Instance op) {
		super(mp, op, ClsEnum.Operation);
		name = (String) op.getOwnSlotValue(mp.getSlot(SlotEnum.name));
	}

	public String getName() {
		return name;
	}

	@Override
    public boolean equals(Object o) {
		if (!(o instanceof Operation))  {
			return false;
		}
		Operation other = (Operation) o;
		return name.equals(other.getName());
	}

	@Override
    public int hashCode() {
		return name.hashCode();
	}

	@Override
    public String toString() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		setSlotValue(SlotEnum.name, name);
	}
	
	public String getDescription() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(SlotEnum.description));
		if (value != null && !(value instanceof String)) {
			throw new OntologyException("The " + SlotEnum.name + " slot should take on string values");
		}
		return (String) value;		
	}
	
	public void setDescription(String description) {
		setSlotValue(SlotEnum.description, description);
	}
	
	// *********************** Deprecated constants *********************** //
	
	/**
	 * @deprecated - Use constants from {@link MetaProjectConstants}
	 */
	@Deprecated
	public final static Operation READ = MetaProjectConstants.OPERATION_READ;
	
	/**
	 * @deprecated - Use constants from {@link MetaProjectConstants}
	 */
	@Deprecated 
	public final static Operation WRITE = MetaProjectConstants.OPERATION_WRITE;

	/**
	 * @deprecated - Use constants from {@link MetaProjectConstants}
	 */
	@Deprecated
	public final static Operation PROPERTY_TAB_READ = MetaProjectConstants.OPERATION_PROPERTY_TAB_READ;
	
	/**
	 * @deprecated - Use constants from {@link MetaProjectConstants}
	 */
	@Deprecated
	public final static Operation PROPERTY_TAB_WRITE = MetaProjectConstants.OPERATION_ONTOLOGY_TAB_WRITE;
	
	/**
	 * @deprecated - Use constants from {@link MetaProjectConstants}
	 */
	@Deprecated
	public final static Operation ONTOLOGY_TAB_READ = MetaProjectConstants.OPERATION_ONTOLOGY_TAB_READ;
	
	/**
	 * @deprecated - Use constants from {@link MetaProjectConstants}
	 */
	@Deprecated
	public final static Operation ONTOLOGY_TAB_WRITE = MetaProjectConstants.OPERATION_ONTOLOGY_TAB_WRITE;

	
}
