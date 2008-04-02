package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * The classes tab.  This tab displays the class hierarchy as a tree and allows the user to select and edit specific
 * classes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 * @author    Holger Knublauch <holger@smi.stanford.edu>  (minor refactorings)
 */
public class ClsesTab extends AbstractTabWidget {
    private ClsesPanel _clsesPanel;
    private ClsInverseRelationshipPanel _inverseRelationshipPanel;
    private InstanceDisplay _instanceDisplay;

    protected JComponent createClsDisplay() {
        _instanceDisplay = new InstanceDisplay(getProject());
        return _instanceDisplay;
    }

    protected ClsesPanel createClsesPanel() {
        ClsesPanel panel = new ClsesPanel(getProject());
        panel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                transmitSelection();
            }
        });
        return panel;
    }

    protected JComponent createClsesSplitter() {
        JSplitPane pane = createTopBottomSplitPane("ClsesTab.left.top_bottom", 400);
        _clsesPanel = createClsesPanel();
        pane.setTopComponent(_clsesPanel);
        _inverseRelationshipPanel = createInverseRelationshipPanel();
        pane.setBottomComponent(_inverseRelationshipPanel);
        return pane;
    }

    protected ClsInverseRelationshipPanel createInverseRelationshipPanel() {
        final ClsInverseRelationshipPanel panel = new ClsInverseRelationshipPanel(getProject());
        initInverseRelationshipPanelListener(panel);
        return panel;
    }

    protected void initInverseRelationshipPanelListener(final ClsInverseRelationshipPanel panel) {
        panel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                Collection selection = panel.getSelection();
                if (selection.size() == 1) {
                    Cls cls = (Cls) CollectionUtilities.getFirstItem(selection);
                    _clsesPanel.setDisplayParent(cls);
                }
            }
        });
    }

    private JComponent createMainSplitter() {
        JSplitPane pane = createLeftRightSplitPane("ClsesTab.left_right", 250);
        pane.setLeftComponent(createClsesSplitter());
        pane.setRightComponent(createClsDisplay());
        return pane;
    }

    public void initialize() {
        setIcon(Icons.getClsIcon());
        setLabel(LocalizedText.getText(ResourceKey.CLASSES_VIEW_TITLE));
        setShortDescription("Domain Ontology");
        add(createMainSplitter());
        setInitialSelection();
        setClsTree(_clsesPanel.getClsesTree());
    }
    
    public void setFinderComponent(JComponent c) {
        _clsesPanel.setFinderComponent(c);
    }

    private void setInitialSelection() {
        if (_clsesPanel != null) {
            transmitSelection();
        }
    }

    protected void transmitSelection() {
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
        _instanceDisplay.setInstance(selectedInstance);
    }
}
