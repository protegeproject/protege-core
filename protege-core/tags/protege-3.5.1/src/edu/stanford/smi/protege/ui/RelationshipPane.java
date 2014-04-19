package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.*;

/**
 * A panel to display any "relationship between frames.  This relationship can be a single slot or all slots.
 */

public class RelationshipPane extends SelectableContainer {

    private static final long serialVersionUID = -6177511814335102076L;
    private Frame _frame;
    private Slot _slot;

    public RelationshipPane(Action doubleClickAction) {
        SelectableTree tree = ComponentFactory.createSelectableTree(doubleClickAction);
        tree.setCellRenderer(FrameRenderer.createInstance());
        setSelectable(tree);
        add(ComponentFactory.createScrollPane(tree), BorderLayout.CENTER);
    }

    public Frame getFrame() {
        return _frame;
    }

    public Slot getSlot() {
        return _slot;
    }

    protected SelectableTree getTree() {
        return (SelectableTree) getSelectable();
    }

    public void load(Frame frame, Slot slot) {
        if (frame != _frame || slot != _slot) {
            _frame = frame;
            _slot = slot;
            rebuild();
        }
    }

    protected void rebuild() {
        getTree().setRoot(new ReferenceRoot(_frame.getKnowledgeBase(), _frame, _slot));
        getTree().setSelectionRow(0);
    }
}
