package edu.stanford.smi.protege.storage.jdbc.framedb;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.storage.jdbc.*;
import edu.stanford.smi.protege.test.*;
import edu.stanford.smi.protege.util.*;

/**
 * Unit tests for FrameDBStorage.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameDBStorage_Test extends APITestCase {

    public void testNull() {
    }

    // creation is disallowed in the old jdbc backend
    public void _testCaching() {
        final int N_FRAMES = 10;
        FrameID[] frameIDs = new FrameID[N_FRAMES];

        OldJdbcDefaultKnowledgeBase kb = new OldJdbcDefaultKnowledgeBase(null);
        Collection rootClses = kb.getRootClses();
        FrameDBStorage storage = (FrameDBStorage) kb.getStorage();
        storage.setCaching(true);
        storage.setUseWeakReference(true);
        for (int i = 0; i < N_FRAMES; ++i) {
            Cls cls = kb.createCls(null, rootClses);
            frameIDs[i] = cls.getFrameID();
            Thread.yield();
        }
        SystemUtilities.gc();
        int cleared = 0;
        for (int i = 0; i < N_FRAMES; ++i) {
            Frame frame = kb.getFrame(frameIDs[i]);
            if (frame == null) {
                ++cleared;
            }
        }
        // Log.trace("cleared " + cleared, this, "testCaching");
        assertTrue("flush failed", cleared > 0);
    }
}
