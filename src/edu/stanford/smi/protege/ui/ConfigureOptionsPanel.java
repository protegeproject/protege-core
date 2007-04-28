package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A panel to display the global options for the application and allow the user to set them.
 *
 * @author Ray Fergerson
 * @author Jennifer Vendetti
 */
class ConfigureOptionsPanel extends AbstractValidatableComponent {

    private Project _project;
    private JCheckBox _hiddenFramesComponent;
    private JCheckBox _confirmOnRemoveComponent;
    private JCheckBox _isEditableComponent;
    private JCheckBox _updateModificationSlotsComponent;
    private JCheckBox _journalingEnabledCheckBox;
    private JCheckBox _prettyPrintSlotWidgetLabelsCheckBox;
    private JCheckBox _tabbedInstanceFormCheckBox;
    private JCheckBox _addNameOnInstanceFormCheckBox;
    
    ConfigureOptionsPanel(Project project) {
        _project = project;
        setLayout(new BorderLayout());
        JComponent c = new Box(BoxLayout.Y_AXIS);
        c.add(createHiddenClassesComponent());
        c.add(createConfirmOnRemoveComponent());
        c.add(createIsEditableComponent());
        c.add(createUpdateModificationSlotsComponent());
        c.add(createJournalingEnabledCheckBox());
        c.add(createPrettyPrintSlotWidgetLabelsCheckBox());
        c.add(createTabbedInstanceFormComponent());
        c.add(createNameOnInstanceFormComponent());
        add(c);

    }

    private JComponent createJournalingEnabledCheckBox() {
        _journalingEnabledCheckBox = ComponentFactory.createCheckBox();
        _journalingEnabledCheckBox.setText("Enable Journaling");
        _journalingEnabledCheckBox.setSelected(_project.isJournalingEnabled());
        return _journalingEnabledCheckBox;
    }

    private JComponent createPrettyPrintSlotWidgetLabelsCheckBox() {
        _prettyPrintSlotWidgetLabelsCheckBox = ComponentFactory.createCheckBox();
        _prettyPrintSlotWidgetLabelsCheckBox.setText("Capitalize Slot Widget Labels");
        _prettyPrintSlotWidgetLabelsCheckBox.setSelected(_project.getPrettyPrintSlotWidgetLabels());
        return _prettyPrintSlotWidgetLabelsCheckBox;
    }

    private JComponent createConfirmOnRemoveComponent() {
        _confirmOnRemoveComponent = ComponentFactory
                .createCheckBox("Display Confirmation Dialog on 'Remove' Operations");
        setValue(_confirmOnRemoveComponent, _project.getDisplayConfirmationOnRemove());
        return _confirmOnRemoveComponent;
    }

    private JComponent createHiddenClassesComponent() {
        _hiddenFramesComponent = ComponentFactory.createCheckBox("Display Hidden Frames");
        setValue(_hiddenFramesComponent, _project.getDisplayHiddenClasses());
        return _hiddenFramesComponent;
    }

    private JComponent createIsEditableComponent() {
        _isEditableComponent = ComponentFactory.createCheckBox("Allow Knowledge-Base Changes");
        setValue(_isEditableComponent, !_project.isReadonly());
        return _isEditableComponent;
    }

    private JComponent createUpdateModificationSlotsComponent() {
        _updateModificationSlotsComponent = ComponentFactory.createCheckBox("Update Modification Slots");
        setValue(_updateModificationSlotsComponent, _project.getUpdateModificationSlots());
        return _updateModificationSlotsComponent;
    }

    private JComponent createTabbedInstanceFormComponent() {
        _tabbedInstanceFormCheckBox = ComponentFactory.createCheckBox("Used Tabbed Forms for Multi-Type Instances");
        setValue(_tabbedInstanceFormCheckBox, _project.getTabbedInstanceFormLayout());
        return _tabbedInstanceFormCheckBox;
    }

    private JComponent createNameOnInstanceFormComponent() {
        _addNameOnInstanceFormCheckBox = ComponentFactory.createCheckBox("Add :NAME Slot on Instance Forms");
        setValue(_addNameOnInstanceFormCheckBox, _project.getAddNameOnInstanceForm());
        return _addNameOnInstanceFormCheckBox;
    }
    
    private static boolean getValue(JCheckBox box) {
        return box.isSelected();
    }

    public void saveContents() {
        _project.setDisplayHiddenClasses(getValue(_hiddenFramesComponent));
        // _project.setDisplayMultiParentClassIcon(getValue(_multiParentClassIconComponent));
        _project.setDisplayConfirmationOnRemove(getValue(_confirmOnRemoveComponent));
        _project.setIsReadonly(!getValue(_isEditableComponent));
        _project.setUpdateModificationSlots(getValue(_updateModificationSlotsComponent));
        _project.setJournalingEnabled(getValue(_journalingEnabledCheckBox));
        _project.setPrettyPrintSlotWidgetLabels(getValue(_prettyPrintSlotWidgetLabelsCheckBox));
        _project.setTabbedInstanceFormLayout(getValue(_tabbedInstanceFormCheckBox));
        _project.setAddNameOnInstanceForm(getValue(_addNameOnInstanceFormCheckBox));
    }

    private static void setValue(JCheckBox box, boolean value) {
        box.setSelected(value);
    }

    public boolean validateContents() {
        return true;
    }
}