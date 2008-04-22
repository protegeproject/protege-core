package edu.stanford.smi.protege.util;

//ESCA*JAVA0130

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.framestore.SimpleTestCase;


public class CacheMap_Test extends SimpleTestCase {
    private static final Logger log = Log.getLogger(CacheMap_Test.class);
    
    public void testSizeLimit() {
        int limit = 15;
        int multiplier = 10;
        CacheMap<String, Integer> map = new CacheMap<String, Integer>(limit);
        Set<String> keep = new HashSet<String>();
        for (int i = 0; i < multiplier * limit; i++) {
            String key = makeKey(i);
            map.put(key, i);
            keep.add(key);
            if (log.isLoggable(Level.FINE)) {
                log.fine("After adding " + (i + 1) + " entries the map size is " + map.size());
            }
            if (i > limit) {
                assertTrue(map.size() <= limit && map.size() >= 4 * limit / 5);
            }
            int counter = 0;
            for (String key2 : map.keySet()) {
                counter++;
                assertTrue(key2.equals(makeKey(map.get(key2))));
                if (log.isLoggable(Level.FINE)) {
                    log.fine("found " + key2 + " -> " + map.get(key2));
                }
            }
            if (i > limit) {
                assertTrue(counter <= limit && counter >= 4 * limit / 5);
            }
        }
    }
    
    private String makeKey(int i) {
        return "Entry at " + i;
    }
}
