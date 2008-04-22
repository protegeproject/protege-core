package edu.stanford.smi.protege.util;

import java.util.Collection;

public interface MutableSelectable extends Selectable {
    void setSelection(Collection objects);
}
