package edu.stanford.smi.protege.model;
//ESCA*JAVA0257

/**
 * System Class, Slot and Facet names and ids
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Model {

    /**
     * system class names
     * 
     * @author Ray Fergerson <fergerson@smi.stanford.edu>
     */
    interface Cls {
        String THING = ":THING";
        String CLASS = ":CLASS";
        String STANDARD_CLASS = ":STANDARD-CLASS";
        String SLOT = ":SLOT";
        String STANDARD_SLOT = ":STANDARD-SLOT";
        String FACET = ":FACET";
        String STANDARD_FACET = ":STANDARD-FACET";

        // String INDIVIDUAL = ":INDIVIDUAL";
        // String NUMBER = ":NUMBER";
        String INTEGER = ":INTEGER";
        String FLOAT = ":FLOAT";
        String STRING = ":STRING";
        String SYMBOL = ":SYMBOL";
        String BOOLEAN = ":BOOLEAN";

        String PRIMITIVE_TYPE = ":PRIMITIVE-TYPE";
        String SYSTEM_CLASS = ":SYSTEM-CLASS";
        String CONSTRAINT = ":CONSTRAINT";
        String RELATION = ":RELATION";

        String PAL_CONSTRAINT = ":PAL-CONSTRAINT";

        String ANNOTATION = ":ANNOTATION";
        String INSTANCE_ANNOTATION = ":INSTANCE-ANNOTATION";
        String DIRECTED_BINARY_RELATION = ":DIRECTED-BINARY-RELATION";

        String ROOT_META_CLASS = ":META-CLASS";
    }

    /**
     * System class frame ids, one for each name. These ids can never be
     * changed.
     * 
     * @author Ray Fergerson <fergerson@smi.stanford.edu>
     */
    interface ClsID {
      FrameID THING = new FrameID(Cls.THING, true);
      FrameID CLASS = new FrameID(Cls.CLASS, true);
      FrameID STANDARD_CLASS = new FrameID(Cls.STANDARD_CLASS, true);
      FrameID SLOT = new FrameID(Cls.SLOT, true);
      FrameID STANDARD_SLOT = new FrameID(Cls.STANDARD_SLOT, true);
      FrameID FACET = new FrameID(Cls.FACET, true);
      FrameID STANDARD_FACET = new FrameID(Cls.STANDARD_FACET, true);

      // FrameID INDIVIDUAL = FrameID.createSystem(BASE_ID + 7);
      // FrameID NUMBER = FrameID.createSystem(BASE_ID + 8);
      FrameID INTEGER = new FrameID(Cls.INTEGER, true);
      FrameID FLOAT = new FrameID(Cls.FLOAT, true);
      FrameID STRING = new FrameID(Cls.STRING, true);
      FrameID SYMBOL = new FrameID(Cls.SYMBOL, true);
      FrameID BOOLEAN = new FrameID(Cls.BOOLEAN, true);

      FrameID PRIMITIVE_TYPE = new FrameID(Cls.PRIMITIVE_TYPE, true);
      FrameID SYSTEM_CLASS = new FrameID(Cls.SYSTEM_CLASS, true);
      FrameID CONSTRAINT = new FrameID(Cls.CONSTRAINT, true);
      FrameID RELATION = new FrameID(Cls.RELATION, true);

      FrameID PAL_CONSTRAINT = new FrameID(Cls.PAL_CONSTRAINT, true);

      FrameID ANNOTATION = new FrameID(Cls.ANNOTATION, true);
      FrameID INSTANCE_ANNOTATION = new FrameID(Cls.INSTANCE_ANNOTATION, true);
      FrameID DIRECTED_BINARY_RELATION = new FrameID(Cls.DIRECTED_BINARY_RELATION, true);

      FrameID ROOT_META_CLASS = new FrameID(Cls.ROOT_META_CLASS, true);
    }

    /**
     * System slot names
     * 
     * @author Ray Fergerson <fergerson@smi.stanford.edu>
     */
    interface Slot {
        String DOCUMENTATION = ":DOCUMENTATION";
        String DOMAIN = ":DOMAIN";

        String NAME = ":NAME";
        String ROLE = ":ROLE";
        String DIRECT_SUPERCLASSES = ":DIRECT-SUPERCLASSES";
        String DIRECT_SUBCLASSES = ":DIRECT-SUBCLASSES";
        String DIRECT_TYPES = ":DIRECT-TYPE";
        /**
         * @deprecated Use #DIRECT_TYPES
         */
        String DIRECT_TYPE = DIRECT_TYPES;
        String DIRECT_INSTANCES = ":DIRECT-INSTANCES";
        String DIRECT_TEMPLATE_SLOTS = ":DIRECT-TEMPLATE-SLOTS";
        // String DIRECT_BROWSER_SLOT = ":DIRECT-BROWSER-SLOT";

        // String OWN_SLOTS = ":OWN-SLOTS";
        String ASSOCIATED_FACET = ":ASSOCIATED-FACET";

        String CONSTRAINTS = ":SLOT-CONSTRAINTS";
        String DEFAULTS = ":SLOT-DEFAULTS";
        String VALUE_TYPE = ":SLOT-VALUE-TYPE";
        String INVERSE = ":SLOT-INVERSE";
        String CARDINALITY = ":SLOT-CARDINALITY";
        String MAXIMUM_CARDINALITY = ":SLOT-MAXIMUM-CARDINALITY";
        String MINIMUM_CARDINALITY = ":SLOT-MINIMUM-CARDINALITY";
        String SAME_VALUES = ":SLOT-SAME-VALUES";
        String NOT_SAME_VALUES = ":SLOT-NOT-SAME-VALUES";
        String SUBSET_OF_VALUES = ":SLOT-SUBSET-OF-VALUES";
        String NUMERIC_MINIMUM = ":SLOT-NUMERIC-MINIMUM";
        String NUMERIC_MAXIMUM = ":SLOT-NUMERIC-MAXIMUM";
        String SOME_VALUES = ":SLOT-SOME-VALUES";
        String COLLECTION_TYPE = ":SLOT-COLLECTION-TYPE";

        String PAL_STATEMENT = ":PAL-STATEMENT";
        String PAL_DESCRIPTION = ":PAL-DESCRIPTION";
        String PAL_NAME = ":PAL-NAME";
        String PAL_RANGE = ":PAL-RANGE";

        String VALUES = ":SLOT-VALUES";

        String DIRECT_SUBSLOTS = ":DIRECT-SUBSLOTS";
        String DIRECT_SUPERSLOTS = ":DIRECT-SUPERSLOTS";

        String ANNOTATED_INSTANCE = ":ANNOTATED-INSTANCE";
        String ANNOTATION_TEXT = ":ANNOTATION-TEXT";
        String CREATOR = ":CREATOR";
        String CREATION_TIMESTAMP = ":CREATION-TIMESTAMP";

        String ASSOCIATED_SLOT = ":ASSOCIATED-SLOT";
        String MODIFIER = ":MODIFIER";
        String MODIFICATION_TIMESTAMP = ":MODIFICATION-TIMESTAMP";

        String FROM = ":FROM";
        String TO = ":TO";

        String DIRECT_DOMAIN = ":DIRECT-DOMAIN";
        /**
         * @deprecated Use #DIRECT_DOMAIN
         */
        String DIRECT_ATTACHED_CLASSES = DIRECT_DOMAIN;
    }

    /**
     * FrameID's for system slots
     * 
     * @author Ray Fergerson <fergerson@smi.stanford.edu>
     */
    interface SlotID {
      FrameID DOCUMENTATION = new FrameID(Slot.DOCUMENTATION, true);
      FrameID DOMAIN = new FrameID(Slot.DOMAIN, true);

      FrameID NAME = new FrameID(Slot.NAME, true);
      FrameID ROLE = new FrameID(Slot.ROLE, true);
      FrameID DIRECT_SUPERCLASSES = new FrameID(Slot.DIRECT_SUPERCLASSES, true);
      FrameID DIRECT_SUBCLASSES = new FrameID(Slot.DIRECT_SUBCLASSES, true);
      FrameID DIRECT_TYPES = new FrameID(Slot.DIRECT_TYPES, true);
        /**
         * @deprecated Use #DIRECT_TYPES
         */
      FrameID DIRECT_TYPE = new FrameID(Slot.DIRECT_TYPE, true);
      FrameID DIRECT_INSTANCES = new FrameID(Slot.DIRECT_INSTANCES, true);
      FrameID DIRECT_TEMPLATE_SLOTS = new FrameID(Slot.DIRECT_TEMPLATE_SLOTS, true);
        // FrameID DIRECT_BROWSER_SLOT = FrameID.createSystem(BASE_ID + 9);

        // FrameID OWN_SLOTS = FrameID.createSystem(BASE_ID + 10);
      FrameID ASSOCIATED_FACET = new FrameID(Slot.ASSOCIATED_FACET, true);

      FrameID CONSTRAINTS = new FrameID(Slot.CONSTRAINTS, true);
      FrameID DEFAULTS = new FrameID(Slot.DEFAULTS, true);
      FrameID VALUE_TYPE = new FrameID(Slot.VALUE_TYPE, true);
      FrameID INVERSE = new FrameID(Slot.INVERSE, true);
      FrameID CARDINALITY = new FrameID(Slot.CARDINALITY, true);
      FrameID MAXIMUM_CARDINALITY = new FrameID(Slot.MAXIMUM_CARDINALITY, true);
      FrameID MINIMUM_CARDINALITY = new FrameID(Slot.MINIMUM_CARDINALITY, true);
      FrameID SAME_VALUES = new FrameID(Slot.SAME_VALUES, true);
      FrameID NOT_SAME_VALUES = new FrameID(Slot.NOT_SAME_VALUES, true);
      FrameID SUBSET_OF_VALUES = new FrameID(Slot.SUBSET_OF_VALUES, true);
      FrameID NUMERIC_MINIMUM = new FrameID(Slot.NUMERIC_MINIMUM, true);
      FrameID NUMERIC_MAXIMUM = new FrameID(Slot.NUMERIC_MAXIMUM, true);
      FrameID SOME_VALUES = new FrameID(Slot.SOME_VALUES, true);
      FrameID COLLECTION_TYPE = new FrameID(Slot.COLLECTION_TYPE, true);

      FrameID PAL_STATEMENT = new FrameID(Slot.PAL_STATEMENT, true);
      FrameID PAL_DESCRIPTION = new FrameID(Slot.PAL_DESCRIPTION, true);
      FrameID PAL_NAME = new FrameID(Slot.PAL_NAME, true);
      FrameID PAL_RANGE = new FrameID(Slot.PAL_RANGE, true);

      FrameID VALUES = new FrameID(Slot.VALUES, true);

      FrameID DIRECT_SUBSLOTS = new FrameID(Slot.DIRECT_SUBSLOTS, true);
      FrameID DIRECT_SUPERSLOTS = new FrameID(Slot.DIRECT_SUPERSLOTS, true);

      FrameID ANNOTATED_INSTANCE = new FrameID(Slot.ANNOTATED_INSTANCE, true);
      FrameID ANNOTATION_TEXT = new FrameID(Slot.ANNOTATION_TEXT, true);
      FrameID CREATOR = new FrameID(Slot.CREATOR, true);
      FrameID CREATION_TIMESTAMP = new FrameID(Slot.CREATION_TIMESTAMP, true);
      FrameID ASSOCIATED_SLOT = new FrameID(Slot.ASSOCIATED_SLOT, true);
      FrameID MODIFIER = new FrameID(Slot.MODIFIER, true);
      FrameID MODIFICATION_TIMESTAMP = new FrameID(Slot.MODIFICATION_TIMESTAMP, true);

      FrameID FROM = new FrameID(Slot.FROM, true);
      FrameID TO = new FrameID(Slot.TO, true);

      FrameID DIRECT_DOMAIN = new FrameID(Slot.DIRECT_DOMAIN, true);
    }

    /**
     * system facet names
     * 
     * @author Ray Fergerson <fergerson@smi.stanford.edu>
     */
    interface Facet {
        String DIRECT_TEMPLATE_FACETS = ":DIRECT-TEMPLATE-FACETS";

        String DOCUMENTATION = ":DOCUMENTATION-IN-FRAME";
        String DEFAULTS = ":DEFAULTS";
        String CONSTRAINTS = ":CONSTRAINTS";

        String VALUE_TYPE = ":VALUE-TYPE";
        String INVERSE = ":INVERSE";
        String CARDINALITY = ":CARDINALITY";
        String MAXIMUM_CARDINALITY = ":MAXIMUM-CARDINALITY";
        String MINIMUM_CARDINALITY = ":MINIMUM-CARDINALITY";
        String SAME_VALUES = ":SAME-VALUES";
        String NOT_SAME_VALUES = ":NOT-SAME-VALUES";
        String SUBSET_OF_VALUES = ":SUBSET-OF-VALUES";
        String NUMERIC_MINIMUM = ":NUMERIC-MINIMUM";
        String NUMERIC_MAXIMUM = ":NUMERIC-MAXIMUM";
        String SOME_VALUES = ":SOME-VALUES";
        String COLLECTION_TYPE = ":COLLECTION-TYPE";

        String VALUES = ":VALUES";
        String MODIFIER = ":MODIFIER-FACET";
        String MODIFICATION_TIMESTAMP = ":MODIFICATION-TIMESTAMP-FACET";
    }

    /**
     * system facet frame ids.
     * 
     * @author Ray Fergerson <fergerson@smi.stanford.edu>
     */
    interface FacetID {
      FrameID DIRECT_TEMPLATE_FACETS = new FrameID(Facet.DIRECT_TEMPLATE_FACETS, true);

      FrameID DOCUMENTATION = new FrameID(Facet.DOCUMENTATION, true);
      FrameID DEFAULTS = new FrameID(Facet.DEFAULTS, true);
      FrameID CONSTRAINTS = new FrameID(Facet.CONSTRAINTS, true);

      FrameID VALUE_TYPE = new FrameID(Facet.VALUE_TYPE, true);
      FrameID INVERSE = new FrameID(Facet.INVERSE, true);
      FrameID CARDINALITY = new FrameID(Facet.CARDINALITY, true);
      FrameID MAXIMUM_CARDINALITY = new FrameID(Facet.MAXIMUM_CARDINALITY, true);
      FrameID MINIMUM_CARDINALITY = new FrameID(Facet.MINIMUM_CARDINALITY, true);
      FrameID SAME_VALUES = new FrameID(Facet.SAME_VALUES, true);
      FrameID NOT_SAME_VALUES = new FrameID(Facet.NOT_SAME_VALUES, true);
      FrameID SUBSET_OF_VALUES = new FrameID(Facet.SUBSET_OF_VALUES, true);
      FrameID NUMERIC_MINIMUM = new FrameID(Facet.NUMERIC_MINIMUM, true);
      FrameID NUMERIC_MAXIMUM = new FrameID(Facet.NUMERIC_MAXIMUM, true);
      FrameID SOME_VALUES = new FrameID(Facet.SOME_VALUES, true);
      FrameID COLLECTION_TYPE = new FrameID(Facet.COLLECTION_TYPE, true);

      FrameID VALUES = new FrameID(Facet.VALUES, true);

      FrameID MODIFIER = new FrameID(Facet.MODIFIER, true);
      FrameID MODIFICATION_TIMESTAMP = new FrameID(Facet.MODIFICATION_TIMESTAMP, true);
    }
}