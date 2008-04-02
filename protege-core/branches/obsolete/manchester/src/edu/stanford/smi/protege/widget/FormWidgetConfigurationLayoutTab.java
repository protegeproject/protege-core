package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Layout configuration panel for a FormWidget.  This panel allows the user to select a widget that will take up any
 * extra space when the form is stretched.  By default all widgets consume the extra space equally.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormWidgetConfigurationLayoutTab extends AbstractValidatableComponent {
    private static final String ALL = FormWidget.STRETCH_ALL;
    private static final String NONE = FormWidget.STRETCH_NONE;
    private FormWidget _formWidget;
    private JComboBox _horizontalStretcherComponent;
    private JComboBox _verticalStretcherComponent;

    public FormWidgetConfigurationLayoutTab(FormWidget widget) {
        _formWidget = widget;
        setLayout(new BorderLayout());
        JComponent grid = new JPanel(new GridLayout(2, 1, 10, 10));
        grid.add(createVerticalStretcherComponent());
        grid.add(createHorizontalStretcherComponent());
        add(grid, BorderLayout.NORTH);
    }

    private JComponent createHorizontalStretcherComponent() {
        _horizontalStretcherComponent = ComponentFactory.createComboBox();
        _horizontalStretcherComponent.setRenderer(FrameRenderer.createInstance());
        ArrayList items = new ArrayList(_formWidget.getCls().getVisibleTemplateSlots());
        items.add(0, ALL);
        items.add(1, NONE);
        DefaultComboBoxModel model = new DefaultComboBoxModel(items.toArray());
        _horizontalStretcherComponent.setModel(model);
        setSelection(_horizontalStretcherComponent, _formWidget.getHorizontalStretcher(), ResizingLayout.HORIZONTAL_FILL_DEFAULT);
        return new LabeledComponent("Fill Horizontal Space With:", _horizontalStretcherComponent);
    }

    private JComponent createVerticalStretcherComponent() {
        _verticalStretcherComponent = ComponentFactory.createComboBox();
        _verticalStretcherComponent.setRenderer(FrameRenderer.createInstance());
        ArrayList items = new ArrayList(_formWidget.getCls().getVisibleTemplateSlots());
        items.add(0, ALL);
        items.add(1, NONE);
        DefaultComboBoxModel model = new DefaultComboBoxModel(items.toArray());
        _verticalStretcherComponent.setModel(model);
        setSelection(_verticalStretcherComponent, _formWidget.getVerticalStretcher(), ResizingLayout.VERTICAL_FILL_DEFAULT);
        return new LabeledComponent("Fill Vertical Space With:", _verticalStretcherComponent);
    }

    private String getSelection(JComboBox box) {
        Object o = box.getSelectedItem();
        if (o != ALL && o != NONE) {
            Slot slot = (Slot) o;
            o = slot.getName();
        }
        return o.toString();
    }

    public void saveContents() {
        _formWidget.setHorizontalStretcher(getSelection(_horizontalStretcherComponent));
        _formWidget.setVerticalStretcher(getSelection(_verticalStretcherComponent));
    }

    private void setSelection(JComboBox box, String name, boolean defaultValue) {
        Object o;
        if (name == null) {
            o = defaultValue ? ALL : NONE;
        } else if (name.equals(ALL)) {
            o = ALL;
        } else if (name.equals(NONE)) {
            o = NONE;
        } else {
            o = _formWidget.getKnowledgeBase().getSlot(name);
        }
        box.setSelectedItem(o);
    }

    public boolean validateContents() {
        return true;
    }
}
