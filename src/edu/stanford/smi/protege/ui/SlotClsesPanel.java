package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to display all of the classes that a given slot is directly attached to.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 * @author    Holger Knublauch  <holger@smi.stanford.edu>  (support for hidden classes)
 */
public class SlotClsesPanel extends JComponent {
    private static final long serialVersionUID = 3204543839780790235L;
    private JList _list;

    public SlotClsesPanel(Project project) {
        setLayout(new BorderLayout());
        _list = createList();
        LabeledComponent c = new LabeledComponent("Classes", ComponentFactory.createScrollPane(_list));
        add(c);
    }

    private JList createList() {
        JList list = ComponentFactory.createList(ModalDialog.getCloseAction(this));
        list.setCellRenderer(FrameRenderer.createInstance());
        return list;
    }

    public void setSlot(Slot slot) {
        if (slot == null) {
            ComponentUtilities.setListValues(_list, Collections.EMPTY_LIST);
        } else {
            Collection allClses = slot.getDirectDomain();
            if (slot.getKnowledgeBase().getProject().getDisplayHiddenClasses()) {
                ComponentUtilities.setListValues(_list, allClses);
            } else {
                Collection visibleClses = new ArrayList();
                Iterator i = allClses.iterator();
                while (i.hasNext()) {
                    Cls cls = (Cls) i.next();
                    if (cls.isVisible()) {
                        visibleClses.add(cls);
                    }
                }
                ComponentUtilities.setListValues(_list, visibleClses);
            }
        }
    }
}
