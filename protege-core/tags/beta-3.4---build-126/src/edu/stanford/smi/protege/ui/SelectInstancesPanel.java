package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to select a set of instances from all of the instances of a given set of classes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectInstancesPanel extends JComponent {
    //ESCA-JAVA0098 
    protected JTree _clsTree;
    protected JList _instanceList;
    protected JComboBox _directAllInstanceComboBox;

    private static String DIRECT_INSTANCES_TEXT = "Direct Instances";
    private static String ALL_INSTANCES_TEXT = "All Instances";

    // static to remember the state across dialog openings.
    private static Object _oldDirectAllInstancesState = DIRECT_INSTANCES_TEXT;

    protected SelectInstancesPanel(KnowledgeBase kb, Collection clses) {
        setPreferredSize(new Dimension(500, 300));
        if (clses.isEmpty()) {
            clses = kb.getRootClses();
        }
        createWidgets(kb, clses);
        fixRenderer();
    }

    protected LabeledComponent createClsesLabeledComponent(KnowledgeBase kb, Collection clses) {
        LabeledComponent clsesComponent = new LabeledComponent("Allowed Classes", new JScrollPane(_clsTree));
        clsesComponent.setFooterComponent(new ClsTreeFinder(kb, _clsTree));
        return clsesComponent;
    }

    protected JComponent createClsTree(Collection clses) {
        LazyTreeRoot root = new ParentChildRoot(clses);
        _clsTree = ComponentFactory.createSelectableTree(null, root);
        _clsTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent event) {
                loadInstances();
            }
        });
        FrameRenderer renderer = FrameRenderer.createInstance();
        renderer.setDisplayDirectInstanceCount(true);
        _clsTree.setCellRenderer(renderer);
        int rows = _clsTree.getRowCount();
        int diff = rows - clses.size();
        for (int i = rows - 1; i > diff; --i) {
            _clsTree.expandRow(i);
        }
        _clsTree.setSelectionRow(0);
        return _clsTree;
    }

    private void fixRenderer() {
        boolean displayType = _directAllInstanceComboBox.getSelectedItem().equals(ALL_INSTANCES_TEXT);
        FrameRenderer frameRenderer = (FrameRenderer) _instanceList.getCellRenderer();
        frameRenderer.setDisplayType(displayType);
    }

    protected LabeledComponent createInstanceLabeledComponent() {
        LabeledComponent c = new LabeledComponent(null, new JScrollPane(_instanceList));
        c.setHeaderComponent(createDirectAllInstanceComboBox(), BorderLayout.WEST);
        c.setFooterComponent(new ListFinder(_instanceList, "Find Instance"));
        return c;
    }

    protected JComboBox createDirectAllInstanceComboBox() {
        _directAllInstanceComboBox = ComponentFactory.createComboBox();
        _directAllInstanceComboBox.addItem(DIRECT_INSTANCES_TEXT);
        _directAllInstanceComboBox.addItem(ALL_INSTANCES_TEXT);
        loadState();
        _directAllInstanceComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                loadInstances();
                fixRenderer();
                saveState();
            }
        });
        return _directAllInstanceComboBox;
    }

    private void saveState() {
        _oldDirectAllInstancesState = _directAllInstanceComboBox.getSelectedItem();
    }

    private void loadState() {
        _directAllInstanceComboBox.setSelectedItem(_oldDirectAllInstancesState);
    }

    protected JComponent createInstanceList() {
        _instanceList = ComponentFactory.createList(null);
        _instanceList.setCellRenderer(FrameRenderer.createInstance());
        _instanceList.addMouseListener(new ModalDialogCloseDoubleClickAdapter());
        return _instanceList;
    }

    protected void createWidgets(KnowledgeBase kb, Collection clses) {
        createInstanceList();
        LabeledComponent instancesComponent = createInstanceLabeledComponent();

        createClsTree(clses);
        LabeledComponent clsesComponent = createClsesLabeledComponent(kb, clses);

        JSplitPane main = ComponentFactory.createLeftRightSplitPane(clsesComponent, instancesComponent);
        main.setDividerLocation(WIDTH / 2);
        setLayout(new BorderLayout());
        add(main);
    }

    protected SimpleListModel getInstanceModel() {
        return (SimpleListModel) _instanceList.getModel();
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(_instanceList);
    }

    protected void loadInstances() {
        ArrayList instances = new ArrayList();
        Iterator i = ComponentUtilities.getSelection(_clsTree).iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            instances.addAll(getInstances(cls));
        }
        Collections.sort(instances, new FrameComparator());
        getInstanceModel().setValues(instances);
        if (!instances.isEmpty()) {
            _instanceList.setSelectedIndex(0);
        }
    }

    protected Collection<Instance> getInstances(Cls cls) {
        boolean direct = _directAllInstanceComboBox.getSelectedItem().equals(DIRECT_INSTANCES_TEXT);
        return (direct) ? cls.getDirectInstances() : cls.getInstances();
    }
}
