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
      FrameID THING = new FrameID(Cls.THING);
      FrameID CLASS = new FrameID(Cls.CLASS);
      FrameID STANDARD_CLASS = new FrameID(Cls.STANDARD_CLASS);
      FrameID SLOT = new FrameID(Cls.SLOT);
      FrameID STANDARD_SLOT = new FrameID(Cls.STANDARD_SLOT);
      FrameID FACET = new FrameID(Cls.FACET);
      FrameID STANDARD_FACET = new FrameID(Cls.STANDARD_FACET);

      // FrameID INDIVIDUAL = FrameID.createSystem(BASE_ID + 7);
      // FrameID NUMBER = FrameID.createSystem(BASE_ID + 8);
      FrameID INTEGER = new FrameID(Cls.INTEGER);
      FrameID FLOAT = new FrameID(Cls.FLOAT);
      FrameID STRING = new FrameID(Cls.STRING);
      FrameID SYMBOL = new FrameID(Cls.SYMBOL);
      FrameID BOOLEAN = new FrameID(Cls.BOOLEAN);

      FrameID PRIMITIVE_TYPE = new FrameID(Cls.PRIMITIVE_TYPE);
      FrameID SYSTEM_CLASS = new FrameID(Cls.SYSTEM_CLASS);
      FrameID CONSTRAINT = new FrameID(Cls.CONSTRAINT);
      FrameID RELATION = new FrameID(Cls.RELATION);

      FrameID PAL_CONSTRAINT = new FrameID(Cls.PAL_CONSTRAINT);

      FrameID ANNOTATION = new FrameID(Cls.ANNOTATION);
      FrameID INSTANCE_ANNOTATION = new FrameID(Cls.INSTANCE_ANNOTATION);
      FrameID DIRECTED_BINARY_RELATION = new FrameID(Cls.DIRECTED_BINARY_RELATION);

      FrameID ROOT_META_CLASS = new FrameID(Cls.ROOT_META_CLASS);
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
      FrameID DOCUMENTATION = new FrameID(Slot.DOCUMENTATION);
      FrameID DOMAIN = new FrameID(Slot.DOMAIN);

      FrameID NAME = new FrameID(Slot.NAME);
      FrameID ROLE = new FrameID(Slot.ROLE);
      FrameID DIRECT_SUPERCLASSES = new FrameID(Slot.DIRECT_SUPERCLASSES);
      FrameID DIRECT_SUBCLASSES = new FrameID(Slot.DIRECT_SUBCLASSES);
      FrameID DIRECT_TYPES = new FrameID(Slot.DIRECT_TYPES);
        /**
         * @deprecated Use #DIRECT_TYPES
         */
      FrameID DIRECT_TYPE = new FrameID(Slot.DIRECT_TYPE);
      FrameID DIRECT_INSTANCES = new FrameID(Slot.DIRECT_INSTANCES);
      FrameID DIRECT_TEMPLATE_SLOTS = new FrameID(Slot.DIRECT_TEMPLATE_SLOTS);
        // FrameID DIRECT_BROWSER_SLOT = FrameID.createSystem(BASE_ID + 9);

        // FrameID OWN_SLOTS = FrameID.createSystem(BASE_ID + 10);
      FrameID ASSOCIATED_FACET = new FrameID(Slot.ASSOCIATED_FACET);

      FrameID CONSTRAINTS = new FrameID(Slot.CONSTRAINTS);
      FrameID DEFAULTS = new FrameID(Slot.DEFAULTS);
      FrameID VALUE_TYPE = new FrameID(Slot.VALUE_TYPE);
      FrameID INVERSE = new FrameID(Slot.INVERSE);
      FrameID CARDINALITY = new FrameID(Slot.CARDINALITY);
      FrameID MAXIMUM_CARDINALITY = new FrameID(Slot.MAXIMUM_CARDINALITY);
      FrameID MINIMUM_CARDINALITY = new FrameID(Slot.MINIMUM_CARDINALITY);
      FrameID SAME_VALUES = new FrameID(Slot.SAME_VALUES);
      FrameID NOT_SAME_VALUES = new FrameID(Slot.NOT_SAME_VALUES);
      FrameID SUBSET_OF_VALUES = new FrameID(Slot.SUBSET_OF_VALUES);
      FrameID NUMERIC_MINIMUM = new FrameID(Slot.NUMERIC_MINIMUM);
      FrameID NUMERIC_MAXIMUM = new FrameID(Slot.NUMERIC_MAXIMUM);
      FrameID SOME_VALUES = new FrameID(Slot.SOME_VALUES);
      FrameID COLLECTION_TYPE = new FrameID(Slot.COLLECTION_TYPE);

      FrameID PAL_STATEMENT = new FrameID(Slot.PAL_STATEMENT);
      FrameID PAL_DESCRIPTION = new FrameID(Slot.PAL_DESCRIPTION);
      FrameID PAL_NAME = new FrameID(Slot.PAL_NAME);
      FrameID PAL_RANGE = new FrameID(Slot.PAL_RANGE);

      FrameID VALUES = new FrameID(Slot.VALUES);

      FrameID DIRECT_SUBSLOTS = new FrameID(Slot.DIRECT_SUBSLOTS);
      FrameID DIRECT_SUPERSLOTS = new FrameID(Slot.DIRECT_SUPERSLOTS);

      FrameID ANNOTATED_INSTANCE = new FrameID(Slot.ANNOTATED_INSTANCE);
      FrameID ANNOTATION_TEXT = new FrameID(Slot.ANNOTATION_TEXT);
      FrameID CREATOR = new FrameID(Slot.CREATOR);
      FrameID CREATION_TIMESTAMP = new FrameID(Slot.CREATION_TIMESTAMP);
      FrameID ASSOCIATED_SLOT = new FrameID(Slot.ASSOCIATED_SLOT);
      FrameID MODIFIER = new FrameID(Slot.MODIFIER);
      FrameID MODIFICATION_TIMESTAMP = new FrameID(Slot.MODIFICATION_TIMESTAMP);

      FrameID FROM = new FrameID(Slot.FROM);
      FrameID TO = new FrameID(Slot.TO);

      FrameID DIRECT_DOMAIN = new FrameID(Slot.DIRECT_DOMAIN);
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
      FrameID DIRECT_TEMPLATE_FACETS = new FrameID(Facet.DIRECT_TEMPLATE_FACETS);

      FrameID DOCUMENTATION = new FrameID(Facet.DOCUMENTATION);
      FrameID DEFAULTS = new FrameID(Facet.DEFAULTS);
      FrameID CONSTRAINTS = new FrameID(Facet.CONSTRAINTS);

      FrameID VALUE_TYPE = new FrameID(Facet.VALUE_TYPE);
      FrameID INVERSE = new FrameID(Facet.INVERSE);
      FrameID CARDINALITY = new FrameID(Facet.CARDINALITY);
      FrameID MAXIMUM_CARDINALITY = new FrameID(Facet.MAXIMUM_CARDINALITY);
      FrameID MINIMUM_CARDINALITY = new FrameID(Facet.MINIMUM_CARDINALITY);
      FrameID SAME_VALUES = new FrameID(Facet.SAME_VALUES);
      FrameID NOT_SAME_VALUES = new FrameID(Facet.NOT_SAME_VALUES);
      FrameID SUBSET_OF_VALUES = new FrameID(Facet.SUBSET_OF_VALUES);
      FrameID NUMERIC_MINIMUM = new FrameID(Facet.NUMERIC_MINIMUM);
      FrameID NUMERIC_MAXIMUM = new FrameID(Facet.NUMERIC_MAXIMUM);
      FrameID SOME_VALUES = new FrameID(Facet.SOME_VALUES);
      FrameID COLLECTION_TYPE = new FrameID(Facet.COLLECTION_TYPE);

      FrameID VALUES = new FrameID(Facet.VALUES);

      FrameID MODIFIER = new FrameID(Facet.MODIFIER);
      FrameID MODIFICATION_TIMESTAMP = new FrameID(Facet.MODIFICATION_TIMESTAMP);
    }
}
