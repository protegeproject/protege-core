package edu.stanford.smi.protege.ui;

//ESCA*JAVA0100

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * A panel that holds a design time class form and allows the user to set the browser text and change slot widgets.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormDisplay extends JComponent implements Disposable {
    private static final long serialVersionUID = -8236544923188409652L;
    private Project _project;
    private JComponent _mainPanel;
    private SetDisplaySlotPanel _setDisplaySlotPanel;
    private JComboBox _widgetSelectionBox;
    private HeaderComponent _header;
    private ClsWidget _currentWidget;

    /* We need to listen for form changes because the user can do layout operations elsewhere (on the FormsPanel)
     * and we need to update the display with the new form widget.
     */
    private ProjectListener _projectListener = new ProjectAdapter() {
        public void formChanged(ProjectEvent event) {
            Cls cls = event.getCls();
            FormWidget widget = getCurrentWidget();
            if (widget != null && FormDisplay.equals(cls, widget.getCls()) && !haveFocus()) {
                setWidgetCls(cls);
            }
        }
    };

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    private SwitchableItemListener _descriptorChoiceListener = new SwitchableItemListener() {
        public void changed(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                _selectionListener.disable();
                String widgetClassName = (String) _widgetSelectionBox.getSelectedItem();
                if (widgetClassName.equals(WidgetClassNameRenderer.NONE)) {
                    widgetClassName = null;
                }
                getCurrentWidget().replaceSelectedWidget(widgetClassName);
                if (widgetClassName == null) {
                    updateWidgetsBox();
                }
                _selectionListener.enable();
            }
        }
    };

    private SwitchableSelectionListener _selectionListener = new SwitchableSelectionListener() {
        public void changed(SelectionEvent event) {
            updateWidgetsBox();
        }
    };

    public FormDisplay(Project project) {
        _project = project;
        _project.addProjectListener(_projectListener);
        setLayout(new BorderLayout());
        add(createNorthPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private void addCls(Cls cls) {
        _currentWidget = _project.getDesignTimeClsWidget(cls);
        JComponent c = (JComponent) _currentWidget;
        _mainPanel.add(new JScrollPane(c));
        c.revalidate();
        c.repaint();
        loadBrowserKeySlotsBox(cls);
        loadWidgetsBox((SlotWidget) CollectionUtilities.getFirstItem(_currentWidget.getSelection()));
        updateHeader(cls);
        _currentWidget.addSelectionListener(_selectionListener);
    }

    private void updateHeader(Cls cls) {
        JLabel label = (JLabel) _header.getComponent();
        label.setIcon(cls.getIcon());
        label.setText(cls.getBrowserText());
    }

    private JComponent createBrowserKeySelection() {
        _setDisplaySlotPanel = new SetDisplaySlotPanel();
        return _setDisplaySlotPanel;
    }

    private JComponent createMainPanel() {
        _mainPanel = new JPanel();
        _mainPanel.setBorder(ComponentUtilities.getAlignBorder());
        _mainPanel.setLayout(new BorderLayout());
        return _mainPanel;
    }

    private JComponent createNorthPanel() {
        JComponent controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(createBrowserKeySelection(), BorderLayout.WEST);
        controlPanel.add(createWidgetSelection(), BorderLayout.EAST);

        JComponent panel = new JPanel(new BorderLayout());
        panel.add(createHeaderPanel(), BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent createHeaderPanel() {
        String formEditorLabel = LocalizedText.getText(ResourceKey.FORM_EDITOR_TITLE);
        String forClassLabel = LocalizedText.getText(ResourceKey.CLASS_EDITOR_FOR_CLASS_LABEL);
        _header = new HeaderComponent(formEditorLabel, forClassLabel, ComponentFactory.createLabel());
        _header.setColor(Colors.getFormColor());
        return _header;
    }

    private JComponent createWidgetSelection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        _widgetSelectionBox = ComponentFactory.createComboBox();
        _widgetSelectionBox.setRenderer(new WidgetClassNameRenderer());
        _widgetSelectionBox.addItemListener(_descriptorChoiceListener);
        Dimension d = _widgetSelectionBox.getPreferredSize();
        d.width = 250;
        _widgetSelectionBox.setPreferredSize(d);
        String selectedWidgetTypeLabel = LocalizedText.getText(ResourceKey.FORM_EDITOR_SELECTED_WIDGET_TYPE_LABEL);
        panel.add(ComponentFactory.createSmallFontLabel(selectedWidgetTypeLabel));
        panel.add(_widgetSelectionBox);
        return panel;
    }

    public void dispose() {
        _project.removeProjectListener(_projectListener);
    }

    private FormWidget getCurrentWidget() {
        FormWidget widget;
        int count = _mainPanel.getComponentCount();
        if (count == 0) {
            widget = null;
        } else {
            JScrollPane pane = (JScrollPane) _mainPanel.getComponent(0);
            widget = (FormWidget) pane.getViewport().getView();
        }
        return widget;
    }

    private static boolean haveFocus() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        return manager.getFocusOwner() != null;
    }

    private void loadBrowserKeySlotsBox(Cls cls) {
        _setDisplaySlotPanel.setCls(cls);
    }

    private void loadWidgetsBox(SlotWidget widget) {
        String widgetClassName = (widget == null) ? (String) null : widget.getClass().getName();
        _descriptorChoiceListener.disable();
        // HACK: avoid JDK 1.2 swing bug
        if (_widgetSelectionBox.getItemCount() > 0) {
            _widgetSelectionBox.removeAllItems();
        }
        if (widget != null) {
            Cls cls = widget.getCls();
            Slot slot = widget.getSlot();
            _widgetSelectionBox.addItem(WidgetClassNameRenderer.NONE);
            Iterator i = _project.getSuitableWidgetClassNames(cls, slot, null).iterator();
            while (i.hasNext()) {
                _widgetSelectionBox.addItem(i.next());
            }
            _widgetSelectionBox.setSelectedItem(widgetClassName);
        }
        _descriptorChoiceListener.enable();
    }

    private void removeCurrentWidget() {
        if (_currentWidget != null) {
            _currentWidget.removeSelectionListener(_selectionListener);
            _mainPanel.removeAll();
            _currentWidget = null;
            _setDisplaySlotPanel.setCls(null);
            revalidate();
            repaint();
        }
    }

    public void setWidgetCls(Cls cls) {
        removeCurrentWidget();
        if (cls != null) {
            addCls(cls);
        }
    }

    private void updateWidgetsBox() {
        SlotWidget w = (SlotWidget) CollectionUtilities.getFirstItem(getCurrentWidget().getSelection());
        loadWidgetsBox(w);
    }
}
