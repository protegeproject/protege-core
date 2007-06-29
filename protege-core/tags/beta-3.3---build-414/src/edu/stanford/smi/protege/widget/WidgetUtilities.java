package edu.stanford.smi.protege.widget;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.plugin.ExportPlugin;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;


/**
 * A collection of utilities useful for creating and working with widgets.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetUtilities {

    public static ClsWidget createClsWidget(WidgetDescriptor descriptor, boolean isDesignTime,
            Project project, Cls cls) {
        ClsWidget widget;
        try {
            Assert.assertNotNull("descriptor", descriptor);
            Assert.assertNotNull("project", project);
            widget = (ClsWidget) SystemUtilities.newInstance(descriptor.getWidgetClassName());
            if (widget == null) {
                widget = new UglyClsWidget();
            }
            widget.setup(descriptor, isDesignTime, project, cls);
            widget.initialize();
        } catch (Exception e) {
            Log.getLogger().warning(Log.toString(e));
            widget = new UglyClsWidget();
        }
        return widget;
    }

    public static SlotWidget createSlotWidget(WidgetDescriptor descriptor, boolean isDesignTime,
            Project project, Cls cls, Slot slot) {
        SlotWidget widget;
        try {
            Assert.assertNotNull("descriptor", descriptor);
            Assert.assertNotNull("project", project);
            widget = (SlotWidget) SystemUtilities.newInstance(descriptor.getWidgetClassName());
            if (widget == null) {
                widget = new UglySlotWidget();
            }
            setupSlotWidget(widget, descriptor, isDesignTime, project, cls, slot);
        } catch (Exception e) {
            Log.getLogger().warning(Log.toString(e));
            widget = new UglySlotWidget(descriptor.getWidgetClassName());
            setupSlotWidget(widget, descriptor, isDesignTime, project, cls, slot);
        }
        return widget;
    }

    public static TabWidget createTabWidget(WidgetDescriptor descriptor, Project project) {
        TabWidget result;
        try {
            Assert.assertNotNull("descriptor", descriptor);
            Assert.assertNotNull("project", project);
            result = (TabWidget) SystemUtilities.newInstance(descriptor.getWidgetClassName());
            if (result == null) {
                result = new UglyTabWidget();
            }
            result.setup(descriptor, project);
            result.initialize();
        } catch (Exception e) {
            Log.getLogger().warning(Log.toString(e));
            result = new UglyTabWidget();
            result.setup(descriptor, project);
            result.initialize();
        }
        return result;
    }

    private static void fixBounds(Widget widget) {
        WidgetDescriptor d = widget.getDescriptor();
        if (d.getBounds() == null) {
            JComponent c = (JComponent) widget;
            Rectangle bounds = new Rectangle(new Point(), c.getPreferredSize());
            d.setBounds(bounds);
            c.setBounds(bounds);
        }
    }

    private static void setupComponent(JComponent c, WidgetDescriptor descriptor) {
        Rectangle bounds = descriptor.getBounds();
        if (bounds != null) {
            c.setBounds(bounds);
        }
    }

    private static void setupSlotWidget(SlotWidget widget, WidgetDescriptor descriptor,
            boolean isDesignTime, Project project, Cls cls, Slot slot) {
        setupComponent((JComponent) widget, descriptor);
        widget.setup(descriptor, isDesignTime, project, cls, slot);
        widget.initialize();
        fixBounds(widget);
    }

    private static final String IS_TAB_SUITABLE_NAME = "isSuitable";
    private static final Class[] IS_TAB_SUITABLE_ARG_CLASSES = new Class[] { Project.class,
            Collection.class };

    public static boolean isSuitableTab(String tabWidgetClassName, Project project,
            Collection errors) {
        boolean isSuitable = false;
        try {
            Class tabWidgetClass = SystemUtilities.forName(tabWidgetClassName);
            Method m = tabWidgetClass.getMethod(IS_TAB_SUITABLE_NAME, IS_TAB_SUITABLE_ARG_CLASSES);
            Object[] args = new Object[] { project, errors };
            Boolean b = (Boolean) m.invoke(null, args);
            isSuitable = b.booleanValue();
        } catch (Exception e) {
            // do nothing
        }
        return isSuitable;
    }

    
    /**
     * Sets all the widgets of an instance form to enabled/disabled according to the enabled argument. 
     * @param instanceDisplay
     * @param enabled
     */
    public static void setEnabledInstanceDisplay(InstanceDisplay instanceDisplay, boolean enabled) {
    	if (instanceDisplay == null)
    		return;
    	
    	Instance inst = instanceDisplay.getCurrentInstance();
    	
    	if (inst == null)
    		return;
    	
    	ClsWidget clsWidget = instanceDisplay.getFirstClsWidget();
    	
    	if (clsWidget == null)
    		return;
    	
    	for (Iterator iter = inst.getOwnSlots().iterator(); iter.hasNext();) {
			Slot slot = (Slot) iter.next();			
			SlotWidget slotWidget = clsWidget.getSlotWidget(slot);
			if (slotWidget != null) {
				((AbstractSlotWidget)slotWidget).setEnabled(enabled);
				//((AbstractSlotWidget)slotWidget).setEditable(enabled);
			}
		}    	
    }
    
    private static final String IS_EXPORT_SUITABLE_METHOD_NAME = "isSuitable";
    //private static final Class[] IS_EXPORT_SUITABLE_METHODS_ARGS = new Class[] { Project.class, Collection.class };
    private static final Class[] IS_EXPORT_SUITABLE_METHODS_ARGS = new Class[] { Project.class };
    
    public static boolean isSuitableExport(Project project, ExportPlugin exportPlugin) {
        boolean isSuitable;
        try {           
            Method method = exportPlugin.getClass().getMethod(IS_EXPORT_SUITABLE_METHOD_NAME, IS_EXPORT_SUITABLE_METHODS_ARGS);            
            Boolean returnValue = (Boolean) method.invoke(exportPlugin, new Object[] { project });
            isSuitable = returnValue.booleanValue();
        } catch (NoSuchMethodException e) {
            isSuitable = true;
        } catch (Exception e) {
            isSuitable = false;
            //Log.getLogger().warning(Log.toString(e));
            Log.getLogger().warning(e.getMessage());
        }
        // Log.getLogger().info("is suitable=" + isSuitable + " " + projectPlugin);
        return isSuitable;
    }

}