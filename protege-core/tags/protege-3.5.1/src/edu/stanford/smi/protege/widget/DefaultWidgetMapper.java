package edu.stanford.smi.protege.widget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * Determines the mapping between a slot and its default widget.  Widgets declare themselves in their manifest file to
 * be the "default" widget for a particular slot cardinality and type.  If multiple widgets declare for the same 
 * combination the last one wins.  This is by design and allows a plugin author to override the standard system widgets.
 * 
 * Plugin authors can also declare their widget to be the default one for a particular Instance/allowed-class combination.
 * This allows a widget to be the default for, for example, the date class. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultWidgetMapper implements WidgetMapper {
    private Logger log = Log.getLogger(DefaultWidgetMapper.class);
    private final static String METHOD_NAME = "isSuitable";
    private final static Class[] METHOD_ARG_CLASSES = new Class[] { Cls.class, Slot.class, Facet.class };

    private KnowledgeBase _knowledgeBase;

    public DefaultWidgetMapper(KnowledgeBase kb) {
        _knowledgeBase = kb;
    }

    public WidgetDescriptor createWidgetDescriptor(Cls cls, Slot slot, Facet facet) {
        // Log.enter(this, "createWidgetDescriptor", cls, slot, facet);
        WidgetDescriptor d = WidgetDescriptor.create(_knowledgeBase);
        String className = getDefaultWidgetClassName(cls, slot, facet);
        d.setWidgetClassName(className);
        d.setName(slot.getName());
        return d;
    }

    public String getDefaultWidgetClassName(Cls cls, Slot slot, Facet facet) {
        boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
        ValueType type = cls.getTemplateSlotValueType(slot);
        Cls allowedCls = null;
        if (type == ValueType.INSTANCE) {
            Collection allowedClses = cls.getTemplateSlotAllowedClses(slot);
            if (allowedClses.size() == 1) {
                allowedCls = (Cls) CollectionUtilities.getFirstItem(allowedClses);
            }
        }
        // This logic should probably be transfered to PluginUtilities
        String className = PluginUtilities.getDefaultWidgetClassName(isMultiple, type, allowedCls, slot);
        if (className != null && !isSuitable(cls, slot, facet, className)) {
            className = PluginUtilities.getDefaultWidgetClassName(isMultiple, type, allowedCls);
        }
        return className;
    }

    public Collection getSuitableWidgetClassNames(Cls cls, Slot slot, Facet facet) {
        Collection suitableWidgetClassNames = new ArrayList();
        Iterator i = PluginUtilities.getAvailableSlotWidgetClassNames().iterator();
        while (i.hasNext()) {
            String className = (String) i.next();
            if (isSuitable(cls, slot, facet, className)) {
                suitableWidgetClassNames.add(className);
            }
        }
        return suitableWidgetClassNames;
    }

    public boolean isSuitable(Cls cls, Slot slot, Facet facet, String className) {
        boolean isSuitable;
        try {
            Class widgetClass = SystemUtilities.forName(className);
            if (widgetClass == null) {
                isSuitable = false;
                Log.getLogger().warning("Invalid widget class name: " + className);
            } else {
                Method method = widgetClass.getMethod(METHOD_NAME, METHOD_ARG_CLASSES);
                Boolean result = (Boolean) method.invoke(null, new Object[] { cls, slot, facet });
                isSuitable = result.booleanValue();
            }
        } catch (Exception e) {
          log.log(Level.WARNING, "Could not verify isSuitable for widget: " + className +
        		  " class: " + cls + " slot: " + slot, e);
          isSuitable = false;
        }
        return isSuitable;
    }

    public boolean isSuitableWidget(Cls cls, Slot slot, Facet facet, WidgetDescriptor d) {
        return isSuitable(cls, slot, facet, d.getWidgetClassName());
    }

    @Override
	public String toString() {
        return StringUtilities.getClassName(this);
    }
}
