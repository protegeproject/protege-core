package edu.stanford.smi.protege.ui;

//ESCA*JAVA0100

import java.awt.*;
import java.util.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Component for acquiring and displaying a single instance of a class
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceField extends SelectableContainer {
    private static final long serialVersionUID = -103319789201403123L;
    private LabeledComponent _labeledComponent;
    private SelectableList _listComponent;
    private AllowableAction _viewAction;
    private AllowableAction _createAction;
    private AllowableAction _addAction;
    private AllowableAction _removeAction;
    private AllowableAction _deleteAction;

    private Collection _allowedClses;
    private Project _project;

    private FrameListener _instanceListener = new FrameAdapter() {
        public void browserTextChanged(FrameEvent event) {
            _listComponent.repaint();
        }
    };

    public InstanceField(String label, Collection allowedClses) {
        _allowedClses = new ArrayList(allowedClses);

        Iterator i = _allowedClses.iterator();
        if (i.hasNext()) {
            Cls cls = (Cls) i.next();
            _project = cls.getProject();
        }

        _listComponent = ComponentFactory.createSingleItemList(null);
        setSelectable(_listComponent);
        _listComponent.setCellRenderer(FrameRenderer.createInstance());

        _labeledComponent = new LabeledComponent(label, _listComponent);

        setLayout(new BorderLayout());
        add(_labeledComponent);
    }

    public void createCreateInstanceAction() {
        _createAction = new CreateAction(ResourceKey.INSTANCE_CREATE) {
            private static final long serialVersionUID = 8030442148042692501L;

            public void onCreate() {
                Cls cls = DisplayUtilities.pickConcreteCls(InstanceField.this, getKnowledgeBase(), _allowedClses);
                if (cls != null) {
                    Instance instance = getKnowledgeBase().createInstance(null, cls);
                    _project.show(instance);
                    setInstance(instance);
                }
            }
        };
        _labeledComponent.addHeaderButton(_createAction);
    }

    public void createDeleteInstancesAction() {
        _deleteAction = new DeleteInstancesAction(this);
        _labeledComponent.addHeaderButton(_deleteAction);
    }

    public void createRemoveInstanceAction() {
        _removeAction = new RemoveAction(ResourceKey.INSTANCE_REMOVE, this) {
            private static final long serialVersionUID = -1657976558917409719L;

            public void onRemove(Object o) {
                setInstance(null);
            }
        };
        _labeledComponent.addHeaderButton(_removeAction);
    }

    public void createSelectInstanceAction() {
        _addAction = new AddAction(ResourceKey.INSTANCE_ADD) {
            private static final long serialVersionUID = 1655533054275289305L;

            public void onAdd() {
                Instance instance = DisplayUtilities.pickInstance(InstanceField.this, _allowedClses);
                if (instance != null) {
                    setInstance(instance);
                }
            }
        };
        _labeledComponent.addHeaderButton(_addAction);
    }

    public void createViewInstanceAction() {
        _viewAction = new ViewAction(ResourceKey.INSTANCE_VIEW, this) {
            private static final long serialVersionUID = 9128249468794341888L;

            public void onView(Object o) {
                _project.show((Instance) o);
            }
        };
        _labeledComponent.addHeaderButton(_viewAction);
        _listComponent.addMouseListener(new DoubleClickActionAdapter(_viewAction));
    }

    public void dispose() {
        super.dispose();
        Instance instance = getInstance();
        if (instance != null) {
            instance.removeFrameListener(_instanceListener);
        }
    }

    public Instance getInstance() {
        return (Instance) CollectionUtilities.getFirstItem(ComponentUtilities.getListValues(_listComponent));
    }

    private KnowledgeBase getKnowledgeBase() {
        return _project.getKnowledgeBase();
    }

    public void setEditable(boolean b) {
        _createAction.setAllowed(b);
        _addAction.setAllowed(b);
        _removeAction.setAllowed(b);
        _deleteAction.setAllowed(b);
    }

    public void setInstance(Instance instance) {
        Instance currentInstance = getInstance();
        if (currentInstance != null) {
            currentInstance.removeFrameListener(_instanceListener);
        }
        if (instance != null) {
            instance.addFrameListener(_instanceListener);
        }
        ComponentUtilities.setListValues(_listComponent, CollectionUtilities.createCollection(instance));
        notifySelectionListeners();
    }
}
