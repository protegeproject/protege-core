package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.server.metaproject.Operation;

public class UnbackedOperationImpl implements Operation, Localizable, Serializable {
    private static final long serialVersionUID = 9014765198232228004L;
    
    private String name;
    private String description;
    
    public UnbackedOperationImpl(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public UnbackedOperationImpl(Operation operation) {
        this(operation.getName(), operation.getDescription());
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public void localize(KnowledgeBase kb) {
    }
    
    @Override
    public String toString() {
        return "[Operation (unbacked): " + name + "]";
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


}
