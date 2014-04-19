package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to allow a user to pick one or more classes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectClsesPanel extends JComponent implements Validatable {
    private static final long serialVersionUID = 4752775085919596616L;
    private JTree _tree;
    private boolean _allowsMultiple;

    public SelectClsesPanel(KnowledgeBase kb) {
        this(kb, Collections.EMPTY_SET);
    }

    public SelectClsesPanel(KnowledgeBase kb, DefaultRenderer renderer) {
        this(kb, Collections.EMPTY_SET);
        _tree.setCellRenderer(renderer);
    }

    public SelectClsesPanel(KnowledgeBase kb, Collection clses) {
        this(kb, clses, true);
    }

    public SelectClsesPanel(KnowledgeBase kb, Collection clses, boolean allowsMultiple) {
        _allowsMultiple = allowsMultiple;
        if (clses.isEmpty()) {
            clses = kb.getRootClses();
        }
        _tree = ComponentFactory.createSelectableTree(ModalDialog.getCloseAction(this), new ParentChildRoot(clses));
        _tree.setCellRenderer(FrameRenderer.createInstance());
        int rows = _tree.getRowCount();
        int diff = rows - clses.size();
        for (int i = rows - 1; i > diff; --i) {
            _tree.expandRow(i);
        }
        _tree.setSelectionRow(0);
        setLayout(new BorderLayout());
        add(new JScrollPane(_tree), BorderLayout.CENTER);
        add(new ClsTreeFinder(kb, _tree), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(300, 300));
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(_tree);
    }

    public boolean validateContents() {
        boolean isValid = _allowsMultiple || getSelection().size() <= 1;
        if (!isValid) {
            ModalDialog.showMessageDialog(this, "Only 1 class can be selected", ModalDialog.MODE_CLOSE);
        }
        return isValid;
    }

    public void saveContents() {
        // do nothing
    }
}
