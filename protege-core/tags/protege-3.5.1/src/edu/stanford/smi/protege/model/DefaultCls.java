package edu.stanford.smi.protege.model;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.resource.*;

/**
 * Default implementation of Cls interface.  Forwards all method calls
 * to its DefaultKnowledgeBase.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultCls extends DefaultInstance implements Cls {

    private static final long serialVersionUID = 1971012286146560647L;

    public DefaultCls() {

    }

    public DefaultCls(KnowledgeBase kb, FrameID id) {
        super(kb, id);
    }

    public void addClsListener(ClsListener listener) {
        getDefaultKnowledgeBase().addClsListener(this, listener);
    }

    public void addDirectSuperclass(Cls superclass) {
        getDefaultKnowledgeBase().addDirectSuperclass(this, superclass);
    }

    public void addDirectTemplateSlot(Slot slot) {
        getDefaultKnowledgeBase().addDirectTemplateSlot(this, slot);
    }

    public void addTemplateFacetValue(Slot slot, Facet facet, Object value) {
        getDefaultKnowledgeBase().addTemplateFacetValue(this, slot, facet, value);
    }

    public void addTemplateSlotValue(Slot slot, Object value) {
        getDefaultKnowledgeBase().addTemplateSlotValue(this, slot, value);
    }

    public Instance createDirectInstance(String name) {
        return getDefaultKnowledgeBase().createInstance(name, this);
    }

    /**
     * @deprecated
     */
    public Slot getBrowserSlot() {
        BrowserSlotPattern pattern = getBrowserSlotPattern();
        return (pattern == null) ? null : pattern.getFirstSlot();
    }

    public BrowserSlotPattern getBrowserSlotPattern() {
        BrowserSlotPattern browserSlotPattern;
        Project p = getProject();
        if (p == null) {
            browserSlotPattern = new BrowserSlotPattern(getDefaultKnowledgeBase().getNameSlot());
        } else {
            browserSlotPattern = getProject().getBrowserSlotPattern(this);
        }
        return browserSlotPattern;
    }

    public Collection getConcreteSubclasses() {
        Collection subclasses = new ArrayList(getSubclasses());
        Iterator i = subclasses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            if (cls.isAbstract()) {
                i.remove();
            }
        }
        return subclasses;
    }

    /**
     * @deprecated Use #getDirectBrowserSlotPattern
     */
    public Slot getDirectBrowserSlot() {
        return getProject().getDirectBrowserSlot(this);
    }

    public BrowserSlotPattern getDirectBrowserSlotPattern() {
        return getProject().getDirectBrowserSlotPattern(this);
    }

    /**
     * @deprecated Use #getInheritedBrowserSlotPattern
     */
    public Slot getInheritedBrowserSlot() {
        return getProject().getInheritedBrowserSlot(this);
    }

    public BrowserSlotPattern getInheritedBrowserSlotPattern() {
        return getProject().getInheritedBrowserSlotPattern(this);
    }

    public int getDirectInstanceCount() {
        return getDefaultKnowledgeBase().getDirectInstanceCount(this);
    }

    public Collection<Instance> getDirectInstances() {
        return getDefaultKnowledgeBase().getDirectInstances(this);
    }

    public int getDirectSubclassCount() {
        return getDefaultKnowledgeBase().getDirectSubclassCount(this);
    }

    public Collection getDirectSubclasses() {
        return getDefaultKnowledgeBase().getDirectSubclasses(this);
    }

    public int getDirectSuperclassCount() {
        return getDefaultKnowledgeBase().getDirectSuperclassCount(this);
    }

    public Collection<Cls> getDirectSuperclasses() {
        return getDefaultKnowledgeBase().getDirectSuperclasses(this);
    }

    public List getDirectTemplateFacetValues(Slot slot, Facet facet) {
        return getDefaultKnowledgeBase().getDirectTemplateFacetValues(this, slot, facet);
    }

    public Collection getDirectTemplateSlots() {
        return getDefaultKnowledgeBase().getDirectTemplateSlots(this);
    }

    public List getDirectTemplateSlotValues(Slot slot) {
        return getDefaultKnowledgeBase().getDirectTemplateSlotValues(this, slot);
    }

    public int getInstanceCount() {
        return getDefaultKnowledgeBase().getInstanceCount(this);
    }

    public Collection<Instance> getInstances() {
        return getDefaultKnowledgeBase().getInstances(this);
    }

    public Collection getSubclasses() {
        return getDefaultKnowledgeBase().getSubclasses(this);
    }

    public Collection getSuperclasses() {
        return getDefaultKnowledgeBase().getSuperclasses(this);
    }

    public Collection getTemplateFacets(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateFacets(this, slot);
    }

    public Collection getOverriddenTemplateFacets(Slot slot) {
        return getDefaultKnowledgeBase().getOverriddenTemplateFacets(this, slot);
    }

    public Collection getDirectlyOverriddenTemplateFacets(Slot slot) {
        return getDefaultKnowledgeBase().getDirectlyOverriddenTemplateFacets(this, slot);
    }

    public Object getTemplateFacetValue(Slot slot, Facet facet) {
        return getDefaultKnowledgeBase().getTemplateFacetValue(this, slot, facet);
    }

    public Collection getTemplateFacetValues(Slot slot, Facet facet) {
        return getDefaultKnowledgeBase().getTemplateFacetValues(this, slot, facet);
    }

    public Collection getTemplateSlotAllowedClses(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotAllowedClses(this, slot);
    }

    public Collection getTemplateSlotAllowedParents(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotAllowedParents(this, slot);
    }

    public Collection getTemplateSlotAllowedValues(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotAllowedValues(this, slot);
    }

    public boolean getTemplateSlotAllowsMultipleValues(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotAllowsMultipleValues(this, slot);
    }

    public Collection getTemplateSlotDefaultValues(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotDefaultValues(this, slot);
    }

    public Collection getTemplateSlotDocumentation(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotDocumentation(this, slot);
    }

    public int getTemplateSlotMaximumCardinality(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotMaximumCardinality(this, slot);
    }

    public Number getTemplateSlotMaximumValue(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotMaximumValue(this, slot);
    }

    public int getTemplateSlotMinimumCardinality(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotMinimumCardinality(this, slot);
    }

    public Number getTemplateSlotMinimumValue(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotMinimumValue(this, slot);
    }

    public Collection getTemplateSlots() {
        return getDefaultKnowledgeBase().getTemplateSlots(this);
    }

    public Object getTemplateSlotValue(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotValue(this, slot);
    }

    public Collection getTemplateSlotValues(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotValues(this, slot);
    }

    public ValueType getTemplateSlotValueType(Slot slot) {
        return getDefaultKnowledgeBase().getTemplateSlotValueType(this, slot);
    }

    public int getVisibleDirectSubclassCount() {
        return getVisibleDirectSubclasses().size();
    }

    public Collection getVisibleTemplateSlots() {
        return getVisibleFrames(getTemplateSlots());
    }

    public Collection getVisibleDirectSubclasses() {
        return getVisibleFrames(getDirectSubclasses());
    }

    private static Collection getVisibleFrames(Collection frames) {
        Collection visibleFrames = new ArrayList();
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (frame.isVisible()) {
                visibleFrames.add(frame);
            }
        }
        return visibleFrames;
    }

    public boolean hasDirectlyOverriddenTemplateFacet(Slot slot, Facet facet) {
        return getDefaultKnowledgeBase().hasDirectlyOverriddenTemplateFacet(this, slot, facet);
    }

    public boolean hasDirectlyOverriddenTemplateSlot(Slot slot) {
        return getDefaultKnowledgeBase().hasDirectlyOverriddenTemplateSlot(this, slot);
    }

    public boolean hasDirectSuperclass(Cls cls) {
        return getDefaultKnowledgeBase().hasDirectSuperclass(this, cls);
    }

    public boolean hasDirectTemplateSlot(Slot slot) {
        return getDefaultKnowledgeBase().hasDirectTemplateSlot(this, slot);
    }

    public boolean hasInheritedTemplateSlot(Slot slot) {
        return getDefaultKnowledgeBase().hasInheritedTemplateSlot(this, slot);
    }

    public boolean hasOverriddenTemplateFacet(Slot slot, Facet facet) {
        return getDefaultKnowledgeBase().hasOverriddenTemplateFacet(this, slot, facet);
    }

    public boolean hasOverriddenTemplateSlot(Slot slot) {
        return getDefaultKnowledgeBase().hasOverriddenTemplateSlot(this, slot);
    }

    public boolean hasSuperclass(Cls cls) {
        return getDefaultKnowledgeBase().hasSuperclass(this, cls);
    }

    public boolean hasTemplateFacet(Slot slot, Facet facet) {
        return getTemplateFacets(slot).contains(facet);
    }

    public boolean hasTemplateSlot(Slot slot) {
        return getDefaultKnowledgeBase().hasTemplateSlot(this, slot);
    }

    public boolean isAbstract() {
        return getDefaultKnowledgeBase().isAbstract(this);
    }

    public boolean isClsMetaCls() {
        return getDefaultKnowledgeBase().isClsMetaCls(this);
    }

    public boolean isConcrete() {
        return !isAbstract();
    }

    public boolean isDefaultClsMetaCls() {
        return getDefaultKnowledgeBase().isDefaultClsMetaCls(this);
    }

    public boolean isDefaultFacetMetaCls() {
        return getDefaultKnowledgeBase().isDefaultFacetMetaCls(this);
    }

    public boolean isDefaultSlotMetaCls() {
        return getDefaultKnowledgeBase().isDefaultSlotMetaCls(this);
    }

    public boolean isFacetMetaCls() {
        return getDefaultKnowledgeBase().isFacetMetaCls(this);
    }

    public boolean isMetaCls() {
        return getDefaultKnowledgeBase().isMetaCls(this);
    }

    public boolean isRoot() {
        return equals(getDefaultKnowledgeBase().getRootCls(), this) || equals(getDirectType(), this);
    }

    public boolean isSlotMetaCls() {
        return getDefaultKnowledgeBase().isSlotMetaCls(this);
    }

    public void moveDirectSubclass(Cls movedSubclass, Cls afterCls) {
        getDefaultKnowledgeBase().moveDirectSubclass(this, movedSubclass, afterCls);
    }

    public void moveDirectTemplateSlot(Slot slot, int toIndex) {
        getDefaultKnowledgeBase().moveDirectTemplateSlot(this, slot, toIndex);
    }

    public void removeClsListener(ClsListener listener) {
        getDefaultKnowledgeBase().removeClsListener(this, listener);
    }

    public void removeDirectSuperclass(Cls superclass) {
        getDefaultKnowledgeBase().removeDirectSuperclass(this, superclass);
    }

    public void removeDirectTemplateSlot(Slot slot) {
        getDefaultKnowledgeBase().removeDirectTemplateSlot(this, slot);
    }

    public void removeTemplateFacetOverrides(Slot slot) {
        getDefaultKnowledgeBase().removeTemplateFacetOverrides(this, slot);
    }

    public void setAbstract(boolean isAbstract) {
        getDefaultKnowledgeBase().setAbstract(this, isAbstract);
    }

    /**
     * @deprecated
     */
    public void setDirectBrowserSlot(Slot slot) {
        setDirectBrowserSlotPattern(new BrowserSlotPattern(slot));
    }

    public void setDirectBrowserSlotPattern(BrowserSlotPattern slotPattern) {
        getDefaultKnowledgeBase().setDirectBrowserSlotPattern(this, slotPattern);
    }

    public void setDirectTypeOfSubclasses(Cls metaCls) {
        getDefaultKnowledgeBase().setDirectTypeOfSubclasses(this, metaCls);
    }

    public void setTemplateFacetValue(Slot slot, Facet facet, Object value) {
        getDefaultKnowledgeBase().setTemplateFacetValue(this, slot, facet, value);
    }

    public void setTemplateFacetValues(Slot slot, Facet facet, Collection values) {
        getDefaultKnowledgeBase().setTemplateFacetValues(this, slot, facet, values);
    }

    public void setTemplateSlotAllowedClses(Slot slot, Collection clses) {
        getDefaultKnowledgeBase().setTemplateSlotAllowedClses(this, slot, clses);
    }

    public void setTemplateSlotAllowedParents(Slot slot, Collection parents) {
        getDefaultKnowledgeBase().setTemplateSlotAllowedParents(this, slot, parents);
    }

    public void setTemplateSlotAllowedValues(Slot slot, Collection values) {
        getDefaultKnowledgeBase().setTemplateSlotAllowedValues(this, slot, values);
    }

    public void setTemplateSlotAllowsMultipleValues(Slot slot, boolean multiple) {
        getDefaultKnowledgeBase().setTemplateSlotAllowsMultipleValues(this, slot, multiple);
    }

    public void setTemplateSlotDefaultValues(Slot slot, Collection values) {
        getDefaultKnowledgeBase().setTemplateSlotDefaultValues(this, slot, values);
    }

    public void setTemplateSlotDocumentation(Slot slot, String value) {
        getDefaultKnowledgeBase().setTemplateSlotDocumentation(this, slot, value);
    }

    public void setTemplateSlotDocumentation(Slot slot, Collection values) {
        getDefaultKnowledgeBase().setTemplateSlotDocumentation(this, slot, values);
    }

    public void setTemplateSlotMaximumCardinality(Slot slot, int max) {
        getDefaultKnowledgeBase().setTemplateSlotMaximumCardinality(this, slot, max);
    }

    public void setTemplateSlotMaximumValue(Slot slot, Number maximum) {
        getDefaultKnowledgeBase().setTemplateSlotMaximumValue(this, slot, maximum);
    }

    public void setTemplateSlotMinimumCardinality(Slot slot, int min) {
        getDefaultKnowledgeBase().setTemplateSlotMinimumCardinality(this, slot, min);
    }

    public void setTemplateSlotMinimumValue(Slot slot, Number minimum) {
        getDefaultKnowledgeBase().setTemplateSlotMinimumValue(this, slot, minimum);
    }

    public void setTemplateSlotValue(Slot slot, Object value) {
        getDefaultKnowledgeBase().setTemplateSlotValue(this, slot, value);
    }

    public void setTemplateSlotValues(Slot slot, Collection values) {
        getDefaultKnowledgeBase().setTemplateSlotValues(this, slot, values);
    }

    public void setTemplateSlotValueType(Slot slot, ValueType type) {
        getDefaultKnowledgeBase().setTemplateSlotValueType(this, slot, type);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Cls(");
        buffer.append(getName());
        buffer.append(")");
        return buffer.toString();
    }

    public Icon getIcon() {
        return Icons.getClsIcon(isClsMetaCls(), isAbstract(), !isEditable(), !isVisible());
    }
    
    public Cls rename(String name) {
        return (Cls) super.rename(name);
    }
}
