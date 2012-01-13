package edu.stanford.smi.protege.action;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to display included projects and allow the user to change them.
 */

class ChangeIncludedProjectsPanel extends JComponent {
    private static final long serialVersionUID = 8112876043190298819L;
    private SelectableList _list;

    ChangeIncludedProjectsPanel(Project project) {
        setLayout(new BorderLayout());
        _list = ComponentFactory.createSelectableList(null);
        ComponentUtilities.setListValues(_list, project.getDirectIncludedProjectURIs());
        LabeledComponent c = new LabeledComponent("Directly Included Projects", new JScrollPane(_list));
        c.addHeaderButton(createSelectProjectAction());
        c.addHeaderButton(createRemoveProjectAction(_list));
        add(c);
        setPreferredSize(new Dimension(300, 300));
    }

    public Collection getIncludedProjectURIs() {
        return ComponentUtilities.getListValues(_list);
    }

    private Action createSelectProjectAction() {
        return new AddAction(ResourceKey.PROJECT_ADD) {
            private static final long serialVersionUID = -8349519785941796661L;

            public void onAdd() {
                JFileChooser chooser = ComponentFactory.createFileChooser("Select Project", "Project Files", "pprj");
                int openDialogResult = chooser.showOpenDialog(ChangeIncludedProjectsPanel.this);
                switch (openDialogResult) {
                    case JFileChooser.ERROR_OPTION:
                        // Get this on 'close"
                        break;
                    case JFileChooser.CANCEL_OPTION:
                        break;
                    case JFileChooser.APPROVE_OPTION:
                        ComponentUtilities.addListValue(_list, chooser.getSelectedFile().toURI());
                        break;
                    default:
                        Assert.fail("bad result: " + openDialogResult);
                        break;
                }
            }
        };
    }

    private Action createRemoveProjectAction(SelectableList list) {
        return new RemoveAction(ResourceKey.PROJECT_REMOVE, list) {
            private static final long serialVersionUID = -3939574688291304185L;

            public void onRemove(Collection values) {
                ComponentUtilities.removeListValues(_list, values);
            }
        };
    }
}
