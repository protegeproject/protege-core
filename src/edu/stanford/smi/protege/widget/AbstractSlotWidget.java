package edu.stanford.smi.protege.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.resource.Colors;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.DoubleClickListener;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.ShowInstanceListener;
import edu.stanford.smi.protege.util.StringUtilities;

/**
 * Base class for all SlotWidgets. 
 *  
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

public abstract class AbstractSlotWidget extends AbstractWidget implements SlotWidget {
	private static final long serialVersionUID = 1790525704709652862L;

    public static final String READ_ONLY_WIDGET_PROPERTY = "readOnly_configured";
	
    private Collection _buttonInfo;
    private Cls _cls;
    private Slot _slot;
    private Instance _instance;
    private Cls _associatedCls;
    private boolean _isBusy;
    private int _preferredColumns = 0;
    private int _preferredRows = 0;
    private static final int NORMAL_BORDER_SIZE = 5;
    private static final int SELECTED_BORDER_SIZE = 3;
    private static final Border NORMAL_BORDER;
    private DoubleClickListener _doubleClickListener;
    private ShowInstanceListener _showInstanceListener;
    private String TOOLTIP_TEXT_PROPERTY = "tooltip_text";

    static {
        int n = NORMAL_BORDER_SIZE;
        NORMAL_BORDER = BorderFactory.createEmptyBorder(n, n, n, n);
    }

    private ClsListener _clsListener = new ClsAdapter() {
        @Override
		public void templateFacetValueChanged(ClsEvent event) {
        	if (event.isReplacementEvent()) return;
            if (AbstractWidget.equals(event.getSlot(), getSlot())) {
                updateBorder(getValues());
            }
        }
    };
  

    private ClsListener _associatedClsListener = new ClsAdapter() {
        @Override
		public void templateFacetValueChanged(ClsEvent event) {
        	if (event.isReplacementEvent()) return;
            if (AbstractWidget.equals(event.getFacet(), _slot.getAssociatedFacet())) {
                loadValues();
            }
        }
    };

    private static class ButtonInfo {
        //ESCA-JAVA0098 
        Action action;
        boolean defaultState;

        ButtonInfo(Action action, boolean defaultState) {
            this.action = action;
            this.defaultState = defaultState;
        }
    }

    protected AbstractSlotWidget() {
        setNormalBorder();
    }
     

    @Override
	public void dispose() {
        super.dispose();
        getCls().removeClsListener(_clsListener);       
        if (getAssociatedCls() != null)  {
        	getAssociatedCls().removeClsListener(_associatedClsListener);
        }
    }

    protected Action getDoubleClickAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = -1888883411836752455L;

            public void actionPerformed(java.awt.event.ActionEvent event) {
                handleDoubleClick();
            }
        };
    }

    public static int getSelectionBorderSize() {
        return SELECTED_BORDER_SIZE;
    }

    protected void handleDoubleClick() {
        Object o = CollectionUtilities.getFirstItem(getSelection());
        if (o != null) {
            if (_doubleClickListener == null) {
                showInstance((Instance) o);
            } else {
                _doubleClickListener.onDoubleClick(o);
            }
        }
    }

    public void setDoubleClickListener(DoubleClickListener listener) {
        _doubleClickListener = listener;
    }

    public void setInvalidValueBorder() {
        setSelectedBorder(Color.red);
    }

    public void setNormalBorder() {
        setBorder(NORMAL_BORDER);
    }

    public void setSelectedBorder() {
        setSelectedBorder(Colors.getFormColor());
    }

    public void setSelectedBorder(Color c) {
        int n = NORMAL_BORDER_SIZE;
        int s = SELECTED_BORDER_SIZE;
        int d = n - s;

        Border innerBandBorder = BorderFactory.createMatteBorder(s, s, s, s, c);
        Border normalBandBorder = BorderFactory.createEmptyBorder(d, d, d, d);
        Border border = BorderFactory.createCompoundBorder(normalBandBorder, innerBandBorder);
        setBorder(border);
    }

    public String getDefaultToolTip() {
        return getPropertyList().getString(TOOLTIP_TEXT_PROPERTY);
    }

    public void setDefaultToolTip(String tooltip) {
        getPropertyList().setString(TOOLTIP_TEXT_PROPERTY, tooltip);
    }

    protected void updateBorder(Collection values) {
        String text = getDefaultToolTip();
        String invalidInstanceText = null;
        Instance instance = getInstance();
        if (instance != null) {
            invalidInstanceText = getKnowledgeBase().getInvalidOwnSlotValuesText(instance, getSlot(), values);
            if (invalidInstanceText != null) {
                text = invalidInstanceText;
            }
        }
        setToolTipText(text);
        if (invalidInstanceText == null) {
            setNormalBorder();
        } else {
            setInvalidValueBorder();
        }
        repaint();
    }

    public void setShowInstanceListener(ShowInstanceListener listener) {
        _showInstanceListener = listener;
    }

    @Override
	public void showInstance(Instance instance) {
        if (_showInstanceListener == null) {
            super.showInstance(instance);
        } else {
            _showInstanceListener.show(instance);
        }
    }

    public void addButtonConfiguration(Action action) {
        addButtonConfiguration(action, true);
    }

    public void addButtonConfiguration(Action action, boolean defaultState) {
        if (_buttonInfo == null) {
            _buttonInfo = new ArrayList();
        }
        _buttonInfo.add(new ButtonInfo(action, defaultState));
        recordDefault(action, defaultState);
        String configuredDescription = getButtonDescription(action);
        if (configuredDescription != null) {
            action.putValue(Action.SHORT_DESCRIPTION, configuredDescription);
        }
    }

    public boolean allowsMultipleValues() {
        boolean result;
        if (_associatedCls == null) {
            result = _slot.getAllowsMultipleValues();
        } else {
            result = _associatedCls.getTemplateSlotAllowsMultipleValues(_slot);
        }
        return result;
    }

    public boolean configure() {
        WidgetConfigurationPanel panel = createWidgetConfigurationPanel();
        int result = ModalDialog.showDialog(this, panel, "Configure " + getLabel(), ModalDialog.MODE_OK_CANCEL, null,
                false);
        return result == ModalDialog.OPTION_OK;
    }

    public WidgetConfigurationPanel createWidgetConfigurationPanel() {
        WidgetConfigurationPanel widgetPanel = new WidgetConfigurationPanel(this);
        if (_buttonInfo != null) {
            ButtonConfigurationPanel buttonPanel = new ButtonConfigurationPanel(getPropertyList());
            widgetPanel.addTab("Buttons", buttonPanel);
            Iterator i = _buttonInfo.iterator();
            while (i.hasNext()) {
                ButtonInfo info = (ButtonInfo) i.next();
                String name = (String) info.action.getValue(Action.NAME);
                String defaultDescription = (String) info.action.getValue(Action.SHORT_DESCRIPTION);
                buttonPanel.addButton(name, defaultDescription, info.defaultState);
            }
        }
        return widgetPanel;
    }

    protected boolean displayButton(String propertyName) {
        Boolean b = getPropertyList().getBoolean(propertyName);
        return (b == null) ? true : b.booleanValue();
    }

    public boolean displayButton(Action action) {
        Boolean b = getPropertyList().getBoolean(getDisplayPropertyName(action));
        return (b == null) ? true : b.booleanValue();
    }

    public Cls getAssociatedCls() {
        return _associatedCls;
    }

    public String getButtonDescription(Action action) {
        return getPropertyList().getString(
                ButtonConfigurationPanel.getDescriptionPropertyName((String) action.getValue(Action.NAME)));
    }

    public Cls getCls() {
        return _cls;
    }

    private static String getDisplayPropertyName(Action action) {
        return ButtonConfigurationPanel.getDisplayPropertyName((String) action.getValue(Action.NAME));
    }

    public static Object getFirstItem(Collection c) {
        return CollectionUtilities.getFirstItem(c);
    }

    protected String getInvalidValueText(Collection values) {
        String result;
        int count = values.size();
        int min = getMinimumCardinality();
        int max = getMaximumCardinality();
        if (count < min) {
            if (max == 1) {
                result = "Value is required";
            } else {
                result = "At least " + min + " value" + (min == 1 ? " is" : "s are") + " required";
            }
        } else if (max != KnowledgeBase.MAXIMUM_CARDINALITY_UNBOUNDED && count > max) {
            result = "At most " + max + " values are allowed";
        } else {
            result = null;
        }
        return result;
    }

    @Override
	public String getLabel() {
        String label = super.getLabel();
        if (label == null) {
            label = getDefaultLabel();
        }
        return label;
    }

    protected String getDefaultLabel() {
        Slot slot = getSlot();
        String text = slot.getBrowserText();
        if (getProject().getPrettyPrintSlotWidgetLabels()) {
            text = StringUtilities.symbolToLabel(text);
        }
        return text;
    }

    protected int getMaximumCardinality() {
        return getCls().getTemplateSlotMaximumCardinality(getSlot());
    }

    protected int getMinimumCardinality() {
        return getCls().getTemplateSlotMinimumCardinality(getSlot());
    }

    @Override
	public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (_preferredColumns > 0) {
            d.width = _preferredColumns * ComponentUtilities.getStandardColumnWidth();
        }
        if (_preferredRows > 0) {
            d.height = _preferredRows * ComponentUtilities.getStandardRowHeight();
        }
        return d;
    }

    public Slot getSlot() {
        return _slot;
    }

    public boolean isSlotAtCls() {
        return _associatedCls != null;
    }

    public void loadValues() {
        if (getInstance() != null && !_isBusy) {
            _isBusy = true;
            try {
                setWidgetValues();
            } finally {
                _isBusy = false;
            }
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
	@Override
	public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
        if (isDesignTime()) {
            // Log.trace("design time", this, "reshape", getBounds());
            getDescriptor().setBounds(getBounds());
        }
    }

    private void recordDefault(Action action, boolean defaultState) {
        String name = getDisplayPropertyName(action);
        Boolean currentBoolean = getPropertyList().getBoolean(name);
        boolean record = currentBoolean == null && !defaultState;
        if (record) {
            getPropertyList().setBoolean(name, defaultState);
        }
    }

    protected static void setAllowed(AllowableAction action, boolean state) {
        if (action != null) {
            action.setAllowed(state);
        }
    }

    public void setAssociatedCls(Cls cls) {
        if ((_associatedCls == null && cls ==null) 
            || (_associatedCls !=null && _associatedCls.equals(cls))) { 
            return; 
        }
        if (_associatedCls != null) {
            _associatedCls.removeClsListener(_associatedClsListener);
        }
        _associatedCls = cls;
        if (_associatedCls != null) {
            _associatedCls.addClsListener(_associatedClsListener);
        }
        loadValues();
    }

    public void setCls(Cls cls) {
        _cls = cls;
    }

    public void setEditable(boolean b) {
        // do nothing
    }

    public void setInstance(Instance newInstance) {    	
        _instance = newInstance;
        loadValues();
    }

    public void setInstanceValues() {
        if (_slot != null) {
            Collection newValues = new ArrayList(getValues());
            if (_associatedCls == null) {
                Collection indirectOwnSlotValues = new ArrayList(_instance.getOwnSlotValues(_slot));
                Collection oldValues = _instance.getDirectOwnSlotValues(_slot);
                indirectOwnSlotValues.removeAll(oldValues);
                newValues.removeAll(indirectOwnSlotValues);
                _instance.setDirectOwnSlotValues(_slot, newValues);
            } else {
                Slot instanceSlot = (Slot) _instance;
                _associatedCls.setTemplateFacetValues(instanceSlot, _slot.getAssociatedFacet(), newValues);
            }
            updateBorder(newValues);
        }
    }

    protected boolean isDirectValue(Object o) {
        boolean isDirectValue;
        if (_associatedCls == null) {
            isDirectValue = _instance.getDirectOwnSlotValues(_slot).contains(o);
        } else {
            isDirectValue = true;
        }
        return isDirectValue;
    }

    protected boolean areDirectValues(Collection values) {
        boolean areDirectValues;
        if (_associatedCls == null) {
            Collection testValues = new HashSet(values);
            testValues.removeAll(_instance.getDirectOwnSlotValues(_slot));
            areDirectValues = testValues.isEmpty();
        } else {
            areDirectValues = true;
        }
        return areDirectValues;
    }

    public void setPreferredColumns(int nColumns) {
        _preferredColumns = nColumns;
    }

    public void setPreferredRows(int nRows) {
        _preferredRows = nRows;
    }

    @Override
	public void setPreferredSize(Dimension size) {
        super.setPreferredSize(size);
        _preferredColumns = 0;
        _preferredRows = 0;
    }

    public void setSlot(Slot slot) {
        _slot = slot;
    }

    public void valueChanged() {
        if (_instance != null && !_isBusy) {
            _isBusy = true;
            try {
                setInstanceValues();
            } finally {
                _isBusy = false;
            }
        }
    }

    public void setup(final WidgetDescriptor descriptor, boolean isDesignTime, Project project, Cls cls, Slot slot) {
        super.setup(descriptor, isDesignTime, project);
        _cls = cls;
        _slot = slot;
        _cls.addClsListener(_clsListener);
        Dimension d = getSize();
        if (d.width > 0 && d.height > 0) {
            setPreferredSize(d);
        }
    }

    public void setWidgetValues() {
        Collection values;
        if (_associatedCls == null) {
            values = new ArrayList(_instance.getOwnSlotValues(_slot));
            boolean editable = _instance.isEditable();
            // editable &= _instance.getOwnSlotValueType(_slot) == ValueType.CLS
            editable &= _slot.getValueType() == ValueType.CLS || _instance.getOwnSlotAllowsMultipleValues(_slot)
                    || _instance.getDirectType().getTemplateSlotValues(_slot).isEmpty();
            setEditable(editable);
        } else {
            Slot instanceSlot = (Slot) _instance;
            Facet facet = _slot.getAssociatedFacet();
            if (facet == null) {
                values = Collections.EMPTY_LIST;
                setEditable(false);
            } else {
                values = _associatedCls.getTemplateFacetValues(instanceSlot, facet);
                boolean editable = _associatedCls.isEditable();
                setEditable(editable);
            }
        }
        try {
            setValues(values);
            updateBorder(values);
        } catch (Exception e) {
            Log.getLogger().warning(e.toString() + " at setting the widget values for instance " + _instance + " and " + _slot );
            setValues(Collections.EMPTY_LIST);
        }
    }

    public Instance getInstance() {
        return _instance;
    }

    public void setValues(Collection values) {
        // do nothing
    }

    //ESCA-JAVA0130 
    public Collection getValues() {
        // do nothing
        return Collections.EMPTY_LIST;
    }

    public ClsWidget getClsWidget() {
        return (ClsWidget) getParent();
    }

    protected String localizeStandardLabel(String currentLabel, String standardCustomizedLabel, ResourceKey key) {
        if (currentLabel.equals(standardCustomizedLabel) || currentLabel.equals(getDefaultLabel())) {
            currentLabel = LocalizedText.getText(key);
        }
        return currentLabel;
    }
        
    public boolean isReadOnlyConfiguredWidget() {
    	Boolean value = this.getPropertyList().getBoolean(READ_ONLY_WIDGET_PROPERTY);
    	return (value == null ? false : value.booleanValue());
    }
    
    public void setReadOnlyWidget(boolean isReadOnly) {
    	this.getPropertyList().setBoolean(READ_ONLY_WIDGET_PROPERTY, isReadOnly);
    }   
    
}