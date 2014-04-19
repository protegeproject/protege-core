package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.exception.ModificationException;
import edu.stanford.smi.protege.model.*;

public class ReadOnlyFrameStoreHandler_Test extends FrameStore_Test {
    private FrameStore _modifiableFrameStore;

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        _modifiableFrameStore = new InMemoryFrameStore(kb);
        FrameStore fs = AbstractFrameStoreInvocationHandler.newInstance(ReadOnlyFrameStoreHandler.class);
        fs.setDelegate(_modifiableFrameStore);
        return fs;
    }

    public void tearDown() throws Exception {
        super.tearDown();
        _modifiableFrameStore = null;
    }

    protected FrameStore getModifiableFrameStore(DefaultKnowledgeBase kb) {
        return _modifiableFrameStore;
    }

    public void testCreateCls() {
        boolean passed = false;
        try {
            getTestFrameStore().createCls(new FrameID(createFrameName()), null, null, true);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testCreateSlot() {
        boolean passed = false;
        try {
            getTestFrameStore().createSlot(new FrameID(createFrameName()), null, null, true);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testCreateSimpleInstance() {
        boolean passed = false;
        try {
            getTestFrameStore().createSimpleInstance(new FrameID(createFrameName()), null, true);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testCreateFacet() {
        boolean passed = false;
        try {
            getTestFrameStore().createFacet(new FrameID(createFrameName()), null, true);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testDeleteCls() {
        boolean passed = false;
        try {
            getTestFrameStore().deleteCls(null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testDeleteSlot() {
        boolean passed = false;
        try {
            getTestFrameStore().deleteSlot(null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testDeleteFacet() {
        boolean passed = false;
        try {
            getTestFrameStore().deleteFacet(null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testDeleteSimpleInstance() {
        boolean passed = false;
        try {
            getTestFrameStore().deleteSimpleInstance(null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testAddDirectTemplateSlot() {
        boolean passed = false;
        try {
            getTestFrameStore().addDirectTemplateSlot(null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testSetOwnSlotValues() {
        boolean passed = false;
        try {
            getTestFrameStore().setDirectOwnSlotValues(null, null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testSetDirectOwnSlotValues() {
        boolean passed = false;
        try {
            getTestFrameStore().setDirectOwnSlotValues(null, null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testRemoveDirectTemplateSlot() {
        boolean passed = false;
        try {
            getTestFrameStore().removeDirectTemplateSlot(null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testMoveDirectTemplateSlot() {
        boolean passed = false;
        try {
            getTestFrameStore().moveDirectTemplateSlot(null, null, 0);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testSetDirectTemplateSlotValues() {
        boolean passed = false;
        try {
            getTestFrameStore().setDirectTemplateSlotValues(null, null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testSetDirectTemplateFacetValues() {
        boolean passed = false;
        try {
            getTestFrameStore().setDirectTemplateFacetValues(null, null, null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testAddDirectSuperclass() {
        boolean passed = false;
        try {
            getTestFrameStore().addDirectSuperclass(null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testRemoveDirectSuperclass() {
        boolean passed = false;
        try {
            getTestFrameStore().removeDirectSuperclass(null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testMoveDirectSubclass() {
        boolean passed = false;
        try {
            getTestFrameStore().moveDirectSubclass(null, null, 0);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testAddDirectSuperslot() {
        boolean passed = false;
        try {
            getTestFrameStore().addDirectSuperslot(null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testRemoveDirectSuperslot() {
        boolean passed = false;
        try {
            getTestFrameStore().removeDirectSuperslot(null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testAddDirectType() {
        boolean passed = false;
        try {
            getTestFrameStore().addDirectType(null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    public void testRemoveDirectType() {
        boolean passed = false;
        try {
            getTestFrameStore().removeDirectType(null, null);
        } catch (ModificationException e) {
            passed = true;
        }
        assertTrue(passed);
    }

    //ESCA-JAVA0025 
    public void testSetInverseSlotValues() {
        // do nothing
    }
}
