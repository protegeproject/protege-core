package edu.stanford.smi.protege.widget;

import java.awt.dnd.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * The tab for displaying the instances of a class.  The performance of this tab is dreadful if there are a lot of instances
 * of a given class (say > 10000).  The problem is that even though only a few instances are displayed, they are all
 * read out of the database.  Even if we only read a few out somehow, sorting requires that we have access to all of them.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstancesTab extends AbstractTabWidget {
    private static final long serialVersionUID = -3055265677325321173L;
    private InstanceDisplay _instanceDisplay;
    private InstanceClsesPanel _clsesPanel;
    private DirectInstancesList _directInstancesList;
    private DirectTypesList _directTypesList;

    private JComponent createClsesPanel() {
        _clsesPanel = new InstanceClsesPanel(getProject());
        _clsesPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                transmitSelection();
            }
        });

        return _clsesPanel;
    }

    private JComponent createClsSplitter() {
        JSplitPane pane = createLeftRightSplitPane("InstancesTab.left_right", 250);
        pane.setLeftComponent(createClsesPanel());
        pane.setRightComponent(createInstanceSplitter());
        return pane;
    }

    protected JComponent createDirectInstancesList() {
        _directInstancesList = new DirectInstancesList(getProject());
        _directInstancesList.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                Collection selection = _directInstancesList.getSelection();
                Instance selectedInstance;
                if (selection.size() == 1) {
                    selectedInstance = (Instance) CollectionUtilities.getFirstItem(selection);
                } else {
                    selectedInstance = null;
                }
                _instanceDisplay.setInstance(selectedInstance);
                _directTypesList.setInstance(selectedInstance);
            }
        });
        setInstanceSelectable((Selectable) _directInstancesList.getDragComponent());
        return _directInstancesList;
    }

    protected JComponent createDirectTypesList() {
        _directTypesList = new DirectTypesList(getProject());
        return _directTypesList;
    }

    protected JComponent createInstanceDisplay() {
        return new InstanceDisplay(getProject());
    }

    private JComponent createInstancesPanel() {
        JSplitPane panel = ComponentFactory.createTopBottomSplitPane();
        panel.setTopComponent(createDirectInstancesList());
        panel.setBottomComponent(createDirectTypesList());
        return panel;
    }

    private JComponent createInstanceSplitter() {
        JSplitPane pane = createLeftRightSplitPane("InstancesTab.right.left_right", 250);
        pane.setLeftComponent(createInstancesPanel());
        _instanceDisplay = (InstanceDisplay) createInstanceDisplay();
        pane.setRightComponent(_instanceDisplay);
        return pane;
    }
    
    public LabeledComponent getLabeledComponent() {
        return _clsesPanel.getLabeledComponent();
    }

    public void initialize() {
        setIcon(Icons.getInstanceIcon());
        setLabel(LocalizedText.getText(ResourceKey.INSTANCES_VIEW_TITLE));
        add(createClsSplitter());
        transmitSelection();
        setupDragAndDrop();
        setClsTree(_clsesPanel.getDropComponent());
    }

    public void setSelectedCls(Cls cls) {
        _clsesPanel.setSelectedCls(cls);
    }

    public void setSelectedInstance(Instance instance) {
        _clsesPanel.setSelectedCls(instance.getDirectType());
        _directInstancesList.setSelectedInstance(instance);
        _directTypesList.setInstance(instance);
    }

    private void setupDragAndDrop() {
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(_directInstancesList.getDragComponent(),
                DnDConstants.ACTION_COPY_OR_MOVE, new InstancesTabDirectInstancesListDragSourceListener());
        new DropTarget(_clsesPanel.getDropComponent(), DnDConstants.ACTION_COPY_OR_MOVE, new InstanceClsesTreeTarget());
    }

    protected void transmitSelection() {
        WaitCursor cursor = new WaitCursor(this);
        try {
            Collection selection = _clsesPanel.getSelection();
            transmitSelection(selection);
        } finally {
            cursor.hide();
        }
    }

    protected void transmitSelection(Collection selection) {
        _directInstancesList.setClses(selection);
    }

	public DirectInstancesList getDirectInstancesList() {
		return _directInstancesList;
	}
}