package edu.stanford.smi.protege.server.metaproject;

import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;

public class MetaProjectConstants {
		
	public final static Operation OPERATION_READ = new UnbackedOperationImpl("Read", null);
	public final static Operation OPERATION_WRITE = new UnbackedOperationImpl("Write", null);

	public final static Operation OPERATION_PROPERTY_TAB_READ = new UnbackedOperationImpl("PropertyTabRead", null);
	public final static Operation OPERATION_PROPERTY_TAB_WRITE = new UnbackedOperationImpl("PropertyTabWrite", null);

	public final static Operation OPERATION_ONTOLOGY_TAB_READ = new UnbackedOperationImpl("OntologyTabRead", null);
	public final static Operation OPERATION_ONTOLOGY_TAB_WRITE = new UnbackedOperationImpl("OntologyTabWrite", null);

	public static final String USER_WORLD = "World";
	
}
