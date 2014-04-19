package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * Panel to allow a user to pick multiple instances from a collection
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectInstancesFromCollectionPanel extends JComponent {
    private static final long serialVersionUID = -1103710623651598237L;
    private JList _list;

    SelectInstancesFromCollectionPanel(Collection instances) {
        ArrayList slotList = new ArrayList(instances);
        Collections.sort(slotList, new FrameComparator());
        _list = ComponentFactory.createList(ModalDialog.getCloseAction(this));
        _list.setListData(slotList.toArray());
        _list.setCellRenderer(FrameRenderer.createInstance());
        setLayout(new BorderLayout());
        add(new JScrollPane(_list), BorderLayout.CENTER);
        add(new ListFinder(_list, "Find"), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(300, 300));
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(_list);
    }
}
