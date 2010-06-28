package edu.stanford.smi.protege.util;
//ESCA*JAVA0037

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.plugin.ProjectFixupPlugin;
import edu.stanford.smi.protege.resource.Files;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.storage.clips.ClipsKnowledgeBaseFactory;

/** 
 * Fix backwards compatibility problems in the .pprj files.  Methods in this class are called automatically when a project
 * is loaded.  
 * 
 * As of release 1.8 there are no recent problems so we should consider removing most of this stuff.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

public class Frames1_8_BackwardsCompatibilityProjectFixups implements ProjectFixupPlugin {

	private static final String MIN_VERSION = "1.9";
	
    private static void addWidgetDescriptor(Instance formWidgetInstance, String slotName) {
        // Log.enter(BackwardsCompatibilityProjectFixups.class, "addWidgetDescriptor", formWidgetInstance, slotName);
        Instance propertyList = (Instance) ModelUtilities.getDirectOwnSlotValue(formWidgetInstance, "property_list");
        Iterator i = ModelUtilities.getDirectOwnSlotValues(propertyList, "properties").iterator();
        boolean foundIt = false;
        while (i.hasNext() && !foundIt) {
            Instance instance = (Instance) i.next();
            String name = (String) ModelUtilities.getDirectOwnSlotValue(instance, "name");
            if (name.equals(slotName)) {
                foundIt = true;
            }
        }
        if (!foundIt) {
            KnowledgeBase kb = formWidgetInstance.getKnowledgeBase();
            WidgetDescriptor d = WidgetDescriptor.create(kb);
            ModelUtilities.setOwnSlotValue(d.getInstance(), "name", slotName);
            ModelUtilities.addOwnSlotValue(propertyList, "properties", d.getInstance());
        }
    }

    //    private static void changeInstanceValue(
    //        KnowledgeBase kb,
    //        String className,
    //        String slotNameToCheck,
    //        String slotValue,
    //        String slotNameToChange,
    //        Object oldValue,
    //        Object newValue) {
    //        Cls cls = kb.getCls(className);
    //        Slot slotToCheck = kb.getSlot(slotNameToCheck);
    //        Slot slotToChange = kb.getSlot(slotNameToChange);
    //        Iterator i = cls.getInstances().iterator();
    //        while (i.hasNext()) {
    //            Instance instance = (Instance) i.next();
    //            if (slotValue.equals(instance.getOwnSlotValue(slotToCheck))
    //                && oldValue.equals(instance.getOwnSlotValue(slotToChange))) {
    //                instance.setOwnSlotValue(slotToChange, newValue);
    //                // Log.trace("set " + slotNameToChange + " to " + newValue, Project.class, "changeInstanceValue");
    //            }
    //        }
    //    }
    //
    //    private static void changeWidgetSlotValue(
    //        KnowledgeBase kb,
    //        String name,
    //        String slotNameToChange,
    //        Object oldValue,
    //        Object newValue) {
    //        changeInstanceValue(kb, "Widget", "name", name, slotNameToChange, oldValue, newValue);
    //    }
    //
    //    private static void deleteWidgetsWithSlotValue(KnowledgeBase kb, String slotName, String name) {
    //        Cls cls = kb.getCls("Widget");
    //        Slot slot = kb.getSlot(slotName);
    //        Iterator i = cls.getInstances().iterator();
    //        while (i.hasNext()) {
    //            Instance instance = (Instance) i.next();
    //            if (name.equals(instance.getOwnSlotValue(slot))) {
    //                kb.deleteInstance(instance);
    //                // Log.trace("delete instance " + instance, Project.class, "changeInstanceValue");
    //            }
    //        }
    //    }

    public static void fix(KnowledgeBase kb) {
        if (shouldUpdate(kb)) {
        	Log.getLogger().info("Backwards compatibility fixup for frames project file");
            updateStandardForms(kb);
        }
    }

    private static boolean shouldUpdate(KnowledgeBase kb) {
        return !isCurrentBuild(kb) && !isOwl(kb);
    }

    private static boolean isOwl(KnowledgeBase kb) {
        Instance instance = kb.getInstance("PROJECT");
        edu.stanford.smi.protege.model.Slot slot = kb.getSlot("default_cls_metaclass");
        String value = (String) instance.getOwnSlotValue(slot);
        return value.contains("owl:") || value.contains("/owl#");
    }

    private static Instance getClsWidgetInstance(String name, KnowledgeBase kb) {
        Instance result = null;
        Collection values = ModelUtilities
                .getDirectOwnSlotValues(getProjectInstance(kb), "customized_instance_widgets");
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Instance widgetInstance = (Instance) i.next();
            String widgetSlotName = (String) ModelUtilities.getDirectOwnSlotValue(widgetInstance, "name");
            if (name.equals(widgetSlotName)) {
                result = widgetInstance;
                break;
            }
        }
        return result;
    }

    private static KnowledgeBaseFactory getFactory(KnowledgeBase kb) {
        KnowledgeBaseFactory result = null;
        Instance projectInstance = getProjectInstance(kb);
        Instance sources = (Instance) ModelUtilities.getDirectOwnSlotValue(projectInstance, "sources");
        Iterator i = ModelUtilities.getDirectOwnSlotValues(sources, "properties").iterator();
        while (i.hasNext()) {
            Instance property = (Instance) i.next();
            String name = (String) ModelUtilities.getDirectOwnSlotValue(property, "name");
            if (name.equals("factory_class_name")) {
                String factoryName = (String) ModelUtilities.getDirectOwnSlotValue(property, "string_value");
                result = (KnowledgeBaseFactory) SystemUtilities.newInstance(factoryName);
                break;
            }
        }
        return result;
    }

    private static Instance getProjectInstance(KnowledgeBase kb) {
        Instance instance = kb.getInstance("PROJECT");
        Assert.assertNotNull("instance", instance);
        return instance;
    }

    private static KnowledgeBase getTemplateKnowledgeBase(KnowledgeBase projectKB) {
        Collection errors = new ArrayList();
        Reader clsesReader = Files.getSystemClsesReader();
        KnowledgeBaseFactory factory = getFactory(projectKB);
        String path = (factory == null) ? (String) null : factory.getProjectFilePath();
        Reader instancesReader;
        if (path == null) {
            instancesReader = Files.getSystemInstancesReader();
        } else {
            instancesReader = FileUtilities.getResourceReader(factory.getClass(), path);
        }
        return new ClipsKnowledgeBaseFactory().loadKnowledgeBase(clsesReader, instancesReader, errors);
    }

    /*
     * This is a hack.  We don't have access to the domain kb here so we can't get a
     * Cls object and ask it directly if it is a metaclass.  Instead we hunt for a widget to handle the
     * "template slots" slot.  If it is found then we assume that the widget is on a metaclass form.
     *
     * The domain class kb should probably be passed into the "fixups" class.
     */
    private static boolean isClsMetaclass(Instance formWidgetInstance) {
        boolean result = false;
        Instance propertyListInstance = (Instance) ModelUtilities.getDirectOwnSlotValue(formWidgetInstance,
                "property_list");
        Iterator i = ModelUtilities.getDirectOwnSlotValues(propertyListInstance, "properties").iterator();
        while (i.hasNext()) {
            Instance slotWidgetInstance = (Instance) i.next();
            String slotName = (String) ModelUtilities.getDirectOwnSlotValue(slotWidgetInstance, "name");
            if (slotName.equals(Model.Slot.DIRECT_TEMPLATE_SLOTS)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static boolean isCurrentBuild(KnowledgeBase kb) {
    	String currentVersion = Text.getVersion();        
        String kbBuild = kb.getBuildString();
        return kbBuild == null || currentVersion.compareTo(MIN_VERSION) >= 0;
    }

    /*
     * This is a hack.  We don't have access to the domain kb here so we can't get a
     * Cls object and ask it directly if it is a slot metaclass.  Instead we hunt for a widget to handle the
     * "value type" slot.  If it is found then we assume that the widget is on a slot metaclass form.
     *
     * The domain class kb should probably be passed into the "fixups" class.
     */
    private static boolean isSlotMetaclass(Instance formWidgetInstance) {
        boolean result = false;
        Instance propertyListInstance = (Instance) ModelUtilities.getDirectOwnSlotValue(formWidgetInstance,
                "property_list");
        Iterator i = ModelUtilities.getDirectOwnSlotValues(propertyListInstance, "properties").iterator();
        while (i.hasNext()) {
            Instance slotWidgetInstance = (Instance) i.next();
            String slotName = (String) ModelUtilities.getDirectOwnSlotValue(slotWidgetInstance, "name");
            if (slotName.equals(Model.Slot.VALUE_TYPE)) {
                result = true;
                break;
            }
        }
        return result;
    }

    //    private static void removeSlot(KnowledgeBase kb, String slotName) {
    //        Slot slot = kb.getSlot(slotName);
    //        if (slot != null) {
    //            kb.deleteSlot(slot);
    //        }
    //    }
    //
    //    private static void renameWidget(KnowledgeBase kb, String oldWidgetName, String newWidgetName) {
    //        changeInstanceValue(
    //            kb,
    //            "Widget",
    //            "widget_class_name",
    //            oldWidgetName,
    //            "widget_class_name",
    //            oldWidgetName,
    //            newWidgetName);
    //    }
    //
    //    private static void renameWidget(KnowledgeBase kb, String name, String oldWidgetName, String newWidgetName) {
    //        changeWidgetSlotValue(kb, name, "widget_class_name", oldWidgetName, newWidgetName);
    //    }
    //
    //    private static void renameWidgets13to14(KnowledgeBase kb) {
    //        removeSlot(kb, "all_knowledge_base_factory_names");
    //        removeSlot(kb, "widget_mapper_properties");
    //
    //        renameWidget(
    //            kb,
    //            Model.Slot.DEFAULTS,
    //            "edu.stanford.smi.protege.widget.StringListWidget",
    //            "edu.stanford.smi.protege.widget.DefaultValuesWidget");
    //        renameWidget(
    //            kb,
    //            Model.Slot.DOCUMENTATION,
    //            "edu.stanford.smi.protege.widget.TextAreaWidget",
    //            "edu.stanford.smi.protege.widget.DocumentationWidget");
    //        renameWidget(
    //            kb,
    //            Model.Slot.DIRECT_TEMPLATE_SLOTS,
    //            "edu.stanford.smi.protege.ui.SlotBindingsWidget",
    //            "edu.stanford.smi.protege.widget.TemplateSlotsWidget");
    //        renameWidget(
    //            kb,
    //            Model.Slot.ROLE,
    //            "edu.stanford.smi.protege.ui.RoleWidget",
    //            "edu.stanford.smi.protege.widget.RoleWidget");
    //        renameWidget(
    //            kb,
    //            Model.Slot.MAXIMUM_CARDINALITY,
    //            "edu.stanford.smi.protege.ui.MaximumCardinalityWidget",
    //            "edu.stanford.smi.protege.widget.MaximumCardinalityWidget");
    //        renameWidget(
    //            kb,
    //            Model.Slot.NUMERIC_MINIMUM,
    //            "edu.stanford.smi.protege.widget.NumberFieldWidget",
    //            "edu.stanford.smi.protege.widget.NumericMinimumWidget");
    //        renameWidget(
    //            kb,
    //            Model.Slot.NUMERIC_MAXIMUM,
    //            "edu.stanford.smi.protege.widget.NumberFieldWidget",
    //            "edu.stanford.smi.protege.widget.NumericMaximumWidget");
    //        renameWidget(
    //            kb,
    //            Model.Slot.NUMERIC_MAXIMUM,
    //            "edu.stanford.smi.protege.widget.NumberFieldWidget",
    //            "edu.stanford.smi.protege.widget.NumericMaximumWidget");
    //
    //        renameWidget(kb, "Classes", "edu.stanford.smi.protege.ui.ClsesTab", "edu.stanford.smi.protege.widget.ClsesTab");
    //        renameWidget(kb, "Slots", "edu.stanford.smi.protege.ui.SlotsTab", "edu.stanford.smi.protege.widget.SlotsTab");
    //        renameWidget(kb, "Forms", "edu.stanford.smi.protege.ui.FormsTab", "edu.stanford.smi.protege.widget.FormsTab");
    //        renameWidget(
    //            kb,
    //            "Instances",
    //            "edu.stanford.smi.protege.ui.InstancesTab",
    //            "edu.stanford.smi.protege.widget.InstancesTab");
    //
    //        changeWidgetSlotValue(kb, "Slots", "is_hidden", Boolean.TRUE, Boolean.FALSE);
    //        changeWidgetSlotValue(kb, Model.Facet.CONSTRAINTS, "name", Model.Facet.CONSTRAINTS, Model.Slot.CONSTRAINTS);
    //
    //        deleteWidgetsWithSlotValue(kb, "widget_class_name", "edu.stanford.smi.protege.ui.FacetsTab");
    //    }
    //
    //    private static void renameWidgets14to15(KnowledgeBase kb) {
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.rdf.uri.widget.URIInstanceWidget",
    //            "edu.stanford.smi.protegex.widget.uri.URIWidget");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.imagemap.ImageMapToSymbolWidget",
    //            "edu.stanford.smi.protegex.widget.imagemap.ImageMapWidget");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.diagrams.DiagramWidget",
    //            "edu.stanford.smi.protegex.widget.diagram.DiagramWidget");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.containswidget.ContainsWidget",
    //            "edu.stanford.smi.protegex.widget.contains.ContainsWidget");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.sliderwidget.SliderWidget",
    //            "edu.stanford.smi.protegex.widget.slider.SliderWidget");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.instancetables.InstanceTableWidget",
    //            "edu.stanford.smi.protegex.widget.instancetable.InstanceTableWidget");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.instancetables.InstanceRowWidget",
    //            "edu.stanford.smi.protegex.widget.instancetable.InstanceRowWidget");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.scatterbox.SingleObjectEntryScatterboxWidget",
    //            "edu.stanford.smi.protegex.widget.scatterbox.SingleObjectEntryScatterboxWidget");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.pal.tab.main.PALTab",
    //            "edu.stanford.smi.protegex.widget.pal.PalConstraintsTab");
    //        renameWidget(
    //            kb,
    //            "edu.stanford.smi.protegex.pal.tab.main.QueryTab",
    //            "edu.stanford.smi.protegex.widget.pal.PalQueriesTab");
    //    }

    private static boolean replaceFormWidget(String name, KnowledgeBase projectKB, KnowledgeBase templateProjectKB) {
        Instance templateClsWidget = getClsWidgetInstance(name, templateProjectKB);
        Instance projectClsWidget = getClsWidgetInstance(name, projectKB);
        if (projectClsWidget == null) {
            Cls widgetCls = projectKB.getCls("Widget");
            projectClsWidget = projectKB.createInstance(null, widgetCls);
            ModelUtilities.setOwnSlotValue(projectClsWidget, "name", name);
            Instance projectInstance = getProjectInstance(projectKB);
            ModelUtilities.addOwnSlotValue(projectInstance, "customized_instance_widgets", projectClsWidget);
            // Log.trace("added customization for new class " + name, BackwardsCompatibilityProjectFixups.class, "replaceFormWidget");
        }
        Instance templatePropertyList = (Instance) ModelUtilities.getDirectOwnSlotValue(templateClsWidget,
                "property_list");
        Instance newPropertyList = (Instance) templatePropertyList.deepCopy(projectKB, null);
        ModelUtilities.setOwnSlotValue(projectClsWidget, "property_list", newPropertyList);
        return templateClsWidget != null;
    }

    private static void updateStandardForms(KnowledgeBase projectKB) {
        KnowledgeBase templateProjectKB = getTemplateKnowledgeBase(projectKB);
        Instance templateProjectInstance = getProjectInstance(templateProjectKB);
        // Instance projectInstance = getProjectInstance(projectKB);
        Iterator i = ModelUtilities.getDirectOwnSlotValues(templateProjectInstance, "customized_instance_widgets")
                .iterator();
        while (i.hasNext()) {
            Instance widgetInstance = (Instance) i.next();
            String widgetClsName = (String) ModelUtilities.getDirectOwnSlotValue(widgetInstance, "name");
            boolean changed = replaceFormWidget(widgetClsName, projectKB, templateProjectKB);
            // Log.trace(widgetClsName + " form changed= " + changed, BackwardsCompatibilityProjectFixups.class, "updateStandardForms");
            if (!changed) {
                if (isClsMetaclass(widgetInstance)) {
                    addWidgetDescriptor(widgetInstance, Model.Slot.DIRECT_TYPES);
                } else if (isSlotMetaclass(widgetInstance)) {
                    addWidgetDescriptor(widgetInstance, Model.Slot.DIRECT_TEMPLATE_SLOTS);
                    addWidgetDescriptor(widgetInstance, Model.Slot.CONSTRAINTS);
                    addWidgetDescriptor(widgetInstance, Model.Slot.DIRECT_SUBSLOTS);
                    addWidgetDescriptor(widgetInstance, Model.Slot.DIRECT_SUPERSLOTS);
                    addWidgetDescriptor(widgetInstance, Model.Slot.DIRECT_DOMAIN);
                    addWidgetDescriptor(widgetInstance, Model.Slot.INVERSE);
                    addWidgetDescriptor(widgetInstance, Model.Slot.VALUES);
                    addWidgetDescriptor(widgetInstance, Model.Slot.MINIMUM_CARDINALITY);
                    addWidgetDescriptor(widgetInstance, Model.Slot.ASSOCIATED_FACET);
                }
            }
        }
    }

	public void fixProject(KnowledgeBase internalKB) {
		fix(internalKB);		
	}

	public String getName() {		
		return "Frames 1.8 Project Backwards Compatibility Fix";
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
