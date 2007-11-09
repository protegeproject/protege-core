package edu.stanford.smi.protege.model.query;
//ESCA*JAVA0257

import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.Slot;

public interface Query extends Localizable {
    // all types
    int EQUALS = 1;
    int CONTAINS = 10;

    // string operations
    int MATCHES_STRING = 2;

    // number operations
    int LESS_THAN = 5;
    int LESS_THAN_OR_EQUAL_TO = 7;
    int GREATER_THAN = 6;
    int GREATER_THAN_OR_EQUAL_TO = 8;
}

abstract class AndQuery implements Query {

    AndQuery(Collection<Query> queries) {
    }

}

abstract class OrQuery implements Query {
    OrQuery(Collection queries) {
    }
}

abstract class SlotValueQuery implements Query {

    protected SlotValueQuery() {

    }

    protected SlotValueQuery(Cls cls, Slot slot, int operation, Object value) {
      
    }
}

abstract class OwnSlotValueQuery extends SlotValueQuery {
    OwnSlotValueQuery(Cls cls, Slot slot, int operation, Object value) {
        super(cls, slot, operation, value);
    }
}

abstract class TemplateSlotValueQuery extends SlotValueQuery {
    TemplateSlotValueQuery(Cls cls, Slot slot, int operation, Object value) {
        super(cls, slot, operation, value);
    }
}

abstract class TemplateFacetValueQuery implements Query {
}
