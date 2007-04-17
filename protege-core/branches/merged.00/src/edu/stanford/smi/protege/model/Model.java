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
        int BASE_ID = 1000;
        FrameID THING = FrameID.createSystem(BASE_ID + 0);
        FrameID CLASS = FrameID.createSystem(BASE_ID + 1);
        FrameID STANDARD_CLASS = FrameID.createSystem(BASE_ID + 2);
        FrameID SLOT = FrameID.createSystem(BASE_ID + 3);
        FrameID STANDARD_SLOT = FrameID.createSystem(BASE_ID + 4);
        FrameID FACET = FrameID.createSystem(BASE_ID + 5);
        FrameID STANDARD_FACET = FrameID.createSystem(BASE_ID + 6);

        FrameID INDIVIDUAL = FrameID.createSystem(BASE_ID + 7);
        FrameID NUMBER = FrameID.createSystem(BASE_ID + 8);
        FrameID INTEGER = FrameID.createSystem(BASE_ID + 9);
        FrameID FLOAT = FrameID.createSystem(BASE_ID + 10);
        FrameID STRING = FrameID.createSystem(BASE_ID + 11);
        FrameID SYMBOL = FrameID.createSystem(BASE_ID + 12);
        FrameID BOOLEAN = FrameID.createSystem(BASE_ID + 13);

        FrameID PRIMITIVE_TYPE = FrameID.createSystem(BASE_ID + 14);
        FrameID SYSTEM_CLASS = FrameID.createSystem(BASE_ID + 15);
        FrameID CONSTRAINT = FrameID.createSystem(BASE_ID + 16);
        FrameID RELATION = FrameID.createSystem(BASE_ID + 17);

        FrameID PAL_CONSTRAINT = FrameID.createSystem(BASE_ID + 18);

        FrameID ANNOTATION = FrameID.createSystem(BASE_ID + 19);
        FrameID INSTANCE_ANNOTATION = FrameID.createSystem(BASE_ID + 20);
        FrameID DIRECTED_BINARY_RELATION = FrameID.createSystem(BASE_ID + 21);

        FrameID ROOT_META_CLASS = FrameID.createSystem(BASE_ID + 22);
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
        int BASE_ID = 2000;
        FrameID DOCUMENTATION = FrameID.createSystem(BASE_ID + 0);
        FrameID DOMAIN = FrameID.createSystem(BASE_ID + 1);

        FrameID NAME = FrameID.createSystem(BASE_ID + 2);
        FrameID ROLE = FrameID.createSystem(BASE_ID + 3);
        FrameID DIRECT_SUPERCLASSES = FrameID.createSystem(BASE_ID + 4);
        FrameID DIRECT_SUBCLASSES = FrameID.createSystem(BASE_ID + 5);
        FrameID DIRECT_TYPES = FrameID.createSystem(BASE_ID + 6);
        /**
         * @deprecated Use #DIRECT_TYPES
         */
        FrameID DIRECT_TYPE = DIRECT_TYPES;
        FrameID DIRECT_INSTANCES = FrameID.createSystem(BASE_ID + 7);
        FrameID DIRECT_TEMPLATE_SLOTS = FrameID.createSystem(BASE_ID + 8);
        // FrameID DIRECT_BROWSER_SLOT = FrameID.createSystem(BASE_ID + 9);

        // FrameID OWN_SLOTS = FrameID.createSystem(BASE_ID + 10);
        FrameID ASSOCIATED_FACET = FrameID.createSystem(BASE_ID + 11);

        FrameID CONSTRAINTS = FrameID.createSystem(BASE_ID + 12);
        FrameID DEFAULTS = FrameID.createSystem(BASE_ID + 13);
        FrameID VALUE_TYPE = FrameID.createSystem(BASE_ID + 14);
        FrameID INVERSE = FrameID.createSystem(BASE_ID + 15);
        FrameID CARDINALITY = FrameID.createSystem(BASE_ID + 16);
        FrameID MAXIMUM_CARDINALITY = FrameID.createSystem(BASE_ID + 17);
        FrameID MINIMUM_CARDINALITY = FrameID.createSystem(BASE_ID + 18);
        FrameID SAME_VALUES = FrameID.createSystem(BASE_ID + 19);
        FrameID NOT_SAME_VALUES = FrameID.createSystem(BASE_ID + 20);
        FrameID SUBSET_OF_VALUES = FrameID.createSystem(BASE_ID + 21);
        FrameID NUMERIC_MINIMUM = FrameID.createSystem(BASE_ID + 22);
        FrameID NUMERIC_MAXIMUM = FrameID.createSystem(BASE_ID + 23);
        FrameID SOME_VALUES = FrameID.createSystem(BASE_ID + 24);
        FrameID COLLECTION_TYPE = FrameID.createSystem(BASE_ID + 25);

        FrameID PAL_STATEMENT = FrameID.createSystem(BASE_ID + 26);
        FrameID PAL_DESCRIPTION = FrameID.createSystem(BASE_ID + 27);
        FrameID PAL_NAME = FrameID.createSystem(BASE_ID + 28);
        FrameID PAL_RANGE = FrameID.createSystem(BASE_ID + 29);

        FrameID VALUES = FrameID.createSystem(BASE_ID + 30);

        FrameID DIRECT_SUBSLOTS = FrameID.createSystem(BASE_ID + 31);
        FrameID DIRECT_SUPERSLOTS = FrameID.createSystem(BASE_ID + 32);

        FrameID ANNOTATED_INSTANCE = FrameID.createSystem(BASE_ID + 33);
        FrameID ANNOTATION_TEXT = FrameID.createSystem(BASE_ID + 34);
        FrameID CREATOR = FrameID.createSystem(BASE_ID + 36);
        FrameID CREATION_TIMESTAMP = FrameID.createSystem(BASE_ID + 37);
        FrameID ASSOCIATED_SLOT = FrameID.createSystem(BASE_ID + 38);
        FrameID MODIFIER = FrameID.createSystem(BASE_ID + 39);
        FrameID MODIFICATION_TIMESTAMP = FrameID.createSystem(BASE_ID + 40);

        FrameID FROM = FrameID.createSystem(BASE_ID + 41);
        FrameID TO = FrameID.createSystem(BASE_ID + 42);

        FrameID DIRECT_DOMAIN = FrameID.createSystem(BASE_ID + 43);
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
        int BASE_ID = 3000;
        FrameID DIRECT_TEMPLATE_FACETS = FrameID.createSystem(BASE_ID + 0);

        FrameID DOCUMENTATION = FrameID.createSystem(BASE_ID + 1);
        FrameID DEFAULTS = FrameID.createSystem(BASE_ID + 2);
        FrameID CONSTRAINTS = FrameID.createSystem(BASE_ID + 3);

        FrameID VALUE_TYPE = FrameID.createSystem(BASE_ID + 4);
        FrameID INVERSE = FrameID.createSystem(BASE_ID + 5);
        FrameID CARDINALITY = FrameID.createSystem(BASE_ID + 6);
        FrameID MAXIMUM_CARDINALITY = FrameID.createSystem(BASE_ID + 7);
        FrameID MINIMUM_CARDINALITY = FrameID.createSystem(BASE_ID + 8);
        FrameID SAME_VALUES = FrameID.createSystem(BASE_ID + 9);
        FrameID NOT_SAME_VALUES = FrameID.createSystem(BASE_ID + 10);
        FrameID SUBSET_OF_VALUES = FrameID.createSystem(BASE_ID + 11);
        FrameID NUMERIC_MINIMUM = FrameID.createSystem(BASE_ID + 12);
        FrameID NUMERIC_MAXIMUM = FrameID.createSystem(BASE_ID + 13);
        FrameID SOME_VALUES = FrameID.createSystem(BASE_ID + 14);
        FrameID COLLECTION_TYPE = FrameID.createSystem(BASE_ID + 15);

        FrameID VALUES = FrameID.createSystem(BASE_ID + 16);

        FrameID MODIFIER = FrameID.createSystem(BASE_ID + 17);
        FrameID MODIFICATION_TIMESTAMP = FrameID.createSystem(BASE_ID + 18);
    }
}