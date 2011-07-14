package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Tab to display class forms and allow the user to edit the forms.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormsTab extends AbstractTabWidget {
    private static final long serialVersionUID = -6911540492963136661L;
    private FormsPanel _formsPanel;
    private FormDisplay _formDisplay;

    private JComponent createFormDisplay() {
        _formDisplay = new FormDisplay(getProject());
        return _formDisplay;
    }

    private JComponent createFormsWidget() {
        _formsPanel = new FormsPanel(getProject());
        _formsPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                transmitSelection();
            }
        });
        return _formsPanel;
    }

    private JComponent createMainSplitter() {
        JSplitPane pane = createLeftRightSplitPane("FormsTab.left_right", 250);
        pane.setLeftComponent(createFormsWidget());
        pane.setRightComponent(createFormDisplay());
        return pane;
    }

    public void initialize() {
        setIcon(Icons.getFormIcon());
        setLabel(LocalizedText.getText(ResourceKey.FORMS_VIEW_TITLE));
        add(createMainSplitter());
        transmitSelection();
        setClsTree(_formsPanel.getFormsTree());
    }
    
    public LabeledComponent getLabeledComponent() {
        return _formsPanel.getLabeledComponent();
    }

    /**
     * Selects a given Cls in the classes tree.
     * @param cls  the Cls to select
     */
    public void setSelectedCls(Cls cls) {
        JTree tree = _formsPanel.getFormsTree();
        Collection path = ModelUtilities.getPathToRoot(cls);
        ComponentUtilities.setSelectedObjectPath(tree, path);
    }

    private void transmitSelection() {
        Collection selection = _formsPanel.getSelection();
        if (selection.size() == 1) {
            Cls cls = (Cls) CollectionUtilities.getFirstItem(selection);
            _formDisplay.setWidgetCls(cls);
        } else {
            _formDisplay.setWidgetCls(null);
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isSuitable(Project project, Collection errors) {
        if(project.isMultiUserClient()) {
            errors.add("Forms don't work in multi-user client mode");
            return false;
        }
        return true;
    }
}
