package edu.stanford.smi.protege.model;

//ESCA*JAVA0037

import java.util.*;

import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

public class SystemFrames {

    private KnowledgeBase _kb;
    private Map _frameIdToFrameMap = new LinkedHashMap();
    private Map _frameIdToNameMap = new LinkedHashMap();

    public SystemFrames(KnowledgeBase kb) {
        _kb = kb;
        createFrames();
    }

    public void replaceFrame(Frame frame) {
        _frameIdToFrameMap.put(frame.getFrameID(), frame);
    }

    public Collection getFrames() {
        return new ArrayList(_frameIdToFrameMap.values());
    }

    public Collection getFrameIDs() {
        return new ArrayList(_frameIdToFrameMap.keySet());
    }

    public Collection getFrameNames() {
        return new ArrayList(_frameIdToNameMap.values());
    }

    public Frame getFrame(FrameID id) {
        Frame frame = (Frame) _frameIdToFrameMap.get(id);
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

    private void createCls(FrameID id, String name) {
        Frame frame = new DefaultCls(_kb, id);
        addFrame(id, name, frame);
    }

    private void createSlot(FrameID id, String name) {
        Frame frame = new DefaultSlot(_kb, id);
        addFrame(id, name, frame);
    }

    private void createFacet(FrameID id, String name, FacetConstraint constraint) {
        Facet facet = new DefaultFacet(_kb, id);
        if (constraint != null) {
            facet.setConstraint(constraint);
        }
        addFrame(id, name, facet);
    }

    private void addFrame(FrameID id, String name, Frame frame) {
        Object value = _frameIdToFrameMap.put(id, frame);
        if (value != null) {
            throw new IllegalStateException("duplicate add: " + id + " " + name);
        }
        _frameIdToNameMap.put(id, name);
    }

    private void createClses() {
        createCls(Model.ClsID.THING, Model.Cls.THING);
        createCls(Model.ClsID.SYSTEM_CLASS, Model.Cls.SYSTEM_CLASS);
        createCls(Model.ClsID.ROOT_META_CLASS, Model.Cls.ROOT_META_CLASS);
        createCls(Model.ClsID.CLASS, Model.Cls.CLASS);
        createCls(Model.ClsID.SLOT, Model.Cls.SLOT);
        createCls(Model.ClsID.FACET, Model.Cls.FACET);
        createCls(Model.ClsID.STANDARD_CLASS, Model.Cls.STANDARD_CLASS);
        createCls(Model.ClsID.STANDARD_SLOT, Model.Cls.STANDARD_SLOT);
        createCls(Model.ClsID.STANDARD_FACET, Model.Cls.STANDARD_FACET);
        createCls(Model.ClsID.CONSTRAINT, Model.Cls.CONSTRAINT);
        createCls(Model.ClsID.RELATION, Model.Cls.RELATION);
        createCls(Model.ClsID.DIRECTED_BINARY_RELATION, Model.Cls.DIRECTED_BINARY_RELATION);
        createCls(Model.ClsID.PAL_CONSTRAINT, Model.Cls.PAL_CONSTRAINT);
        createCls(Model.ClsID.ANNOTATION, Model.Cls.ANNOTATION);
        createCls(Model.ClsID.INSTANCE_ANNOTATION, Model.Cls.INSTANCE_ANNOTATION);
    }

    private void createSlots() {
        createSlot(Model.SlotID.DOCUMENTATION, Model.Slot.DOCUMENTATION);
        createSlot(Model.SlotID.NAME, Model.Slot.NAME);
        createSlot(Model.SlotID.ROLE, Model.Slot.ROLE);
        createSlot(Model.SlotID.DIRECT_TYPES, Model.Slot.DIRECT_TYPES);
        createSlot(Model.SlotID.DIRECT_INSTANCES, Model.Slot.DIRECT_INSTANCES);
        createSlot(Model.SlotID.DIRECT_SUPERCLASSES, Model.Slot.DIRECT_SUPERCLASSES);
        createSlot(Model.SlotID.DIRECT_SUBCLASSES, Model.Slot.DIRECT_SUBCLASSES);
        createSlot(Model.SlotID.DIRECT_SUPERSLOTS, Model.Slot.DIRECT_SUPERSLOTS);
        createSlot(Model.SlotID.DIRECT_SUBSLOTS, Model.Slot.DIRECT_SUBSLOTS);
        createSlot(Model.SlotID.ASSOCIATED_FACET, Model.Slot.ASSOCIATED_FACET);
        createSlot(Model.SlotID.ASSOCIATED_SLOT, Model.Slot.ASSOCIATED_SLOT);
        createSlot(Model.SlotID.DIRECT_TEMPLATE_SLOTS, Model.Slot.DIRECT_TEMPLATE_SLOTS);
        createSlot(Model.SlotID.DIRECT_DOMAIN, Model.Slot.DIRECT_DOMAIN);
        createSlot(Model.SlotID.INVERSE, Model.Slot.INVERSE);
        createSlot(Model.SlotID.CONSTRAINTS, Model.Slot.CONSTRAINTS);
        createSlot(Model.SlotID.DEFAULTS, Model.Slot.DEFAULTS);
        createSlot(Model.SlotID.VALUE_TYPE, Model.Slot.VALUE_TYPE);
        createSlot(Model.SlotID.MAXIMUM_CARDINALITY, Model.Slot.MAXIMUM_CARDINALITY);
        createSlot(Model.SlotID.MINIMUM_CARDINALITY, Model.Slot.MINIMUM_CARDINALITY);
        createSlot(Model.SlotID.NUMERIC_MINIMUM, Model.Slot.NUMERIC_MINIMUM);
        createSlot(Model.SlotID.NUMERIC_MAXIMUM, Model.Slot.NUMERIC_MAXIMUM);
        createSlot(Model.SlotID.PAL_STATEMENT, Model.Slot.PAL_STATEMENT);
        createSlot(Model.SlotID.PAL_DESCRIPTION, Model.Slot.PAL_DESCRIPTION);
        createSlot(Model.SlotID.PAL_NAME, Model.Slot.PAL_NAME);
        createSlot(Model.SlotID.PAL_RANGE, Model.Slot.PAL_RANGE);
        createSlot(Model.SlotID.VALUES, Model.Slot.VALUES);
        createSlot(Model.SlotID.ANNOTATED_INSTANCE, Model.Slot.ANNOTATED_INSTANCE);
        createSlot(Model.SlotID.ANNOTATION_TEXT, Model.Slot.ANNOTATION_TEXT);
        createSlot(Model.SlotID.CREATOR, Model.Slot.CREATOR);
        createSlot(Model.SlotID.CREATION_TIMESTAMP, Model.Slot.CREATION_TIMESTAMP);
        createSlot(Model.SlotID.MODIFIER, Model.Slot.MODIFIER);
        createSlot(Model.SlotID.MODIFICATION_TIMESTAMP, Model.Slot.MODIFICATION_TIMESTAMP);
        createSlot(Model.SlotID.FROM, Model.Slot.FROM);
        createSlot(Model.SlotID.TO, Model.Slot.TO);
    }

    private void createFacets() {
        createFacet(Model.FacetID.DOCUMENTATION, Model.Facet.DOCUMENTATION, null);
        createFacet(Model.FacetID.DEFAULTS, Model.Facet.DEFAULTS, new DefaultValuesConstraint());
        createFacet(Model.FacetID.CONSTRAINTS, Model.Facet.CONSTRAINTS, null);
        createFacet(Model.FacetID.VALUE_TYPE, Model.Facet.VALUE_TYPE, new ValueTypeConstraint());
        createFacet(Model.FacetID.INVERSE, Model.Facet.INVERSE, null);
        createFacet(Model.FacetID.MAXIMUM_CARDINALITY, Model.Facet.MAXIMUM_CARDINALITY,
                new MaximumCardinalityConstraint());
        createFacet(Model.FacetID.MINIMUM_CARDINALITY, Model.Facet.MINIMUM_CARDINALITY,
                new MinimumCardinalityConstraint());
        createFacet(Model.FacetID.NUMERIC_MINIMUM, Model.Facet.NUMERIC_MINIMUM, new NumericMinimumConstraint());
        createFacet(Model.FacetID.NUMERIC_MAXIMUM, Model.Facet.NUMERIC_MAXIMUM, new NumericMaximumConstraint());
        createFacet(Model.FacetID.VALUES, Model.Facet.VALUES, null);
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

    private Cls addSystemCls(FrameStore fs, FrameID id, String name) {
        Collection types = CollectionUtilities.createCollection(getStandardClsMetaCls());
        return fs.createCls(id, name, types, Collections.EMPTY_SET, false);
    }

    private void addSystemSlot(FrameStore fs, FrameID id, String name) {
        Collection types = CollectionUtilities.createCollection(getStandardSlotMetaCls());
        fs.createSlot(id, name, types, Collections.EMPTY_SET, false);
    }

    private void addSystemFacet(FrameStore fs, FrameID id, String name, FacetConstraint constraint) {
        Collection types = CollectionUtilities.createCollection(getStandardFacetMetaCls());
        Facet facet = fs.createFacet(id, name, types, false);
        if (constraint != null) {
            facet.setConstraint(constraint);
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
        Iterator i = _frameIdToFrameMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            FrameID id = (FrameID) entry.getKey();
            Frame frame = (Frame) entry.getValue();
            String name = (String) _frameIdToNameMap.get(id);
            if (frame instanceof Cls) {
                addSystemCls(fs, id, name);
            } else if (frame instanceof Slot) {
                addSystemSlot(fs, id, name);
            } else if (frame instanceof Facet) {
                addSystemFacet(fs, id, name, ((Facet) frame).getConstraint());
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
