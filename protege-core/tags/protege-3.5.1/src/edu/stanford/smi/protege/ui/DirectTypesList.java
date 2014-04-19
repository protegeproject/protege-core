package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DirectTypesList extends SelectableContainer {
    private static final long serialVersionUID = 3753480292661208200L;
    private SelectableList list;
    private Instance instance;
    private KnowledgeBase knowledgeBase;
    private AbstractAction addAction;

    private InstanceListener instanceListener = new InstanceListener() {

        public void directTypeAdded(InstanceEvent event) {
            ComponentUtilities.addListValue(list, event.getCls());
        }

        public void directTypeRemoved(InstanceEvent event) {
            ComponentUtilities.removeListValue(list, event.getCls());
        }

    };

    public DirectTypesList(Project project) {
        this.knowledgeBase = project.getKnowledgeBase();
        list = ComponentFactory.createSelectableList(null);
        list.setCellRenderer(new FrameRenderer());
        setSelectable(list);

        LabeledComponent c = new LabeledComponent("Types", new JScrollPane(list));
        c.addHeaderButton(createAddTypeAction());
        c.addHeaderButton(createRemoteTypeAction());
        setLayout(new BorderLayout());
        add(c);
        setPreferredSize(new Dimension(0, 100));

        list.setDragEnabled(true);
        list.setTransferHandler(new FrameTransferHandler());
    }

    private class FrameTransferHandler extends TransferHandler {
        private static final long serialVersionUID = 4409217176875767828L;

        protected Transferable createTransferable(JComponent c) {
            Collection collection = getSelection();
            return collection.isEmpty() ? null : new TransferableCollection(collection);
        }

        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            return true;
        }

        public boolean importData(JComponent component, Transferable data) {
            return true;
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == MOVE) {
                Iterator i = getSelection().iterator();
                while (i.hasNext()) {
                    Cls type = (Cls) i.next();
                    int index = 0;
                    Log.getLogger().info("Move " + type + " to: " + index);
                    instance.moveDirectType(type, index);
                    updateModel();
                }
            }
        }

        public int getSourceActions(JComponent c) {
            return MOVE;
        }
    };

    public void setInstance(Instance newInstance) {
        if (instance != null) {
            instance.removeInstanceListener(instanceListener);
        }
        instance = newInstance;
        if (instance != null) {
            instance.addInstanceListener(instanceListener);
        }
        updateModel();
        updateAddButton();
    }

    public void updateModel() {
        ListModel model;
        if (instance == null) {
            model = new DefaultListModel();
        } else {
            Collection types = instance.getDirectTypes();
            model = new SimpleListModel(types);
        }
        list.setModel(model);
    }

    public void updateAddButton() {
        addAction.setEnabled(instance != null);
    }

    private Action createAddTypeAction() {
        addAction = new AddAction(ResourceKey.CLASS_ADD) {
            private static final long serialVersionUID = -7607219183090478296L;

            public void onAdd() {
                Collection clses = DisplayUtilities.pickClses(DirectTypesList.this, knowledgeBase);
                Iterator i = clses.iterator();
                while (i.hasNext()) {
                    Cls cls = (Cls) i.next();
                    if (!instance.hasType(cls)) {
                    	instance.addDirectType(cls);
                    }
                }
            }
        };
        return addAction;
    }

    private Action createRemoteTypeAction() {
        return new RemoveAction(ResourceKey.CLASS_REMOVE, list) {
            private static final long serialVersionUID = 4441015930033207570L;

            public void onRemove(Object o) {
                Cls cls = (Cls) o;
                instance.removeDirectType(cls);
            }
        };
    }
}