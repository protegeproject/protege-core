package edu.stanford.smi.protege.model;

import edu.stanford.smi.protege.test.*;

/**
 * Unit tests for DefaultCls
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultCls_Test extends APITestCase {

    public void testHasDirectlyOverriddenTemplateFacet() {
        Cls standardSlot = getCls(Model.Cls.STANDARD_SLOT);
        Cls myMetaSlot = createSubCls(standardSlot);
        Slot slotWithFacet = createSingleValuedSlot(ValueType.STRING);
        Facet facet = createFacet();
        slotWithFacet.setAssociatedFacet(facet);
        myMetaSlot.addDirectTemplateSlot(slotWithFacet);
        Slot slot = (Slot) createInstance(myMetaSlot);
        slot.setOwnSlotValue(slotWithFacet, "foo");
        Cls cls = createCls();
        cls.addDirectTemplateSlot(slot);
        assertTrue("attached", !cls.hasDirectlyOverriddenTemplateFacet(slot, facet));
        cls.setTemplateFacetValue(slot, facet, "bar");
        assertTrue("overridden", cls.hasDirectlyOverriddenTemplateFacet(slot, facet));
        Cls subclass = createSubCls(cls);
        assertTrue("not overridden in subclass", !subclass.hasDirectlyOverriddenTemplateFacet(slot, facet));
        Cls subSubClass = createSubCls(subclass);
        assertTrue("not overridden in subsubclass", !subSubClass.hasDirectlyOverriddenTemplateFacet(slot, facet));
        subclass.setTemplateFacetValue(slot, facet, "baz");
        assertTrue("overridden in subclass", subclass.hasDirectlyOverriddenTemplateFacet(slot, facet));
        assertTrue("not overridden in subsubclass - 2", !subSubClass.hasDirectlyOverriddenTemplateFacet(slot, facet));
        subSubClass.setTemplateFacetValue(slot, facet, "bat");
        assertTrue("overridden in subsubclass", subSubClass.hasDirectlyOverriddenTemplateFacet(slot, facet));
    }

    public void testHasOverriddenTemplateFacet() {
        Facet facet = getFacet(Model.Facet.VALUE_TYPE);
        Cls cls = createCls();
        Slot slot = createMultiValuedSlot(ValueType.ANY);
        cls.addDirectTemplateSlot(slot);
        Cls subclass = createSubCls(cls);
        assertTrue("subclass not overridden", !subclass.hasOverriddenTemplateFacet(slot, facet));
        cls.setTemplateSlotValueType(slot, ValueType.INTEGER);
        assertTrue("class overridden", cls.hasOverriddenTemplateFacet(slot, facet));
        assertTrue("subclass overridden", subclass.hasOverriddenTemplateFacet(slot, facet));
    }
}
