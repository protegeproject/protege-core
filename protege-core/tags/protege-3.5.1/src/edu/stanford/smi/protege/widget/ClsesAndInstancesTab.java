package edu.stanford.smi.protege.widget;

import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * This tab is an attempt to merge the functionality of ClsesTab and the InstancesTab.  Unfortunately I never go around
 * to doing drag and drop on this tab because it is so difficult to do in JDK 1.3.  I hope the use the new JDK 1.4
 * drag and drop capabilities with this tab in the future.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsesAndInstancesTab extends AbstractTabWidget {
    private static final long serialVersionUID = -918873507158261898L;
    private InstanceDisplay _instanceDisplay;
    private ClsesPanel _clsesPanel;
    private DirectInstancesList _directInstancesList;
    private DirectTypesList _directTypesList;
    // private SelectableList _clsList;
    private ClsInverseRelationshipPanel _inverseRelationshipPanel;
    private boolean _isUpdating;

    private JComponent createClsControlPanel() {
        JSplitPane pane = createTopBottomSplitPane("ClsesAndInstancesTab.left, top_bottom", 400);
        pane.setTopComponent(createClsesPanel());
        pane.setBottomComponent(createInverseRelationshipPanel());
        return pane;
    }


    private JComponent createClsesPanel() {
        _clsesPanel = new ClsesPanel(getProject());
        FrameRenderer renderer = FrameRenderer.createInstance();
        renderer.setDisplayDirectInstanceCount(true);
        _clsesPanel.setRenderer(renderer);
        _clsesPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                transmitClsSelection();
            }
        });
        _clsesPanel.getClsesTree().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                transmitClsSelection();
            }
        });
        return _clsesPanel;
    }

    private JComponent createClsSplitter() {
        JSplitPane pane = createLeftRightSplitPane("ClsesAndInstancesTab.left_right", 250);
        pane.setLeftComponent(createClsControlPanel());
        pane.setRightComponent(createInstanceSplitter());
        return pane;
    }

    private JComponent createDirectInstancesList() {
        _directInstancesList = new DirectInstancesList(getProject());
        _directInstancesList.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                if (!_isUpdating) {
                    _isUpdating = true;
                    Collection selection = _directInstancesList.getSelection();
                    Instance selectedInstance;
                    if (selection.size() == 1) {
                        selectedInstance = (Instance) CollectionUtilities.getFirstItem(selection);
                    } else {
                        selectedInstance = null;
                    }
                    _instanceDisplay.setInstance(selectedInstance);
                    _directTypesList.setInstance(selectedInstance);
                    _isUpdating = false;
                }
            }
        });
        setInstanceSelectable((Selectable) _directInstancesList.getDragComponent());
        return _directInstancesList;
    }

    private JComponent createInstanceDisplay() {
        _instanceDisplay = new InstanceDisplay(getProject());
        return _instanceDisplay;
    }

    private JComponent createInstancesPanel() {
        JSplitPane panel = ComponentFactory.createTopBottomSplitPane();
        panel.setTopComponent(createDirectInstancesList());
        panel.setBottomComponent(createDirectTypesList());
        return panel;
    }

    protected JComponent createDirectTypesList() {
        _directTypesList = new DirectTypesList(getProject());
        return _directTypesList;
    }

    private JComponent createInstanceSplitter() {
        JSplitPane pane = createLeftRightSplitPane("ClsesAndInstancesTab.right.left_right", 200);
        pane.setLeftComponent(createInstancesPanel());
        pane.setRightComponent(createInstanceDisplay());
        return pane;
    }

    private JComponent createInverseRelationshipPanel() {
        _inverseRelationshipPanel = new ClsInverseRelationshipPanel(getProject());
        _inverseRelationshipPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                Collection selection = _inverseRelationshipPanel.getSelection();
                if (selection.size() == 1) {
                    Cls cls = (Cls) selection.iterator().next();
                    _clsesPanel.setDisplayParent(cls);
                }
            }
        });
        return _inverseRelationshipPanel;
    }

    public void initialize() {
        setIcon(Icons.getClsAndInstanceIcon());
        setLabel("Classes & Instances");
        add(createClsSplitter());
        setupDragAndDrop();
        transmitClsSelection();
        setClsTree(_clsesPanel.getClsesTree());
    }

    private void setupDragAndDrop() {
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(_directInstancesList,
                DnDConstants.ACTION_COPY_OR_MOVE, new ClsesAndInstancesTabDirectInstancesListDragSourceListener());
        new DropTarget(_clsesPanel.getDropComponent(), DnDConstants.ACTION_COPY_OR_MOVE, new InstanceClsesTreeTarget());
    }

    private void transmitClsSelection() {
        // Log.enter(this, "transmitSelection");
        Collection selection = _clsesPanel.getSelection();
        Instance selectedInstance = null;
        Cls selectedCls = null;
        Cls selectedParent = null;
        if (selection.size() == 1) {
            selectedInstance = (Instance) CollectionUtilities.getFirstItem(selection);
            if (selectedInstance instanceof Cls) {
                selectedCls = (Cls) selectedInstance;
                selectedParent = _clsesPanel.getDisplayParent();
            }
        }
        _inverseRelationshipPanel.setCls(selectedCls, selectedParent);
        _directInstancesList.setClses(selection);

        /*
         ComponentUtilities.setListValues(_clsList, selection);
         if (!selection.isEmpty()) {
         _clsList.setSelectedIndex(0);
         }
         */
        _directInstancesList.clearSelection();
        _instanceDisplay.setInstance(selectedCls);
    }
}