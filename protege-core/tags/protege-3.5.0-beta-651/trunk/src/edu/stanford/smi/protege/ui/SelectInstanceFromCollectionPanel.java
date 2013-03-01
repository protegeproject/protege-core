package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.*;

/**
 *  Panel to allow a user to pick a single instance from a collection of instances.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectInstanceFromCollectionPanel extends JComponent {
    private static final long serialVersionUID = 1189735912135124770L;
    private JList _list;

    public SelectInstanceFromCollectionPanel(Collection c, int initialSelection) {
        setLayout(new BorderLayout());
        _list = ComponentFactory.createList(ModalDialog.getCloseAction(this));
        c = removeHidden(c);
        _list.setListData(c.toArray());
        configureRenderer();
        if (initialSelection >= 0) {
            setSelection(initialSelection);
        }
        JScrollPane pane = ComponentFactory.createScrollPane(_list);
        add(pane);
        setPreferredSize(new Dimension(300, 150));
    }

    private boolean isMultiUserClient() {
        boolean isMultiUserClient = false;
        if (_list.getModel().getSize() > 0) {
            Object o = _list.getModel().getElementAt(0);
            if (o instanceof Frame) {
                Frame frame = (Frame) o;
                Project p = frame.getProject();
                isMultiUserClient = p.isMultiUserClient();
            }
        }
        return isMultiUserClient;
    }

    private Icon _clsIcon;

    private void configureRenderer() {
        FrameRenderer renderer;
        if (isMultiUserClient()) {
            // a really strange performance hack
            renderer = new FrameRenderer() {
                private static final long serialVersionUID = -4327868983862509964L;

                protected Icon getIcon(Cls cls) {
                    Icon icon;
                    if (_clsIcon == null) {
                        icon = cls.getIcon();
                        if (!cls.isMetaCls()) {
                            _clsIcon = icon;
                        }
                    } else {
                        icon = _clsIcon;
                    }
                    return icon;
                }
            };
        } else {
            renderer = FrameRenderer.createInstance();
            renderer.setDisplayTrailingIcons(false);
        }
        _list.setCellRenderer(renderer);
    }

    public Instance getSelection() {
        return (Instance) _list.getSelectedValue();
    }

    private static Collection removeHidden(Collection instances) {
        Collection result;
        Project p = ((Instance) (CollectionUtilities.getFirstItem(instances))).getProject();
        if (p.getDisplayHiddenClasses()) {
            result = instances;
        } else {
            result = new ArrayList();
            Iterator i = instances.iterator();
            while (i.hasNext()) {
                Instance instance = (Instance) i.next();
                if (instance.isVisible()) {
                    result.add(instance);
                }
            }
        }
        return result;
    }

    private void setSelection(final int index) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _list.setSelectedIndex(index);
                _list.ensureIndexIsVisible(index);
            }
        });
    }
    
    public void setCellRenderer(ListCellRenderer cellRenderer) {
        if (cellRenderer != null) {
            _list.setCellRenderer(cellRenderer); 
        }
    }
}
