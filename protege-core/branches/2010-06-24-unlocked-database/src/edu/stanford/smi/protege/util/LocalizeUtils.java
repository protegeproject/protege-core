package edu.stanford.smi.protege.util;

import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LocalizeUtils {

    public static void localize(Object o, KnowledgeBase kb) {
        if (o == null) {
            // do nothing
        } else if (o instanceof Collection) {
            localizeCollection((Collection) o, kb);
        } else if (o instanceof Map) {
            localizeMap((Map) o, kb);
        } else if (o instanceof Localizable) {
            ((Localizable) o).localize(kb);
        } else if (o instanceof Remote && o instanceof Proxy) {
            ;
        } else if (!isPrimative(o)) {
            Log.getLogger().warning("Unhandled localization: " + o);
        }
    }

    private static boolean isPrimative(Object o) {
        return o instanceof String || o instanceof Boolean || o instanceof Integer || o instanceof Float;
    }

    private static void localizeCollection(Collection values, KnowledgeBase kb) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            localize(o, kb);
        }
    }

    private static void localizeMap(Map values, KnowledgeBase kb) {
        Iterator i = values.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            localize(entry.getKey(), kb);
            localize(entry.getValue(), kb);
        }
    }
}