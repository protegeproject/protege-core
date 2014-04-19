package edu.stanford.smi.protege.model.framestore.undo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;

public class DeleteClsCommand extends DeleteFrameCommand {
    private Map<Slot, Collection> directTemplateSlotValues = new HashMap<Slot, Collection>();
    private Map<Slot, Map<Facet, Collection>> directTemplateSlotFacetValues = new HashMap<Slot, Map<Facet, Collection>>();
    
    public DeleteClsCommand(FrameStore delegate, Cls cls) {
        super(delegate, cls);
    }
    
    public Cls getCls() {
        return (Cls) getFrame();
    }
    
    protected void saveFrame() {
        super.saveFrame();
        saveCls();
    }
    
    protected void restoreFrame() {
        super.restoreFrame();
        restoreCls();
    }
    
    private void saveCls() {
        Cls cls = getCls();
        Set<Slot> templateSlots = getDelegate().getTemplateSlots(cls);
        for (Slot slot : templateSlots) {
            Collection values = getDelegate().getDirectTemplateSlotValues(cls, slot);
            if (values != null && !values.isEmpty()) {
                directTemplateSlotValues.put(slot, values);
            }
            Map<Facet, Collection> facetMap = new HashMap<Facet, Collection>();
            directTemplateSlotFacetValues.put(slot, facetMap);
            Set<Facet> facets  = getDelegate().getTemplateFacets(cls, slot);
            for (Facet facet : facets) {
                Collection facetValues = getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
                if (facetValues != null && !facetValues.isEmpty()) {
                    facetMap.put(facet, facetValues);
                }
            }
        }
    }
    
    private void restoreCls() {
        Cls cls = getCls();
        for (Entry<Slot, Collection>  entry : directTemplateSlotValues.entrySet()) {
            getDelegate().setDirectTemplateSlotValues(cls, entry.getKey(), entry.getValue());
        }
        for (Entry<Slot, Map<Facet, Collection>> entry : directTemplateSlotFacetValues.entrySet()) {
            Slot slot = entry.getKey();
            for (Entry<Facet, Collection> facetEntry : entry.getValue().entrySet()) {
                getDelegate().setDirectTemplateFacetValues(cls, slot, facetEntry.getKey(), facetEntry.getValue());
            }
        }
    }

}
