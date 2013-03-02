package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.undo.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to display the commands that have been executed an allow the user to undo/redo them.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CommandHistoryPanel extends JPanel implements Disposable {
    private static final long serialVersionUID = -5242899916645126654L;
    private static final String LINE = "------------------------------";
    private static final String INSERT_POINT = LINE + " Current Command Position " + LINE;

    private CommandManager _manager;
    private JList _list;
    private JButton _undoButton;
    private JButton _redoButton;
    private ChangeListener _changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent event) {
            updateModel();
        }
    };

    public CommandHistoryPanel(CommandManager manager) {
        _manager = manager;
        _manager.addChangeListener(_changeListener);
        _list = createList();
        _list.setCellRenderer(new CommandRenderer());
        _undoButton = createUndoButton();
        _redoButton = createRedoButton();
        layoutWidgets();
        setPreferredSize(new Dimension(800, 400));
        updateModel();
    }

    public void dispose() {
        _manager.removeChangeListener(_changeListener);
    }

    private static JList createList() {
        return ComponentFactory.createList(null);
    }

    private static JButton createRedoButton() {
        JButton button = ComponentFactory.createButton(new RedoAction(true));
        button.setText("Redo");

        return button;
    }

    private static JButton createUndoButton() {
        JButton button = ComponentFactory.createButton(new UndoAction(true));
        button.setText("Undo");
        return button;
    }

    //ESCA-JAVA0130 
    public String getTitle() {
        return "Command History";
    }

    private void layoutWidgets() {
        JPanel innerPane = ComponentFactory.createPanel();
        innerPane.setLayout(new GridLayout(1, 2, 10, 10));
        innerPane.add(_undoButton);
        innerPane.add(_redoButton);

        JPanel buttonPane = ComponentFactory.createPanel();
        buttonPane.setLayout(new FlowLayout());
        buttonPane.add(innerPane);

        setLayout(new BorderLayout());
        add(ComponentFactory.createScrollPane(_list), BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
    }

    private void updateModel() {
        SimpleListModel model = new SimpleListModel();
        model.addValues(_manager.getDoneCommands());
        model.addValue(INSERT_POINT);
        model.addValues(_manager.getUndoneCommands());
        _list.setModel(model);
    }

    private static class CommandRenderer extends DefaultRenderer {
        private static final long serialVersionUID = -8428061618792898673L;

        public void load(Object o) {
            if (o instanceof Command) {
                setMainText(((Command) o).getDescription());
            } else {
                setMainText(o.toString());
            }
        }
    }
}
