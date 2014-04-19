package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ViewSelector extends JComponent {
    private static final long serialVersionUID = 296789933860074572L;
    private JComboBox combobox;
    private ProjectView projectView;
    private ItemListener itemListener = createChangeViewAction();
    private JToolBar toolBar;
    private ButtonGroup buttonGroup = new ButtonGroup();
    private Map descriptorToButtonMap = new HashMap();

    public ViewSelector(ProjectView projectView) {
        this.projectView = projectView;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        combobox = ComponentFactory.createComboBox();
        ComboBoxModel model = createModel();
        combobox.setModel(model);
        combobox.addItemListener(itemListener);
        setOpaque(false);
        combobox.setRenderer(new TabRenderer(projectView));
        combobox.setPreferredSize(new Dimension(150, 10));
        Box box = Box.createHorizontalBox();
        box.add(ComponentFactory.createLabel("View:"));
        box.add(Box.createHorizontalStrut(2));
        box.add(combobox);
        box.add(Box.createHorizontalStrut(3));
        toolBar = ComponentFactory.createToolBar();
        addButtonsToToolbar();
        box.add(toolBar);
        add(box);

        projectView.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                TabWidget widget = ViewSelector.this.projectView.getSelectedTab();
                if (widget != null) {
                    setSelection(widget);
                }
            }
        });
    }

    private void addButtonsToToolbar() {
        Iterator i = getCurrentDescriptors().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            addButtonForExistingTab(d);
        }

    }

    public void reload() {
        descriptorToButtonMap.clear();
        buttonGroup = new ButtonGroup();
        toolBar.removeAll();
        addButtonsToToolbar();
    }

    private ComboBoxModel createModel() {
        Project project = projectView.getProject();
        Collection tabs = project.getTabWidgetDescriptors();
        Collection modelElements = new ArrayList();

        modelElements.addAll(getCurrentDescriptors());
        modelElements.addAll(getPotentialDescriptors(tabs));

        return new DefaultComboBoxModel(modelElements.toArray());
    }

    private Collection getCurrentDescriptors() {
        Collection currentDescriptors = new ArrayList();
        Iterator i = projectView.getTabs().iterator();
        while (i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            WidgetDescriptor d = tab.getDescriptor();
            currentDescriptors.add(d);
        }
        return currentDescriptors;
    }

    private Collection getPotentialDescriptors(Collection tabDescriptors) {
        Collection potentialTabs = new ArrayList();
        Iterator i = tabDescriptors.iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            String className = d.getWidgetClassName();
            TabWidget tab = projectView.getTabByClassName(className);
            if (tab == null) {
                Collection errors = new ArrayList();
                if (WidgetUtilities.isSuitableTab(className, projectView.getProject(), errors)) {
                    potentialTabs.add(d);
                }
            }
        }
        return potentialTabs;
    }

    private ItemListener createChangeViewAction() {
        return new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    WidgetDescriptor d = (WidgetDescriptor) e.getItem();
                    showView(d);
                }
            }
        };
    }

    private void showView(WidgetDescriptor descriptor) {
        AbstractButton button = (AbstractButton) descriptorToButtonMap.get(descriptor);
        if (button == null) {
            button = addButton(descriptor.getWidgetClassName());
        }
        button.doClick();
    }

    public void addButtonForExistingTab(WidgetDescriptor d) {
        TabWidget widget = projectView.getTabByClassName(d.getWidgetClassName());
        AbstractButton button = addButton(widget);
        if (projectView.getSelectedTab() == widget) {
            button.setSelected(true);
        }
    }

    public AbstractButton addButton(String className) {
        WidgetDescriptor d = projectView.getProject().getTabWidgetDescriptor(className);
        d.setVisible(true);
        TabWidget widget = projectView.addTab(d);
        return addButton(widget);
    }

    private AbstractButton addButton(final TabWidget widget) {
        Icon icon = widget.getIcon();
        Action action = new AbstractAction(widget.getLabel(), icon) {
            private static final long serialVersionUID = -6284395473725147927L;

            public void actionPerformed(ActionEvent event) {
                projectView.setSelectedTab(widget);
            }
        };
        AbstractButton button = ComponentFactory.addToggleToolBarButton(toolBar, action);
        if (icon == null) {
            button.setText(widget.getLabel());
        }
        descriptorToButtonMap.put(widget.getDescriptor(), button);
        buttonGroup.add(button);
        button.setRolloverEnabled(false);
        button.setBorderPainted(true);
        toolBar.setRollover(false);
        return button;
    }

    private void setSelection(TabWidget widget) {
        combobox.removeItemListener(itemListener);
        combobox.setSelectedItem(widget.getDescriptor());
        combobox.addItemListener(itemListener);

        AbstractButton button = (AbstractButton) descriptorToButtonMap.get(widget.getDescriptor());
        if (button != null) {
            button.setSelected(true);
        }
    }
}

class TabRenderer extends DefaultRenderer {
    private static final long serialVersionUID = 2875667491321456107L;
    private ProjectView projectView;

    TabRenderer(ProjectView view) {
        // Log.enter(this, "TabRenderer");
        projectView = view;
    }

    public void load(Object o) {
        if (o instanceof WidgetDescriptor) {
            WidgetDescriptor d = (WidgetDescriptor) o;
            String longClassName = d.getWidgetClassName();
            TabWidget tab = projectView.getTabByClassName(longClassName);
            if (tab == null) {
                String shortClassName = StringUtilities.getShortClassName(longClassName);
                if (shortClassName.endsWith("Tab")) {
                    shortClassName = shortClassName.substring(0, shortClassName.length() - 3);
                }
                setMainText(shortClassName);
                setMainIcon(null);
            } else {
                setMainText(tab.getLabel());
                setMainIcon(tab.getIcon());
            }
        } else {
            setMainText(o.toString());
        }
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}