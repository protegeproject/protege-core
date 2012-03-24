package edu.stanford.smi.protege.code.generator.wrapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.LocalizeUtils;

public abstract class AbstractWrappedInstance implements Localizable, Serializable, Disposable {
	private static final long serialVersionUID = -5498034704463571567L;
    private Instance wrappedProtegeInstance;

	protected AbstractWrappedInstance(Instance instance) {
		wrappedProtegeInstance = instance;
	}

	protected AbstractWrappedInstance() {

	}

	public Instance getWrappedProtegeInstance() {
		return wrappedProtegeInstance;
	}

	public KnowledgeBase getKnowledgeBase() {
		return wrappedProtegeInstance == null ? null : wrappedProtegeInstance.getKnowledgeBase();
	}

	public String getName() {
		return wrappedProtegeInstance.getName();
	}

	protected boolean hasSlotValues(Slot slot) {
		return getKnowledgeBase().getOwnSlotValueCount(wrappedProtegeInstance, slot) > 0;
	}

		protected void addSlotValue(Slot slot, Object value) {
		wrappedProtegeInstance.addOwnSlotValue(slot, getUnwrappedObject(value));
	}

	protected void removeSlotValue(Slot slot, Object value) {
		wrappedProtegeInstance.removeOwnSlotValue(slot, getUnwrappedObject(value));
	}

	protected void setSlotValue(Slot slot, Object value) {
		wrappedProtegeInstance.setOwnSlotValue(slot, getUnwrappedObject(value));
	}

	protected void setSlotValues(Slot slot, Collection<?> values) {
		wrappedProtegeInstance.setOwnSlotValues(slot, getUnwrappedCollection(values));
	}

	public boolean canAs(Class<?> javaInterface) {
		return OntologyJavaMappingUtil.canAs(this, javaInterface);
	}

	public <X> X as(Class<? extends X> javaInterface) {
		return OntologyJavaMappingUtil.as(this, javaInterface);
	}

	public void delete() {
		wrappedProtegeInstance.delete();
	}

	@SuppressWarnings("unchecked")
	private Collection<?> getUnwrappedCollection(Collection<?> values) {
		Collection newValues = new ArrayList();
		for (Object value : values) {
			newValues.add(getUnwrappedObject(value));
		}
		return newValues;
	}

	private Object getUnwrappedObject (Object value) {
		if (value instanceof AbstractWrappedInstance) {
			value = ((AbstractWrappedInstance) value).getWrappedProtegeInstance();
		}
		return value;
	}


    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof AbstractWrappedInstance)) {
    		return false;
    	}

    	Instance wrappedProtegeInstance2 = ((AbstractWrappedInstance)obj).getWrappedProtegeInstance();
    	if (wrappedProtegeInstance2 == null) {
    		return getWrappedProtegeInstance() == null;
    	}
		return wrappedProtegeInstance2.equals(getWrappedProtegeInstance());
    }

    @Override
    public int hashCode() {
    	return getWrappedProtegeInstance().getName().length() + 42;
    }
    
    public void localize(KnowledgeBase kb) {
        LocalizeUtils.localize(wrappedProtegeInstance, kb);
    }

    public void dispose() {
    	wrappedProtegeInstance = null;    	
    }
    
    
    @Override
    public String toString() {
    	return getName();
    }
}
