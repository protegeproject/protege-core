package edu.stanford.smi.protege.model.query;

import java.util.*;
import edu.stanford.smi.protege.model.*;

public interface Query {
    // all types
    public final int EQUALS = 1;
    public final int CONTAINS = 10;

    // string operations
    public final int MATCHES_STRING = 2;

    // number operations
    public final int LESS_THAN = 5;
    public final int LESS_THAN_OR_EQUAL_TO = 7;
    public final int GREATER_THAN = 6;
    public final int GREATER_THAN_OR_EQUAL_TO = 8;
}

class AndQuery implements Query {
    public AndQuery(Collection queries) {
    }
}

class OrQuery implements Query {
    public OrQuery(Collection queries) {
    }
}

abstract class SlotValueQuery implements Query {


    protected SlotValueQuery(Cls cls, Slot slot, int operation, Object value) {
    }
}

class OwnSlotValueQuery extends SlotValueQuery {
    public OwnSlotValueQuery(Cls cls, Slot slot, int operation, Object value) {
        super(cls, slot, operation, value);
    }
}

class TemplateSlotValueQuery extends SlotValueQuery {
    protected TemplateSlotValueQuery(Cls cls, Slot slot, int operation, Object value) {
        super(cls, slot, operation, value);
    }
}

class TemplateFacetValueQuery implements Query {
}
