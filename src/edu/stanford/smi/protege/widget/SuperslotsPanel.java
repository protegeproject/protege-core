package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

public class SuperslotsPanel extends SelectableContainer {
    private static final long serialVersionUID = -8861214720299721320L;
    private Project project;
    private Slot slot;
    private SelectableList list;
    private AbstractAction addAction;
    private AbstractAction removeAction;

    private SlotListener slotListener = new SlotAdapter() {
        public void directSuperslotAdded(SlotEvent event) {
            ComponentUtilities.addListValue(list, event.getSuperslot());
        }

        public void directSuperslotRemoved(SlotEvent event) {
            boolean wasEnabled = setNotificationsEnabled(false);
            ComponentUtilities.removeListValue(list, event.getSuperslot());
            setNotificationsEnabled(wasEnabled);
        }
    };

    public SuperslotsPanel(Project project) {
        this.project = project;
        createComponents();
        layoutComponents();
        setSelectable(list);
        
        setPreferredSize(new Dimension(0, 100));
    }

    private void createComponents() {
        list = ComponentFactory.createSelectableList(null);
        list.setCellRenderer(new FrameRenderer());
        addAction = createAddAction();
        removeAction = createRemoveAction();
    }

    private AbstractAction createAddAction() {
        return new AddAction(ResourceKey.SLOT_ADD_SUPERSLOT) {
            private static final long serialVersionUID = -1324800378677177673L;

            public void onAdd() {
                if (slot != null) {
                    addSlots();
                }
            }
        };
    }

    private void addSlots() {
        Collection allowedSlots = new ArrayList(project.getKnowledgeBase().getSlots());
        allowedSlots.remove(slot);
        allowedSlots.removeAll(slot.getSubslots());
        allowedSlots.removeAll(slot.getSuperslots());
        Collection superslots = selectSlots(allowedSlots);
        Iterator i = superslots.iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            slot.addDirectSuperslot(superslot);
        }
        updateModel();
    }

    protected Collection selectSlots(Collection allowedSlots) {
        return DisplayUtilities.pickSlots(this, allowedSlots);
    }

    private AbstractAction createRemoveAction() {
        return new RemoveAction(ResourceKey.SLOT_REMOVE_SUPERSLOT, list) {
            private static final long serialVersionUID = 3522248994702422099L;

            public void onRemove(Collection superslots) {
                removeSlots(superslots);
            }
        };
    }

    private void removeSlots(Collection superslots) {
        try {
            // beginTransaction("Remove superslots from " + slot);
            Iterator i = superslots.iterator();
            while (i.hasNext()) {
                Slot superslot = (Slot) i.next();
                slot.removeDirectSuperslot(superslot);
            }
            updateModel();
        } finally {
            // endTransaction();
        }
    }

    private void layoutComponents() {
        JScrollPane pane = ComponentFactory.createScrollPane(list);
        String superslotsLabel = LocalizedText.getText(ResourceKey.SLOT_BROWSER_SUPERSLOTS_LABEL);
        LabeledComponent c = new LabeledComponent(superslotsLabel, pane);
        c.addHeaderButton(addAction);
        c.addHeaderButton(removeAction);
        setLayout(new BorderLayout());
        add(c, BorderLayout.CENTER);
    }

    public void setSlot(Slot slot, Slot parent) {
        if (this.slot != null) {
            this.slot.removeSlotListener(slotListener);
        }
        this.slot = slot;
        if (this.slot != null) {
            this.slot.addSlotListener(slotListener);
        }
        updateModel();
        addAction.setEnabled(slot != null && slot.isEditable());
        list.setSelectedValue(parent, true);
    }

    public void setDisplayParent(Slot parent) {
        list.setSelectedValue(parent, true);
    }

    private void updateModel() {
        Collection slots = (slot == null) ? Collections.EMPTY_LIST : slot.getDirectSuperslots();
        ListModel model = new SimpleListModel(slots);
        list.setModel(model);
        repaint();
    }
}