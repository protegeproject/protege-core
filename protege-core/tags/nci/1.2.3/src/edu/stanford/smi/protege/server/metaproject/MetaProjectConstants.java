package edu.stanford.smi.protege.server.metaproject;

import edu.stanford.smi.protege.server.metaproject.impl.OperationImpl;

public class MetaProjectConstants {
		
	public final static OperationImpl OPERATION_READ = new OperationImpl("Read");
	public final static OperationImpl OPERATION_WRITE = new OperationImpl("Write");

	public final static OperationImpl OPERATION_PROPERTY_TAB_READ = new OperationImpl("PropertyTabRead");
	public final static OperationImpl OPERATION_PROPERTY_TAB_WRITE = new OperationImpl("PropertyTabWrite");

	public final static OperationImpl OPERATION_ONTOLOGY_TAB_READ = new OperationImpl("OntologyTabRead");
	public final static OperationImpl OPERATION_ONTOLOGY_TAB_WRITE = new OperationImpl("OntologyTabWrite");

	public static final String USER_WORLD = "World";
	
}
