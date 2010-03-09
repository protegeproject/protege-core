package edu.stanford.smi.protege.server.metaproject;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.server.metaproject.impl.UserImpl;

public class Digest_Test extends TestCase {
    public static final String  META_PROJECT_WITH_DIGEST = "junit/pprj/metaproject-digest.pprj";
    public static final String TIMOTHY = "Timothy Redmond";
    public static final String TIMOTHY_PASSWORD = "troglodyte";
    public static final String RAY = "Ray Fergerson";
    public static final String RAY_PASSWORD = "Claudia";

    @SuppressWarnings("unchecked")
    public void testDigest() {
        Collection errors = new  ArrayList();
        Project p = new Project(META_PROJECT_WITH_DIGEST, errors);
        assertTrue(errors.isEmpty());
        MetaProject metaproject = new MetaProjectImpl(p);

        User ray = metaproject.createUser(RAY, RAY_PASSWORD);
        assertTrue(ray.verifyPassword(RAY_PASSWORD));
        assertTrue(!ray.verifyPassword(TIMOTHY_PASSWORD));
        ray.setPassword(TIMOTHY_PASSWORD);
        assertTrue(!ray.verifyPassword(RAY_PASSWORD));
        assertTrue(ray.verifyPassword(TIMOTHY_PASSWORD));
        
        metaproject = new MetaProjectImpl(p);
        ray = metaproject.getUser(RAY);
        assertTrue(!ray.verifyPassword(RAY_PASSWORD));
        assertTrue(ray.verifyPassword(TIMOTHY_PASSWORD));
        
        Instance r = ((UserImpl) ray).getProtegeInstance();
        assertTrue(r.getOwnSlotValue(p.getKnowledgeBase().getSlot(MetaProjectImpl.SlotEnum.password.toString())) != null);
        assertTrue(r.getOwnSlotValue(p.getKnowledgeBase().getSlot(MetaProjectImpl.SlotEnum.salt.toString())) != null);

    }
}
