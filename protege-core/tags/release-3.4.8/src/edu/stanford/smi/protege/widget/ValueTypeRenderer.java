package edu.stanford.smi.protege.widget;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ValueTypeRenderer extends DefaultRenderer {
    private static final long serialVersionUID = -6016440571992119716L;
    private static final Map valueTypeToKeyMap = new HashMap();
    
    static {
        insert(ValueType.INSTANCE, ResourceKey.TYPE_INSTANCE);
        insert(ValueType.CLS, ResourceKey.TYPE_CLASS);
        insert(ValueType.ANY, ResourceKey.TYPE_ANY);
        insert(ValueType.BOOLEAN, ResourceKey.TYPE_BOOLEAN);
        insert(ValueType.FLOAT, ResourceKey.TYPE_FLOAT);
        insert(ValueType.INTEGER, ResourceKey.TYPE_INTEGER);
        insert(ValueType.SYMBOL, ResourceKey.TYPE_SYMBOL);
        insert(ValueType.STRING, ResourceKey.TYPE_STRING);
    }
    
    private static void insert(ValueType type, ResourceKey key) {
        valueTypeToKeyMap.put(type, key);
    }
    
    public void load(Object object) {
        ValueType type = (ValueType) object;
        ResourceKey key = getKey(type);
        String text = LocalizedText.getText(key);
        setMainText(text);
    }
    
    private ResourceKey getKey(ValueType type) {
        return (ResourceKey) valueTypeToKeyMap.get(type);
    }

}
