package edu.stanford.smi.protege.model;

//ESCA*JAVA0037

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;

public class SystemFrames {

    private KnowledgeBase _kb;
    private Map<FrameID, Frame> _frameIdToFrameMap = new LinkedHashMap<FrameID, Frame>();

    public SystemFrames(KnowledgeBase kb) {
        _kb = kb;
        createFrames();
    }

    public Collection<Frame> getFrames() {
        return new ArrayList<Frame>(_frameIdToFrameMap.values());
    }

    public Collection<FrameID> getFrameIDs() {
        return new ArrayList<FrameID>(_frameIdToFrameMap.keySet());
    }

    public Collection<String> getFrameNames() {
      List<String> names = new ArrayList<String>();
      for (FrameID id : _frameIdToFrameMap.keySet()) {
        names.add(id.getName());
      }
      return names;
    }

    public synchronized Frame getFrame(FrameID id) {
        Frame frame = _frameIdToFrameMap.get(id);
        if (frame == null) {
            Log.getLogger().severe("Missing system frame: " + id);
        }
        return frame;
    }

    private Cls getCls(FrameID id) {
        return (Cls) getFrame(id);
    }

    private Slot getSlot(FrameID id) {
        return (Slot) getFrame(id);
    }

    private Facet getFacet(FrameID id) {
        return (Facet) getFrame(id);
    }

    private void createFrames() {
        createClses();
        createSlots();
        createFacets();
    }

    private void createCls(FrameID id) {
        Frame frame = new DefaultCls(_kb, id);
        addFrame(id, frame);
    }

    private void createSlot(FrameID id) {
        Frame frame = new DefaultSlot(_kb, id);
        addFrame(id, frame);
    }

    private void createFacet(FrameID id, FacetConstraint constraint) {
        Facet facet = new DefaultFacet(_kb, id);
        if (constraint != null) {
            facet.setConstraint(constraint);
        }
        addFrame(id, facet);
    }

    protected void addFrame(FrameID id, Frame frame) {
        Frame value = _frameIdToFrameMap.put(id, frame);
        if (value != null) {
            throw new IllegalStateException("duplicate add: " + id + " " + id.getName());
        }
    }
    
    protected void replaceFrame(FrameID id, Frame frame) {
    	Frame value = _frameIdToFrameMap.put(id, frame);
    	if (value == null) {
    		throw new IllegalStateException("illegal replace: " + id);
    	}
    }
    
    protected void removeFrame(FrameID id) {
        _frameIdToFrameMap.remove(id);
    }

    private void createClses() {
        createCls(Model.ClsID.THING);
        createCls(Model.ClsID.SYSTEM_CLASS);
        createCls(Model.ClsID.ROOT_META_CLASS);
        createCls(Model.ClsID.CLASS);
        createCls(Model.ClsID.SLOT);
        createCls(Model.ClsID.FACET);
        createCls(Model.ClsID.STANDARD_CLASS);
        createCls(Model.ClsID.STANDARD_SLOT);
        createCls(Model.ClsID.STANDARD_FACET);
        createCls(Model.ClsID.CONSTRAINT);
        createCls(Model.ClsID.RELATION);
        createCls(Model.ClsID.DIRECTED_BINARY_RELATION);
        createCls(Model.ClsID.PAL_CONSTRAINT);
        createCls(Model.ClsID.ANNOTATION);
        createCls(Model.ClsID.INSTANCE_ANNOTATION);
    }

    private void createSlots() {
        createSlot(Model.SlotID.DOCUMENTATION);
        createSlot(Model.SlotID.NAME);
        createSlot(Model.SlotID.ROLE);
        createSlot(Model.SlotID.DIRECT_TYPES);
        createSlot(Model.SlotID.DIRECT_INSTANCES);
        createSlot(Model.SlotID.DIRECT_SUPERCLASSES);
        createSlot(Model.SlotID.DIRECT_SUBCLASSES);
        createSlot(Model.SlotID.DIRECT_SUPERSLOTS);
        createSlot(Model.SlotID.DIRECT_SUBSLOTS);
        createSlot(Model.SlotID.ASSOCIATED_FACET);
        createSlot(Model.SlotID.ASSOCIATED_SLOT);
        createSlot(Model.SlotID.DIRECT_TEMPLATE_SLOTS);
        createSlot(Model.SlotID.DIRECT_DOMAIN);
        createSlot(Model.SlotID.INVERSE);
        createSlot(Model.SlotID.CONSTRAINTS);
        createSlot(Model.SlotID.DEFAULTS);
        createSlot(Model.SlotID.VALUE_TYPE);
        createSlot(Model.SlotID.MAXIMUM_CARDINALITY);
        createSlot(Model.SlotID.MINIMUM_CARDINALITY);
        createSlot(Model.SlotID.NUMERIC_MINIMUM);
        createSlot(Model.SlotID.NUMERIC_MAXIMUM);
        createSlot(Model.SlotID.PAL_STATEMENT);
        createSlot(Model.SlotID.PAL_DESCRIPTION);
        createSlot(Model.SlotID.PAL_NAME);
        createSlot(Model.SlotID.PAL_RANGE);
        createSlot(Model.SlotID.VALUES);
        createSlot(Model.SlotID.ANNOTATED_INSTANCE);
        createSlot(Model.SlotID.ANNOTATION_TEXT);
        createSlot(Model.SlotID.CREATOR);
        createSlot(Model.SlotID.CREATION_TIMESTAMP);
        createSlot(Model.SlotID.MODIFIER);
        createSlot(Model.SlotID.MODIFICATION_TIMESTAMP);
        createSlot(Model.SlotID.FROM);
        createSlot(Model.SlotID.TO);
    }

    private void createFacets() {
        createFacet(Model.FacetID.DOCUMENTATION, null);
        createFacet(Model.FacetID.DEFAULTS, new DefaultValuesConstraint());
        createFacet(Model.FacetID.CONSTRAINTS, null);
        createFacet(Model.FacetID.VALUE_TYPE, new ValueTypeConstraint());
        createFacet(Model.FacetID.INVERSE, null);
        createFacet(Model.FacetID.MAXIMUM_CARDINALITY, new MaximumCardinalityConstraint());
        createFacet(Model.FacetID.MINIMUM_CARDINALITY, new MinimumCardinalityConstraint());
        createFacet(Model.FacetID.NUMERIC_MINIMUM, new NumericMinimumConstraint());
        createFacet(Model.FacetID.NUMERIC_MAXIMUM, new NumericMaximumConstraint());
        createFacet(Model.FacetID.VALUES, null);
    }

    public Cls getRootCls() {
        return getCls(Model.ClsID.THING);
    }

    public Cls getRootMetaCls() {
        return getCls(Model.ClsID.ROOT_META_CLASS);
    }

    public Cls getRootClsMetaCls() {
        return getCls(Model.ClsID.CLASS);
    }

    public Cls getStandardClsMetaCls() {
        return getCls(Model.ClsID.STANDARD_CLASS);
    }

    public Cls getStandardSlotMetaCls() {
        return getCls(Model.ClsID.STANDARD_SLOT);
    }

    public Cls getRootSlotMetaCls() {
        return getCls(Model.ClsID.SLOT);
    }

    public Cls getRootFacetMetaCls() {
        return getCls(Model.ClsID.FACET);
    }

    public Cls getStandardFacetMetaCls() {
        return getCls(Model.ClsID.STANDARD_FACET);
    }

    public Cls getSystemCls() {
        return getCls(Model.ClsID.SYSTEM_CLASS);
    }

    public Cls getConstraintCls() {
        return getCls(Model.ClsID.CONSTRAINT);
    }

    public Cls getRelationCls() {
        return getCls(Model.ClsID.RELATION);
    }

    public Cls getDirectedBinaryRelationCls() {
        return getCls(Model.ClsID.DIRECTED_BINARY_RELATION);
    }

    public Cls getPalConstraintCls() {
        return getCls(Model.ClsID.PAL_CONSTRAINT);
    }

    public Cls getAnnotationCls() {
        return getCls(Model.ClsID.ANNOTATION);
    }

    public Cls getInstanceAnnotationCls() {
        return getCls(Model.ClsID.INSTANCE_ANNOTATION);
    }

    // Slots
    public Slot getDocumentationSlot() {
        return getSlot(Model.SlotID.DOCUMENTATION);
    }

    public Slot getDirectDomainSlot() {
        return getSlot(Model.SlotID.DIRECT_DOMAIN);
    }

    public Slot getNameSlot() {
        return getSlot(Model.SlotID.NAME);
    }

    public Slot getRoleSlot() {
        return getSlot(Model.SlotID.ROLE);
    }

    public Slot getDirectSuperclassesSlot() {
        return getSlot(Model.SlotID.DIRECT_SUPERCLASSES);
    }

    public Slot getDirectSubclassesSlot() {
        return getSlot(Model.SlotID.DIRECT_SUBCLASSES);
    }

    public Slot getDirectTypesSlot() {
        return getSlot(Model.SlotID.DIRECT_TYPES);
    }

    public Slot getDirectInstancesSlot() {
        return getSlot(Model.SlotID.DIRECT_INSTANCES);
    }

    public Slot getDirectTemplateSlotsSlot() {
        return getSlot(Model.SlotID.DIRECT_TEMPLATE_SLOTS);
    }

    public Slot getAssociatedFacetSlot() {
        return getSlot(Model.SlotID.ASSOCIATED_FACET);
    }

    public Slot getSlotConstraintsSlot() {
        return getSlot(Model.SlotID.CONSTRAINTS);
    }

    public Slot getDefaultValuesSlot() {
        return getSlot(Model.SlotID.DEFAULTS);
    }

    public Slot getValueTypeSlot() {
        return getSlot(Model.SlotID.VALUE_TYPE);
    }

    public Slot getInverseSlotSlot() {
        return getSlot(Model.SlotID.INVERSE);
    }

    public Slot getMaximumCardinalitySlot() {
        return getSlot(Model.SlotID.MAXIMUM_CARDINALITY);
    }

    public Slot getMinimumCardinalitySlot() {
        return getSlot(Model.SlotID.MINIMUM_CARDINALITY);
    }

    public Slot getMinimumValueSlot() {
        return getSlot(Model.SlotID.NUMERIC_MINIMUM);
    }

    public Slot getMaximumValueSlot() {
        return getSlot(Model.SlotID.NUMERIC_MAXIMUM);
    }

    public Slot getPalStatementSlot() {
        return getSlot(Model.SlotID.PAL_STATEMENT);
    }

    public Slot getPalNameSlot() {
        return getSlot(Model.SlotID.PAL_NAME);
    }

    public Slot getPalDescriptionSlot() {
        return getSlot(Model.SlotID.PAL_DESCRIPTION);
    }

    public Slot getPalRangeSlot() {
        return getSlot(Model.SlotID.PAL_RANGE);
    }

    public Slot getValuesSlot() {
        return getSlot(Model.SlotID.VALUES);
    }

    public Slot getDirectSubslotsSlot() {
        return getSlot(Model.SlotID.DIRECT_SUBSLOTS);
    }

    public Slot getDirectSuperslotsSlot() {
        return getSlot(Model.SlotID.DIRECT_SUPERSLOTS);
    }

    public Slot getAnnotatedInstanceSlot() {
        return getSlot(Model.SlotID.ANNOTATED_INSTANCE);
    }

    public Slot getAnnotationTextSlot() {
        return getSlot(Model.SlotID.ANNOTATION_TEXT);
    }

    public Slot getCreatorSlot() {
        return getSlot(Model.SlotID.CREATOR);
    }

    public Slot getCreationTimestampSlot() {
        return getSlot(Model.SlotID.CREATION_TIMESTAMP);
    }

    public Slot getAssociatedSlotSlot() {
        return getSlot(Model.SlotID.ASSOCIATED_SLOT);
    }

    public Slot getModifierSlot() {
        return getSlot(Model.SlotID.MODIFIER);
    }

    public Slot getModificationTimestampSlot() {
        return getSlot(Model.SlotID.MODIFICATION_TIMESTAMP);
    }

    public Slot getConstraintsSlot() {
        return getSlot(Model.SlotID.CONSTRAINTS);
    }

    public Slot getFromSlot() {
        return getSlot(Model.SlotID.FROM);
    }

    public Slot getToSlot() {
        return getSlot(Model.SlotID.TO);
    }

    // facets
    public Facet getDocumentationFacet() {
        return getFacet(Model.FacetID.DOCUMENTATION);
    }

    public Facet getDefaultValuesFacet() {
        return getFacet(Model.FacetID.DEFAULTS);
    }

    public Facet getConstraintsFacet() {
        return getFacet(Model.FacetID.CONSTRAINTS);
    }

    public Facet getValueTypeFacet() {
        return getFacet(Model.FacetID.VALUE_TYPE);
    }

    public Facet getInverseFacet() {
        return getFacet(Model.FacetID.INVERSE);
    }

    public Facet getMaximumCardinalityFacet() {
        return getFacet(Model.FacetID.MAXIMUM_CARDINALITY);
    }

    public Facet getMinimumCardinalityFacet() {
        return getFacet(Model.FacetID.MINIMUM_CARDINALITY);
    }

    public Facet getMinimumValueFacet() {
        return getFacet(Model.FacetID.NUMERIC_MINIMUM);
    }

    public Facet getMaximumValueFacet() {
        return getFacet(Model.FacetID.NUMERIC_MAXIMUM);
    }

    public Facet getValuesFacet() {
        return getFacet(Model.FacetID.VALUES);
    }
    
    /*
     * TODO This may change to provide a persistent way to represent the fact that a frame
     *      is a system frame.
     */
    public boolean isSystem(Frame frame) {
      return _frameIdToFrameMap.keySet().contains(frame.getFrameID());
    }

    public void addSystemFrames(FrameStore fs) {
        try {
            addFrames(fs);
            addInheritance(fs);
            addTemplateSlots(fs);
            addInverseSlots(fs);
            configureClses(fs);
            configureSlots(fs);
            configureFacets(fs);
            configureOverrides(fs);
        } catch (Exception e) {
            // can't happen
            Log.getLogger().severe(Log.toString(e));
            throw new RuntimeException(e.toString());
        }
    }

    private void addSystemCls(FrameStore fs, Cls cls) {
        Collection types = CollectionUtilities.createCollection(getStandardClsMetaCls());
        assertTypeAndName(fs, cls, types);
    }

    private void addSystemSlot(FrameStore fs, Slot slot) {
        Collection types = CollectionUtilities.createCollection(getStandardSlotMetaCls());
        assertTypeAndName(fs, slot, types);
    }

    private void addSystemFacet(FrameStore fs, Facet facet, FacetConstraint constraint) {
        Collection types = CollectionUtilities.createCollection(getStandardFacetMetaCls());
        assertTypeAndName(fs, facet, types);
        if (constraint != null) {
            facet.setConstraint(constraint);
        }
    }
    
    /*
     * Note that I don't use the more convenient FrameStore methods to set the type because
     * the knowledge base is not yet ready to start swizzling instances.
     */
    public void assertTypeAndName(FrameStore fs, Frame frame, Collection<Cls> types) {
        String name = frame.getFrameID().getName();
        fs.setDirectOwnSlotValues(frame, getNameSlot(), Collections.singleton(name));
        fs.setDirectOwnSlotValues(frame, getDirectTypesSlot(), types);
        for (Cls type : types) {
        	Collection framesOfType = new ArrayList(fs.getDirectOwnSlotValues(type, getDirectInstancesSlot()));
        	if (!framesOfType.contains(frame)) {
        		framesOfType.add(frame);
        		fs.setDirectOwnSlotValues(type, getDirectInstancesSlot(), framesOfType);
        	}
        }

    }


    private void addInverseSlot(FrameStore fs, Slot slota, Slot slotb) {
        setOwnSlotValue(fs, slota, getInverseSlotSlot(), slotb);
    }

    private static void addTemplateSlot(FrameStore fs, Cls cls, Slot slot) {
        fs.addDirectTemplateSlot(cls, slot);
    }

    private static void addSuperclass(FrameStore fs, Cls cls, Cls superclass) {
        fs.addDirectSuperclass(cls, superclass);
    }

    private void addFrames(FrameStore fs) {
        Iterator<Map.Entry<FrameID, Frame>> i = _frameIdToFrameMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<FrameID, Frame> entry = i.next();
            FrameID id = entry.getKey();
            Frame frame = entry.getValue();
            if (frame instanceof Cls) {
                addSystemCls(fs, (Cls) frame);
            } else if (frame instanceof Slot) {
                addSystemSlot(fs, (Slot) frame);
            } else if (frame instanceof Facet) {
                addSystemFacet(fs, (Facet) frame, ((Facet) frame).getConstraint());
            }
        }
    }

    private void addInheritance(FrameStore fs) {
        addSuperclass(fs, getSystemCls(), getRootCls());
        addSuperclass(fs, getRootMetaCls(), getSystemCls());
        addSuperclass(fs, getRootClsMetaCls(), getRootMetaCls());
        addSuperclass(fs, getStandardClsMetaCls(), getRootClsMetaCls());
        addSuperclass(fs, getRootSlotMetaCls(), getRootMetaCls());
        addSuperclass(fs, getStandardSlotMetaCls(), getRootSlotMetaCls());
        addSuperclass(fs, getRootFacetMetaCls(), getRootMetaCls());
        addSuperclass(fs, getStandardFacetMetaCls(), getRootFacetMetaCls());
        addSuperclass(fs, getConstraintCls(), getSystemCls());
        addSuperclass(fs, getPalConstraintCls(), getConstraintCls());
        addSuperclass(fs, getAnnotationCls(), getSystemCls());
        addSuperclass(fs, getInstanceAnnotationCls(), getAnnotationCls());
        addSuperclass(fs, getRelationCls(), getSystemCls());
        addSuperclass(fs, getDirectedBinaryRelationCls(), getRelationCls());
    }

    private void addTemplateSlots(FrameStore fs) {
        addRootMetaClsTemplateSlots(fs);
        addRootClsMetaClsTemplateSlots(fs);
        addStandardClsMetaClsTemplateSlots(fs);
        addRootSlotMetaClsTemplateSlots(fs);
        addStandardSlotMetaClsTemplateSlots(fs);
        addRootFacetMetaClsTemplateSlots(fs);
        addStandardFacetMetaClsTemplateSlots(fs);
        addPalConstraintTemplateSlots(fs);
        addInstanceAnnotationTemplateSlots(fs);
        addDirectedBinaryRelationTemplateSlots(fs);
    }

    private void addRootMetaClsTemplateSlots(FrameStore fs) {
        Cls cls = getRootMetaCls();
        addTemplateSlot(fs, cls, getNameSlot());
        addTemplateSlot(fs, cls, getDirectTypesSlot());
    }

    private void addRootClsMetaClsTemplateSlots(FrameStore fs) {
        Cls cls = getRootClsMetaCls();
        addTemplateSlot(fs, cls, getDirectInstancesSlot());
        addTemplateSlot(fs, cls, getDirectSuperclassesSlot());
        addTemplateSlot(fs, cls, getDirectSubclassesSlot());
        addTemplateSlot(fs, cls, getDirectTemplateSlotsSlot());
    }

    private void addStandardClsMetaClsTemplateSlots(FrameStore fs) {
        Cls cls = getStandardClsMetaCls();
        addTemplateSlot(fs, cls, getRoleSlot());
        addTemplateSlot(fs, cls, getDocumentationSlot());
        addTemplateSlot(fs, cls, getConstraintsSlot());
    }

    private void addRootSlotMetaClsTemplateSlots(FrameStore fs) {
        Cls cls = getRootSlotMetaCls();
        addTemplateSlot(fs, cls, getDirectDomainSlot());
        addTemplateSlot(fs, cls, getValueTypeSlot());
    }

    private void addStandardSlotMetaClsTemplateSlots(FrameStore fs) {
        Cls cls = getStandardSlotMetaCls();
        addTemplateSlot(fs, cls, getDocumentationSlot());
        addTemplateSlot(fs, cls, getConstraintsSlot());
        addTemplateSlot(fs, cls, getMaximumCardinalitySlot());
        addTemplateSlot(fs, cls, getMinimumCardinalitySlot());
        addTemplateSlot(fs, cls, getMaximumValueSlot());
        addTemplateSlot(fs, cls, getMinimumValueSlot());
        addTemplateSlot(fs, cls, getInverseSlotSlot());
        addTemplateSlot(fs, cls, getDefaultValuesSlot());
        addTemplateSlot(fs, cls, getValuesSlot());
        addTemplateSlot(fs, cls, getAssociatedFacetSlot());
        addTemplateSlot(fs, cls, getDirectSubslotsSlot());
        addTemplateSlot(fs, cls, getDirectSuperslotsSlot());
    }

    private void addRootFacetMetaClsTemplateSlots(FrameStore fs) {
        // Cls cls = getRootFacetMetaCls();
        // no slots
    }

    private void addStandardFacetMetaClsTemplateSlots(FrameStore fs) {
        Cls cls = getStandardFacetMetaCls();
        addTemplateSlot(fs, cls, getDocumentationSlot());
        addTemplateSlot(fs, cls, getAssociatedSlotSlot());
    }

    private void addPalConstraintTemplateSlots(FrameStore fs) {
        Cls cls = getPalConstraintCls();
        addTemplateSlot(fs, cls, getPalNameSlot());
        addTemplateSlot(fs, cls, getPalRangeSlot());
        addTemplateSlot(fs, cls, getPalDescriptionSlot());
        addTemplateSlot(fs, cls, getPalStatementSlot());
    }

    private void addInstanceAnnotationTemplateSlots(FrameStore fs) {
        Cls cls = getInstanceAnnotationCls();
        addTemplateSlot(fs, cls, getAnnotatedInstanceSlot());
        addTemplateSlot(fs, cls, getAnnotationTextSlot());
        addTemplateSlot(fs, cls, getCreatorSlot());
        addTemplateSlot(fs, cls, getCreationTimestampSlot());
    }

    private void addDirectedBinaryRelationTemplateSlots(FrameStore fs) {
        Cls cls = getDirectedBinaryRelationCls();
        addTemplateSlot(fs, cls, getFromSlot());
        addTemplateSlot(fs, cls, getToSlot());
    }

    private void addInverseSlots(FrameStore fs) {
        addInverseSlot(fs, getInverseSlotSlot(), getInverseSlotSlot());
        addInverseSlot(fs, getAssociatedFacetSlot(), getAssociatedSlotSlot());
        addInverseSlot(fs, getDirectTemplateSlotsSlot(), getDirectDomainSlot());
        addInverseSlot(fs, getDirectSuperclassesSlot(), getDirectSubclassesSlot());
        addInverseSlot(fs, getDirectTypesSlot(), getDirectInstancesSlot());
        addInverseSlot(fs, getDirectSuperslotsSlot(), getDirectSubslotsSlot());
    }

    private void configureClses(FrameStore fs) {
        configureCls(fs, getRootCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getSystemCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getRootMetaCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getRootClsMetaCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getStandardClsMetaCls(), RoleConstraint.CONCRETE);
        configureCls(fs, getRootSlotMetaCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getStandardSlotMetaCls(), RoleConstraint.CONCRETE);
        configureCls(fs, getRootFacetMetaCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getStandardFacetMetaCls(), RoleConstraint.CONCRETE);
        configureCls(fs, getConstraintCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getPalConstraintCls(), RoleConstraint.CONCRETE);
        configureCls(fs, getAnnotationCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getInstanceAnnotationCls(), RoleConstraint.CONCRETE);
        configureCls(fs, getRelationCls(), RoleConstraint.ABSTRACT);
        configureCls(fs, getDirectedBinaryRelationCls(), RoleConstraint.CONCRETE);
    }

    private void configureCls(FrameStore fs, Cls cls, String role) {
        setOwnSlotValue(fs, cls, getRoleSlot(), role);
    }

    private void configureSlots(FrameStore fs) {
        configureMultiStringSlot(fs, getDocumentationSlot());
        configureSingleStringSlot(fs, getNameSlot());
        configureSingleSymbolSlot(fs, getRoleSlot(), RoleConstraint.getValues());
        configureMultiClassSlot(fs, getDirectTypesSlot(), getRootCls());
        configureMultiInstanceSlot(fs, getDirectInstancesSlot(), getRootCls());
        configureMultiClassSlot(fs, getDirectSuperclassesSlot(), getRootCls());
        configureMultiClassSlot(fs, getDirectSubclassesSlot(), getRootCls());
        configureMultiInstanceSlot(fs, getDirectSuperslotsSlot(), getRootSlotMetaCls());
        configureMultiInstanceSlot(fs, getDirectSubslotsSlot(), getRootSlotMetaCls());
        configureSingleInstanceSlot(fs, getAssociatedFacetSlot(), getRootFacetMetaCls());
        configureSingleInstanceSlot(fs, getAssociatedSlotSlot(), getRootSlotMetaCls());
        configureMultiInstanceSlot(fs, getDirectTemplateSlotsSlot(), getRootSlotMetaCls());
        configureMultiInstanceSlot(fs, getDirectDomainSlot(), getRootClsMetaCls());
        configureSingleInstanceSlot(fs, getInverseSlotSlot(), getRootSlotMetaCls());
        configureMultiInstanceSlot(fs, getConstraintsSlot(), getConstraintCls());
        configureMultiAnySlot(fs, getDefaultValuesSlot());
        configureMultiAnySlot(fs, getValueTypeSlot());
        configureSingleIntSlot(fs, getMaximumCardinalitySlot());
        configureSingleIntSlot(fs, getMinimumCardinalitySlot());
        configureSingleFloatSlot(fs, getMinimumValueSlot());
        configureSingleFloatSlot(fs, getMaximumValueSlot());
        configureSingleStringSlot(fs, getPalStatementSlot());
        configureSingleStringSlot(fs, getPalDescriptionSlot());
        configureSingleStringSlot(fs, getPalNameSlot());
        configureSingleStringSlot(fs, getPalRangeSlot());
        configureMultiAnySlot(fs, getValuesSlot());
        configureSingleInstanceSlot(fs, getAnnotatedInstanceSlot(), getRootCls());
        configureSingleStringSlot(fs, getAnnotationTextSlot());
        configureSingleStringSlot(fs, getCreatorSlot());
        configureSingleStringSlot(fs, getCreationTimestampSlot());
        configureSingleStringSlot(fs, getModifierSlot());
        configureSingleStringSlot(fs, getModificationTimestampSlot());
        configureSingleInstanceSlot(fs, getFromSlot(), getRootCls());
        configureSingleInstanceSlot(fs, getToSlot(), getRootCls());

        setDefault(fs, getMaximumCardinalitySlot(), new Integer(1));
        setDefault(fs, getValueTypeSlot(), ValueType.STRING.toString());
        setDefault(fs, getRoleSlot(), RoleConstraint.CONCRETE.toString());
    }

    private void configureOverrides(FrameStore fs) {
        setAllowedParent(fs, getRootMetaCls(), getDirectTypesSlot(), getRootMetaCls());
        setAllowedParent(fs, getRootClsMetaCls(), getDirectTypesSlot(), getRootClsMetaCls());
        setAllowedParent(fs, getRootSlotMetaCls(), getDirectTypesSlot(), getRootSlotMetaCls());
        setAllowedParent(fs, getRootFacetMetaCls(), getDirectTypesSlot(), getRootFacetMetaCls());
    }

    private void setAllowedParent(FrameStore fs, Cls cls, Slot slot, Cls parent) {
        Collection values = ValueTypeConstraint.getValues(ValueType.CLS, Collections.singleton(parent));
        fs.setDirectTemplateFacetValues(cls, slot, getValueTypeFacet(), values);
    }

    private void configureFacets(FrameStore fs) {
        configureFacet(fs, getDocumentationFacet(), getDocumentationSlot());
        configureFacet(fs, getDefaultValuesFacet(), getDefaultValuesSlot());
        configureFacet(fs, getConstraintsFacet(), getConstraintsSlot());
        configureFacet(fs, getValueTypeFacet(), getValueTypeSlot());
        configureFacet(fs, getMaximumCardinalityFacet(), getMaximumCardinalitySlot());
        configureFacet(fs, getMinimumCardinalityFacet(), getMinimumCardinalitySlot());
        configureFacet(fs, getMaximumValueFacet(), getMaximumValueSlot());
        configureFacet(fs, getMinimumValueFacet(), getMinimumValueSlot());
        configureFacet(fs, getValuesFacet(), getValuesSlot());
    }

    private void setDefault(FrameStore fs, Slot slot, Object o) {
        setOwnSlotValue(fs, slot, getDefaultValuesSlot(), o);
    }

    private void setCardinality(FrameStore fs, Slot slot, boolean multiple) {
        Collection values = MaximumCardinalityConstraint.getValues(multiple);
        setOwnSlotValues(fs, slot, getMaximumCardinalitySlot(), values);
    }

    private void setValueType(FrameStore fs, Slot slot, ValueType type) {
        setValueType(fs, slot, type, Collections.EMPTY_LIST);
    }

    private void setValueType(FrameStore fs, Slot slot, ValueType type, Collection constraints) {
        Collection values = ValueTypeConstraint.getValues(type, constraints);
        setOwnSlotValues(fs, slot, getValueTypeSlot(), values);
    }

    private void configureMultiStringSlot(FrameStore fs, Slot slot) {
        setCardinality(fs, slot, true);
        setValueType(fs, slot, ValueType.STRING);
    }

    private void configureSingleStringSlot(FrameStore fs, Slot slot) {
        setCardinality(fs, slot, false);
        setValueType(fs, slot, ValueType.STRING);
    }

    private void configureSingleSymbolSlot(FrameStore fs, Slot slot, Collection allowedValues) {
        setCardinality(fs, slot, false);
        setValueType(fs, slot, ValueType.SYMBOL, allowedValues);
    }

    private void configureMultiInstanceSlot(FrameStore fs, Slot slot, Cls cls) {
        setCardinality(fs, slot, true);
        setValueType(fs, slot, ValueType.INSTANCE, CollectionUtilities.createCollection(cls));
    }

    private void configureMultiClassSlot(FrameStore fs, Slot slot, Cls cls) {
        setCardinality(fs, slot, true);
        setValueType(fs, slot, ValueType.CLS, CollectionUtilities.createCollection(cls));
    }

    private void configureSingleInstanceSlot(FrameStore fs, Slot slot, Cls cls) {
        setCardinality(fs, slot, false);
        setValueType(fs, slot, ValueType.INSTANCE, CollectionUtilities.createCollection(cls));
    }

    private void configureSingleIntSlot(FrameStore fs, Slot slot) {
        setCardinality(fs, slot, false);
        setValueType(fs, slot, ValueType.INTEGER);
    }

    private void configureSingleFloatSlot(FrameStore fs, Slot slot) {
        setCardinality(fs, slot, false);
        setValueType(fs, slot, ValueType.FLOAT);
    }

    private void configureMultiAnySlot(FrameStore fs, Slot slot) {
        setCardinality(fs, slot, true);
        setValueType(fs, slot, ValueType.ANY);
    }

    private static void setOwnSlotValue(FrameStore fs, Frame frame, Slot slot, Object value) {
        Collection values = Collections.singleton(value);
        setOwnSlotValues(fs, frame, slot, values);
    }

    private static void setOwnSlotValues(FrameStore fs, Frame frame, Slot slot, Collection values) {
        fs.setDirectOwnSlotValues(frame, slot, values);
    }

    private void configureFacet(FrameStore fs, Facet facet, Slot slot) {
        setOwnSlotValue(fs, facet, getAssociatedSlotSlot(), slot);
    }
}
