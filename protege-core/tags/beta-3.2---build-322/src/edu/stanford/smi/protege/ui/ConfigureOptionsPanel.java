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
    private JCheckBox journalingEnabledCheckBox;
    private JCheckBox prettyPrintSlotWidgetLabelsCheckBox;
    private JCheckBox tabbedInstanceFormCheckBox;

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
        add(c);

    }

    private JComponent createJournalingEnabledCheckBox() {
        journalingEnabledCheckBox = ComponentFactory.createCheckBox();
        journalingEnabledCheckBox.setText("Enable Journaling");
        journalingEnabledCheckBox.setSelected(_project.isJournalingEnabled());
        return journalingEnabledCheckBox;
    }

    private JComponent createPrettyPrintSlotWidgetLabelsCheckBox() {
        prettyPrintSlotWidgetLabelsCheckBox = ComponentFactory.createCheckBox();
        prettyPrintSlotWidgetLabelsCheckBox.setText("Capitalize Slot Widget Labels");
        prettyPrintSlotWidgetLabelsCheckBox.setSelected(_project.getPrettyPrintSlotWidgetLabels());
        return prettyPrintSlotWidgetLabelsCheckBox;
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
        tabbedInstanceFormCheckBox = ComponentFactory.createCheckBox("Used Tabbed Forms for Multi-Type Instances");
        setValue(tabbedInstanceFormCheckBox, _project.getTabbedInstanceFormLayout());
        return tabbedInstanceFormCheckBox;
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
        _project.setJournalingEnabled(getValue(journalingEnabledCheckBox));
        _project.setPrettyPrintSlotWidgetLabels(getValue(prettyPrintSlotWidgetLabelsCheckBox));
        _project.setTabbedInstanceFormLayout(getValue(tabbedInstanceFormCheckBox));
    }

    private static void setValue(JCheckBox box, boolean value) {
        box.setSelected(value);
    }

    public boolean validateContents() {
        return true;
    }
}