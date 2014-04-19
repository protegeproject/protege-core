package edu.stanford.smi.protege.server.metaproject;

import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;

public class MetaProjectConstants {

	public final static Operation OPERATION_DISPLAY_IN_PROJECT_LIST = new  UnbackedOperationImpl("DisplayInProjectList", null);

	public final static Operation OPERATION_DELEGATE = new UnbackedOperationImpl("Delegate", null);

	public final static Operation OPERATION_READ = new UnbackedOperationImpl("Read", null);
	public final static Operation OPERATION_WRITE = new UnbackedOperationImpl("Write", null);
	public final static Operation OPERATION_COMMENT = new UnbackedOperationImpl("Comment", null);

	/**
     * @deprecated - Use constants from {@link OwlMetaProjectConstants}
     */
    @Deprecated
	public final static Operation OPERATION_PROPERTY_TAB_READ = new UnbackedOperationImpl("PropertyTabRead", null);
    /**
     * @deprecated - Use constants from {@link OwlMetaProjectConstants}
     */
    @Deprecated	public final static Operation OPERATION_PROPERTY_TAB_WRITE = new UnbackedOperationImpl("PropertyTabWrite", null);
    /**
     * @deprecated - Use constants from {@link OwlMetaProjectConstants}
     */
    @Deprecated
	public final static Operation OPERATION_ONTOLOGY_TAB_READ = new UnbackedOperationImpl("OntologyTabRead", null);
    /**
     * @deprecated - Use constants from {@link OwlMetaProjectConstants}
     */
    @Deprecated
    public final static Operation OPERATION_ONTOLOGY_TAB_WRITE = new UnbackedOperationImpl("OntologyTabWrite", null);

	public final static Operation OPERATION_ADMINISTER_SERVER = new UnbackedOperationImpl("AdministerServer", null);

	public static final Operation OPERATION_KILL_OTHER_USER_SESSION = new UnbackedOperationImpl("KillOtherUserSession", null);
	public static final Operation OPERATION_STOP_REMOTE_PROJECT = new UnbackedOperationImpl("StopRemoteProject", null);
	public static final Operation OPERATION_START_REMOTE_PROJECT = new UnbackedOperationImpl("StartRemoteProject", null);
	public static final Operation OPERATION_SHUTDOWN_SERVER = new UnbackedOperationImpl("ShutdownServer", null);

	public static final String USER_WORLD = "World";

}
