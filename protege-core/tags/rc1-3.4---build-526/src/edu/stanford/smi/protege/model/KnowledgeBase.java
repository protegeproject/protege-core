package edu.stanford.smi.protege.model;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FacetListener;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.InstanceListener;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.event.ServerProjectListener;
import edu.stanford.smi.protege.event.SlotListener;
import edu.stanford.smi.protege.event.TransactionListener;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.util.Disposable;

/**
 *  A container for frames.  Frame creation is funneled through here.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface KnowledgeBase extends Disposable {
    int MAXIMUM_CARDINALITY_UNBOUNDED = -1;
    int UNLIMITED_MATCHES = -1;

    /**
     * This method requires some background information.  When Protege wants to
     * create a simple instance in the kb it creates an instance of the java
     * class "DefaultSimpleInstance".  Programmers can subclass this class and
     * give the subclass the name of a class in the kb.  Then when protege wants
     * to create an instance of the class in the kb it will instead make an
     * instance of the programmers java class rather than DefaultSimpleInstance.
     * In order for this to work the user must specify the package that their
     * java class appears in.  (The class file must also be in the plugins directory.)
     * For example, consider a protege project with a class A.  Now a programmer
     * creates a subclass of model.DefaultSimpleInstance and calls it org.me.A.
     * He puts this .class file in the plugins/org/me directory.
     * (Presumably he creates the subclass  this in order to add additional
     * methods onto org.me.A
     * to provide some sort of desired functionality.  The desired behavior is
     * that when the user creates an instance of the kb class A that the system
     * will create an instance of org.me.A and put it in the kb.  Then when the
     * programmer queries the kb for "get me instances of A" he will get back instances
     * which can be cast to org.me.A.
     *
     * So, what this method does is to tell the system what package to search to
     * find the java class to create.  When the system creates an instance of
     * A it searches all the packages, in order, to find a java class with the
     * name <package_name>.A.  If it finds one then it uses it to create an instance.
     * If it doesn't find any matches then it creates an instance of
     * DefaultSimpleInstance.
     */
    void addJavaLoadPackage(String path);

    void addKnowledgeBaseListener(KnowledgeBaseListener listener);

    boolean areValidOwnSlotValues(Frame frame, Slot slot, Collection values);

    boolean containsFrame(String name);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Cls createCls(String name, Collection parents);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Cls createCls(String name, Collection parents, Cls metaCls);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Cls createCls(String name, Collection parents, Cls metaCls, boolean initializeDefaults);

    /**
     * @param id Pass null to cause the system to generate an id
     * @param name Pass null to cause the system to generate a name
     */
    Cls createCls(FrameID id, Collection parents, Collection metaClses, boolean initializeDefaults);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Facet createFacet(String name);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Facet createFacet(String name, Cls metaCls);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Facet createFacet(String name, Cls metaCls, boolean initializeDefaults);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Instance createInstance(String name, Cls directType);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Instance createInstance(String name, Collection directTypes);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Instance createInstance(String name, Cls directType, boolean initializeDefaults);

    /**
     * @param id Pass null to cause the system to generate an id
     * @param name Pass null to cause the system to generate a name
     */
    Instance createInstance(FrameID id, Cls directType, boolean initializeDefaults);

    /**
     * @param id Pass null to cause the system to generate an id
     * @param name Pass null to cause the system to generate a name
     */
    Instance createInstance(FrameID id, Collection directTypes, boolean initializeDefaults);

    /**
     * @param id Pass null to cause the system to generate an id
     * @param name Pass null to cause the system to generate a name
     */
    SimpleInstance createSimpleInstance(FrameID id, Collection directTypes, boolean initializeDefaults);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Slot createSlot(String name);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Slot createSlot(String name, Cls metaCls);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Slot createSlot(String name, Cls metaCls, boolean initializeDefaults);

    /**
     * @param name Pass null to cause the system to generate a name
     */
    Slot createSlot(String name, Cls metaCls, Collection superslots, boolean initializeDefaults);

    /**
     *
     *
     * @deprecated    pass "null" in as a frame name to a create method to get a
     *      gensym name
     */
    String createUniqueFrameName(String name);

    void deleteCls(Cls cls);

    void deleteFacet(Facet facet);

    void deleteFrame(Frame frame);

    void deleteInstance(Instance instance);

    void deleteSlot(Slot slot);

    /**
     * Returns a string that contains something like "build 840" that refers to the
     * build of Protege in use when this kb was last saved.
     * This string format is not reliable and may change.
     */
    String getBuildString();

    /**
     * Allows a programmer to hang arbitrary information on a kb
     * and retrieve it later.  This information is not persistent.
     * Since all programmers share the same "client information space" we
     * recommend that your "key" be your java.lang.Class object.  If you want
     * to store more than one piece of information then you can make your
     * value a set (or a map).  Thus the common usage would be:
     * <pre> <code>
     *       Map myGoodStuff = new HashMap();
     *       myGoodStuff.put("foo", "bar");
     *       kb.setClientInformation(myClass.class, myGoodStuff);
     *       // ... later
     *       Map myGoodStuff = (Map) kb.getClientInformation(myClass.class);
     * </code> </pre>
     *
     * Widget writers typically don't need this sort of thing but backend
     * authors do because one instance of the backend may need to communicate
     * information to another instance (the loader to the storer, for example).
     * This is very similar to the "client property" feature on
     * javax.swing.JComponent or the client information on MS Windows windows
     * and it exists for the same reasons.  It allows you to store anything
     * you want.
     */
    Object getClientInformation(Object key);

    Cls getCls(String name);

    int getClsCount();

    Collection getClses();

    /**
    * Get classes whose name matches the give string.  This string allows "*" for "match any sequence" of characters.
    * The string is not a regular expression.  The matching is case-insensitive.
    */
    Collection getClsNameMatches(String s, int maxMatches);

    /**
     * If no meta class is specified when a class is created the system uses the metaclass of the first specified parent.
     * If no parent is specified, or if the parent is :THING then the system uses this metaclass.
     */
    Cls getDefaultClsMetaCls();

    Cls getDefaultFacetMetaCls();

    Cls getDefaultSlotMetaCls();

    Facet getFacet(String name);

    int getFacetCount();

    Collection getFacets();

    Frame getFrame(String name);
    SimpleInstance getSimpleInstance(String name);
    Frame getFrame(FrameID id);

    int getFrameCount();

    String getFrameCreationTimestamp(Frame frame);

    String getFrameCreator(Frame frame);

    String getFrameLastModificationTimestamp(Frame frame);

    String getFrameLastModifier(Frame frame);

    Collection getFrameNameMatches(String s, int maxMatches);

    String getFrameNamePrefix();

    Collection<Frame> getFrames();
    
    FrameStoreManager getFrameStoreManager();

    /**
     * Gets frames with a particular own/template slot/facet value.
     *
     * @param slot The slot to use.  Must not be null.
     * @param facet If null then the value parameter is a slot value.  Otherwise it is a facet value.
     * @param isTemplate If true then the return value is a template slot/facet value.
     * @param value A value to match exactly.  The type of the object can be one of those listed in the documentation
     * for {@link Frame}
     * @return Collection of frames.
     */
    Collection getFramesWithValue(Slot slot, Facet facet, boolean isTemplate, Object value);

    Instance getInstance(String fullname);

    Collection<Instance> getInstances();

    Collection<Instance> getInstances(Cls cls);

    String getInvalidOwnSlotValuesText(Frame frame, Slot slot, Collection values);

    String getInvalidOwnSlotValueText(Frame frame, Slot slot, Object value);

    KnowledgeBaseFactory getKnowledgeBaseFactory();

    /**
     * Finds all occurances of a string in the knowledge base.
     * 
     * This function searches all slot/facet values for the given slot and facet and looks for string values that 
     * match matchString.  A string value is said to match matchString if the string value is a match for the regular
     * expression given by matchString.
     * 
     * In the interests of performance, there is some leeway in what exactly is meant by a match.  For the in
     * memory backends, the match is calculated using the java.util.regex.Pattern algorithm except that any occurance
     * "*" in the matchString is replaced with ".*" (e.g. "*" matches anything).  However, database backends
     * use a variation of the database LIKE operator except that "*" (match everything in the getMatching frames
     * sense) is mapped to "%" (match everything in the database sense).
     *
     * @param slot The slot to use.  Must not be null.
     * @param facet If null then the matchString parameter is a slot value.  Otherwise it is a facet value.
     * @param isTemplate If true then the return value is a template slot/facet value.
     * @param matchString The string to match on.  The string can include wild cards.  The matching is case insensitive.
     * @param maxMatches maximum number of matches.  Use -1 to get all matches.  Be careful though.  If the use
     * searches on "e" do you really want to return 1M frames?
     * @return Collection of frames.
     */
    Collection<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String matchString, int maxMatches);

    String getName();

    int getNextFrameNumber();

    Project getProject();

    Collection getReachableSimpleInstances(Collection roots);

    /**
     * @return A collection of #Reference instances.
     */
    Collection<Reference> getReferences(Object o, int maxReferences);
    Collection<Reference> getMatchingReferences(String s, int maxReferences);

    Collection<Cls> getClsesWithMatchingBrowserText(String s, Collection superclasses, int maxMatches);
    Cls getRootCls();

    /**
     * @return ":THING" wrapped in a collection.  A convenience method.
     */
    Collection getRootClses();

    Cls getRootClsMetaCls();

    Cls getRootFacetMetaCls();

    Cls getRootSlotMetaCls();

    Collection getRootSlots();

    Slot getSlot(String name);

    int getSlotCount();

    Collection getSlots();

    String getSlotValueLastModificationTimestamp(Frame frame, Slot slot, boolean isTemplate);

    String getSlotValueLastModifier(Frame frame, Slot slot, boolean isTemplate);

    Collection getSubclasses(Cls cls);

    Collection getUnreachableSimpleInstances(Collection roots);

    String getUserName();

    /**
     * @return a string that contains something like "Version 1.6.2" that refers to the
     * version of Protege in use when this kb was last saved.
     * This string format is not reliable and may change.
     */
    String getVersionString();

    boolean hasChanged();

    boolean isAutoUpdatingFacetValues();

    boolean isClsMetaCls(Cls cls);

    boolean isDefaultClsMetaCls(Cls cls);

    boolean isDefaultFacetMetaCls(Cls cls);

    boolean isDefaultSlotMetaCls(Cls cls);

    boolean isFacetMetaCls(Cls cls);

    boolean isLoading();

    boolean isSlotMetaCls(Cls cls);
    
    boolean isUndoEnabled();

    boolean isValidOwnSlotValue(Frame frame, Slot slot, Object value);

    void removeJavaLoadPackage(String path);

    void removeKnowledgeBaseListener(KnowledgeBaseListener listener);

    /**
     * @deprecated Use setModificationRecordUpdatingEnabled
     */
    void setAutoUpdateFacetValues(boolean b);
    boolean setModificationRecordUpdatingEnabled(boolean b);

    void setBuildString(String s);

    void setChanged(boolean b);

    Object setClientInformation(Object key, Object value);

    void setDefaultClsMetaCls(Cls cls);

    void setDefaultFacetMetaCls(Cls cls);

    void setDefaultSlotMetaCls(Cls cls);

    /**
     * @deprecated Use #getGenerateEventsEnabled(boolean)
     */
    boolean getEventsEnabled();
    /**
     * @deprecated Use #setGenerateEventsEnabled(boolean)
     */
    boolean setEventsEnabled(boolean enabled);
    boolean setUndoEnabled(boolean enabled);

    boolean getDispatchEventsEnabled();
    boolean setDispatchEventsEnabled(boolean enabled);

    boolean getGenerateEventsEnabled();
    boolean setGenerateEventsEnabled(boolean enabled);
    boolean setGenerateDeletingFrameEventsEnabled(boolean enabled);

    void setPollForEvents(boolean enabled);

    boolean setJournalingEnabled(boolean enabled);
    boolean isJournalingEnabled();

    boolean setArgumentCheckingEnabled(boolean enabled);
    boolean setChangeMonitorEnabled(boolean enabled);
    boolean setCleanDispatchEnabled(boolean enabled);
    boolean setFacetCheckingEnabled(boolean enabled);

    void setFrameNamePrefix(String name);

    // void setLoading(boolean b);

    void setName(String name);

    void setNextFrameNumber(int i);

    void setProject(Project project);

    /**
     * Checks every call that changes an own slot value that the new value
     * is consistent with all facets.  This checking is disabled by default.  It can significately slow
     * down the system but is useful for tracking down bugs in code that calls the api (both system
     * and user code).
     */
    void setValueChecking(boolean b);

    void setVersionString(String s);

    void setFrameFactory(FrameFactory factory);

    // new methods
    /** add a listener fro frame evnets for a particular frame */
    void addFrameListener(Frame frame, FrameListener listener);
    /** add a listener for frame events for all frames */
    void addFrameListener(FrameListener listener);
    void removeFrameListener(Frame frame, FrameListener listener);
    void removeFrameListener(FrameListener listener);
    void addOwnSlotValue(Frame frame, Slot slot, Object value);
    Collection getDocumentation(Frame frame);
    String getName(Frame frame);
    boolean getOwnSlotAllowsMultipleValues(Frame frame, Slot slot);
    Collection getOwnSlotAndSubslotValues(Frame frame, Slot slot);
    Collection getOwnSlotDefaultValues(Frame frame, Slot slot);
    Collection getOwnSlotFacets(Frame frame, Slot slot);
    Collection getOwnSlotFacetValues(Frame frame, Slot slot, Facet facet);
    Collection<Slot> getOwnSlots(Frame frame);
    Collection getOwnSlotValues(Frame frame, Slot slot);
    Object getDirectOwnSlotValue(Frame frame, Slot slot);
    List getDirectOwnSlotValues(Frame frame, Slot slot);
    Object getOwnSlotValue(Frame frame, Slot slot);
    int getOwnSlotValueCount(Frame frame, Slot slot);
    ValueType getOwnSlotValueType(Frame frame, Slot slot);
    boolean hasOwnSlot(Frame frame, Slot slot);
    void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to);
    void removeOwnSlotValue(Frame frame, Slot slot, Object value);
    void setDocumentation(Frame frame, String text);
    void setDocumentation(Frame frame, Collection text);
    void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values);

    /** same as #setDirectOwnSlotValues(Frame, Slot, Collection) */
    void setOwnSlotValues(Frame frame, Slot slot, Collection values);
    void notifyVisibilityChanged(Frame frame);

    /** Add a listener for facet events for all facets */
    void addFacetListener(FacetListener listener);
    void removeFacetListener(FacetListener listener);
    /** Add a listener for facet events for a particular facet */
    void addFacetListener(Facet facet, FacetListener listener);
    void removeFacetListener(Facet facet, FacetListener listener);
    
    void addServerProjectListener(ServerProjectListener listener);
    void removeServerProjectListener(ServerProjectListener listener);
    
    Slot getAssociatedSlot(Facet facet);
    void setAssociatedSlot(Facet facet, Slot slot);
    

    /** Add a listener for class events for all classes */
    void addClsListener(ClsListener listener);
    /** Add a listener for class events for a particular class */
    void addClsListener(Cls cls, ClsListener listener);
    void removeClsListener(Cls cls, ClsListener listener);
    void removeClsListener(ClsListener listener);
    void addDirectSuperclass(Cls cls, Cls superclass);
    void removeDirectSuperclass(Cls cls, Cls superclass);
    void addDirectTemplateSlot(Cls cls, Slot slot);
    void removeDirectTemplateSlot(Cls cls, Slot slot);
    void addTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value);
    void addTemplateSlotValue(Cls cls, Slot slot, Object value);
    Slot getNameSlot();
    int getDirectInstanceCount(Cls cls);
    Collection<Instance> getDirectInstances(Cls cls);
    int getDirectSubclassCount(Cls cls);
    Collection getDirectSubclasses(Cls cls);
    int getDirectSuperclassCount(Cls cls);
    Collection<Cls> getDirectSuperclasses(Cls cls);
    List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet);
    Collection getDirectTemplateSlots(Cls cls);
    List getDirectTemplateSlotValues(Cls cls, Slot slot);
    int getInstanceCount(Cls cls);
    int getSimpleInstanceCount();
    Collection getSuperclasses(Cls cls);
    Collection getTemplateFacets(Cls cls, Slot slot);
    Object getTemplateFacetValue(Cls cls, Slot slot, Facet facet);
    Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet);
    Collection getTemplateSlotAllowedClses(Cls cls, Slot slot);
    Collection getTemplateSlotAllowedParents(Cls cls, Slot slot);
    Collection getTemplateSlotAllowedValues(Cls cls, Slot slot);
    boolean getTemplateSlotAllowsMultipleValues(Cls cls, Slot slot);
    Collection getTemplateSlotDefaultValues(Cls cls, Slot slot);
    Collection getTemplateSlotDocumentation(Cls cls, Slot slot);
    int getTemplateSlotMaximumCardinality(Cls cls, Slot slot);
    Number getTemplateSlotMaximumValue(Cls cls, Slot slot);
    int getTemplateSlotMinimumCardinality(Cls cls, Slot slot);
    Number getTemplateSlotMinimumValue(Cls cls, Slot slot);
    Collection getTemplateSlots(Cls cls);
    Object getTemplateSlotValue(Cls cls, Slot slot);
    Collection getTemplateSlotValues(Cls cls, Slot slot);
    ValueType getTemplateSlotValueType(Cls cls, Slot slot);
    boolean hasDirectlyOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet);
    boolean hasDirectlyOverriddenTemplateSlot(Cls cls, Slot slot);
    Collection getDirectlyOverriddenTemplateSlots(Cls cls);
    Collection getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot);
    boolean hasDirectSuperclass(Cls cls, Cls superclass);
    boolean hasDirectSuperslot(Slot slot, Slot superslot);
    boolean hasSuperslot(Slot slot, Slot superslot);
    void moveDirectSubslot(Slot slot, Slot subslot, Slot afterSlot);
    void moveDirectTemplateSlot(Cls cls, Slot slot, int toIndex);
    boolean hasDirectTemplateSlot(Cls cls, Slot slot);
    boolean hasInheritedTemplateSlot(Cls cls, Slot slot);
    boolean hasOverriddenTemplateSlot(Cls cls, Slot slot);
    boolean hasOverriddenTemplateFacet(Cls cls, Slot slot, Facet facet);
    boolean hasSuperclass(Cls cls, Cls superclass);
    boolean hasTemplateSlot(Cls cls, Slot slot);
    boolean isAbstract(Cls cls);
    boolean isMetaCls(Cls cls);
    void moveDirectSubclass(Cls cls, Cls subclass, Cls afterclass);
    void removeTemplateFacetOverrides(Cls cls, Slot slot);
    void setAbstract(Cls cls, boolean isAbstract);
    void setDirectTypeOfSubclasses(Cls cls, Cls type);
    void setTemplateFacetValue(Cls cls, Slot slot, Facet facet, Object value);
    void setTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values);
    void setTemplateSlotAllowedClses(Cls cls, Slot slot, Collection values);
    void setTemplateSlotAllowedParents(Cls cls, Slot slot, Collection values);
    void setTemplateSlotAllowedValues(Cls cls, Slot slot, Collection values);
    void setTemplateSlotAllowsMultipleValues(Cls cls, Slot slot, boolean allowsMultiple);
    void setTemplateSlotDefaultValues(Cls cls, Slot slot, Collection values);
    void setTemplateSlotDocumentation(Cls cls, Slot slot, String doc);
    void setTemplateSlotDocumentation(Cls cls, Slot slot, Collection docs);
    void setTemplateSlotMaximumCardinality(Cls cls, Slot slot, int value);
    void setTemplateSlotMaximumValue(Cls cls, Slot slot, Number value);
    void setTemplateSlotMinimumCardinality(Cls cls, Slot slot, int value);
    void setTemplateSlotMinimumValue(Cls cls, Slot slot, Number value);
    void setTemplateSlotValue(Cls cls, Slot slot, Object value);
    void setTemplateSlotValues(Cls cls, Slot slot, Collection value);
    void setTemplateSlotValueType(Cls cls, Slot slot, ValueType type);

    void addInstance(Instance instance, String name, Cls type, boolean isNew);
    void addInstanceListener(Instance instance, InstanceListener listener);
    void addInstanceListener(InstanceListener listener);
    void removeInstanceListener(Instance instance, InstanceListener listener);
    void removeInstanceListener(InstanceListener listener);
    String getBrowserText(Instance instance);
    Cls getDirectType(Instance instance);
    Collection getDirectTypes(Instance instance);
    boolean hasDirectType(Instance instance, Cls cls);
    boolean hasType(Instance instance, Cls cls);
    Instance setDirectType(Instance instance, Cls cls);
    Instance setDirectTypes(Instance instance, Collection types);

    void addDirectSuperslot(Slot slot, Slot superslot);
    void addSlotListener(Slot slot, SlotListener listener);
    void addSlotListener(SlotListener listener);
    void removeSlotListener(Slot slot, SlotListener listener);
    void removeSlotListener(SlotListener listener);
    Collection getAllowedClses(Slot slot);
    Collection getAllowedParents(Slot slot);
    Collection getAllowedValues(Slot slot);
    boolean getAllowsMultipleValues(Slot slot);
    Facet getAssociatedFacet(Slot slot);
    Collection getDefaultValues(Slot slot);
    int getDirectSubslotCount(Slot slot);
    Collection getDirectSubslots(Slot slot);
    Collection getDirectSuperslots(Slot slot);
    int getDirectSuperslotCount(Slot slot);
    Slot getInverseSlot(Slot slot);
    int getMaximumCardinality(Slot slot);
    Number getMaximumValue(Slot slot);
    int getMinimumCardinality(Slot slot);
    Number getMinimumValue(Slot slot);
    Collection getSubslots(Slot slot);
    Collection getSuperslots(Slot slot);
    Collection getDirectDomain(Slot slot);
    Collection getDomain(Slot slot);
    Collection getValues(Slot slot);
    ValueType getValueType(Slot slot);
    boolean hasSlotValueAtSomeFrame(Slot slot);
    void removeDirectSuperslot(Slot slot, Slot superslot);
    void setAllowedClses(Slot slot, Collection clses);
    void setAllowedParents(Slot slot, Collection parents);
    void setAllowedValues(Slot slot, Collection values);
    void setAllowsMultipleValues(Slot slot, boolean allowsMultiple);
    void setAssociatedFacet(Slot slot, Facet facet);
    void setDefaultValues(Slot slot, Collection values);
    void setDirectTypeOfSubslots(Slot slot, Cls type);
    void setInverseSlot(Slot slot, Slot inverseSlot);
    void setMaximumCardinality(Slot slot, int max);
    void setMaximumValue(Slot slot, Number max);
    void setMinimumCardinality(Slot slot, int max);
    void setMinimumValue(Slot slot, Number min);
    void setValues(Slot slot, Collection values);
    void setValueType(Slot slot, ValueType type);
    Collection getOverriddenTemplateFacets(Cls cls, Slot slot);

    Collection getCurrentUsers();

        
    /**
     * Tells the system that one or more edit actions will follow which should
     * be handled as a unit for undo.  Editing components should wrap set/add/remove
     * calls to any resource in a 
     * <CODE>beginTransaction() - commitTransaction() or rollbackTransaction()</CODE>
     * block.
     *
     * @param name the human-readable name of the following transaction
     * @return true
     * @see #endTransaction()
     */
    boolean beginTransaction(String name);
    
    boolean beginTransaction(String name, String appliedToFrameName);
    
    /**
     * @deprecated Use #commitTransaction()
     * Ends the recently opened transaction and commits the state.
     *  
     * @return 	true - if commit succeeds
     * 			false - otherwise
     * @see #beginTransaction
     */
    boolean endTransaction();
    
    
    /**
     * @deprecated Use #commitTransaction or #rollbackTransaction()
     * Ends the recently opened transaction and commits or rollback based on
     * doCommit value
     *  
     * @param doCommit 	true: commits transaction
     * 					false: rolls back transaction
     * @return true - if operation succeeded
     * 		   false - if operation failed
     */
    boolean endTransaction(boolean doCommit);
    
    /**
     * Commits the recently opened transaction
     * @return 	true - if commit suceeded
     * 			false - otherwise
     * @see #beginTransaction
     */
    boolean commitTransaction();
    
    
    /**
     * Rolls back the recently opened transaction
     * @return	true - if rollback succeeded
     * 			false - otherwise
     * @see #beginTransaction
     */
    boolean rollbackTransaction();

    
    void addDirectType(Instance instance, Cls directType);
    void removeDirectType(Instance instance, Cls directType);
    void moveDirectType(Instance instance, Cls directType, int index);

    CommandManager getCommandManager();

    void setFrameNameValidator(FrameNameValidator validator);
    boolean isValidFrameName(String s, Frame frame);
    String getInvalidFrameNameDescription(String s, Frame frame);

    void setDirectBrowserSlot(Cls cls, Slot slot);

    /**
     * Inserts a new frame store into the delegation chain.
     *
     * @param newFrameStore
     * @param position Position in the frame store list to insert this frame store.
     */
    void insertFrameStore(FrameStore newFrameStore, int position);

    /**
     * Inserts a new frame store into the front of the delegation chain.
     *
     * @param newFrameStore
     */
    void insertFrameStore(FrameStore newFrameStore);

    void removeFrameStore(FrameStore frameStore);

    /**
     * Returns a list of FrameStores available to the system.  This includes both enabled and disabled frame stores.
     * @return List of #FrameStore
     */
    List<FrameStore> getFrameStores();

    FrameFactory getFrameFactory();
    SystemFrames getSystemFrames();

    /**
     *
     */
    void clearAllListeners();

    FrameCounts getFrameCounts();

    void setDirectBrowserSlotPattern(Cls cls, BrowserSlotPattern pattern);
    BrowserSlotPattern getDirectBrowserSlotPattern(Cls cls);

    Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot);

    boolean setCallCachingEnabled(boolean enabled);
    boolean isCallCachingEnabled();

    boolean getValueChecking();

    void startJournaling(URI uri);
    void stopJournaling();

    void flushCache();
    void flushEvents() throws ProtegeException;

    Cls getReifiedRelationCls();
    Slot getReifedRelationFromSlot();
    Slot getReifedRelationToSlot();

    boolean isClosed();
    
    void addTransactionListener(TransactionListener listener);
    void removeTransactionListener(TransactionListener listener);
    
    public Collection<Frame> executeQuery(Query q);
    
    Frame rename(Frame frame, String name);
    void assertFrameName(Frame frame);
}
