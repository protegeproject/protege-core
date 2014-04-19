package edu.stanford.smi.protege.action;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to display the included projects but not allow them to be changed.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

class ManageIncludedProjectsPanel extends SelectableContainer implements Validatable {
    private static final long serialVersionUID = -3853967523137933454L;
    private SelectableTree tree;
    private StandardAction addProjectAction;
    private StandardAction removeProjectAction;
    private StandardAction activateProjectAction;
    private URI activeProject;
    private Tree uriTree;
    private Tree originalUriTree;
    private Project project;

    ManageIncludedProjectsPanel(Project project) {
        setLayout(new BorderLayout());
        this.project = project;
        activeProject = project.getActiveRootURI();
        uriTree = project.getProjectTree();
        originalUriTree = (Tree) uriTree.clone();
        tree = ComponentFactory.createSelectableTree(null);
        tree.setLargeModel(true);
        generateTreeModel();
        tree.setCellRenderer(new ProjectRenderer());
        setSelectable(tree);
        LabeledComponent c = new LabeledComponent("Project Tree", new JScrollPane(tree));
        c.addHeaderButton(createAddIncludedProjectAction());
        c.addHeaderButton(createRemoveIncludedProjectAction());
        c.addHeaderButton(createActivateIncludedProjectAction());
        add(c);
        setPreferredSize(new Dimension(400, 400));
        updateButtons();
    }

    private void generateTreeModel() {
        tree.setModel(new LazyTreeModel(new ProjectRoot(uriTree)));
        tree.setSelectionRow(0);
        ComponentUtilities.fullSelectionExpand(tree, 100);
    }

    private Action createAddIncludedProjectAction() {
        addProjectAction = new AddAction(ResourceKey.PROJECT_ADD) {
            private static final long serialVersionUID = 281085250312768171L;

            public void onAdd() {
                String type = Text.getProgramName() + " Project Files";
                JFileChooser chooser = ComponentFactory.createFileChooser("Select Project", type, "pprj");
                int openDialogResult = chooser.showOpenDialog(ManageIncludedProjectsPanel.this);
                switch (openDialogResult) {
                    case JFileChooser.ERROR_OPTION:
                        // Get this on 'close"
                        break;
                    case JFileChooser.CANCEL_OPTION:
                        break;
                    case JFileChooser.APPROVE_OPTION:
                        URI uri = chooser.getSelectedFile().toURI();
                        uriTree.addChild(uriTree.getRoot(), uri);
                        generateTreeModel();
                        break;
                    default:
                        Assert.fail("bad result: " + openDialogResult);
                        break;
                }
            }
        };
        return addProjectAction;
    }

    private Action createRemoveIncludedProjectAction() {
        removeProjectAction = new RemoveAction(ResourceKey.PROJECT_REMOVE, null) {
            private static final long serialVersionUID = -3548026495703249060L;

            public void onRemove(Collection values) {
                Iterator i = tree.getSelection().iterator();
                while (i.hasNext()) {
                    URI uri = (URI) i.next();
                    uriTree.removeChild(uriTree.getRoot(), uri);
                }
                generateTreeModel();
            }
        };
        return removeProjectAction;
    }

    private Action createActivateIncludedProjectAction() {
        activateProjectAction = new StandardAction("Activate Selected Project", Icons.getSelectProjectIcon()) {
            private static final long serialVersionUID = 7554544212783921618L;

            public void actionPerformed(ActionEvent event) {
                URI uri = (URI) CollectionUtilities.getFirstItem(getSelection());
                if (uri != null) {
                    activeProject = uri;
                    tree.repaint();
                }
            }
        };
        return activateProjectAction;
    }

    public void onSelectionChange() {
        super.onSelectionChange();
        updateButtons();
    }

    private void updateButtons() {
        TreePath path = tree.getSelectionPath();
        if (path == null) {
            addProjectAction.setEnabled(false);
            removeProjectAction.setEnabled(false);
            activateProjectAction.setEnabled(false);
        } else {
            LazyTreeNode node = (LazyTreeNode) path.getLastPathComponent();
            addProjectAction.setEnabled(path.getPathCount() == 2);
            removeProjectAction.setEnabled(path.getPathCount() == 3);
            activateProjectAction.setEnabled(node.getUserObject() != activeProject);
        }
    }

    public void saveContents() {
        Set directIncludes = uriTree.getChildren(uriTree.getRoot());
        Set oldDirectIncludes = originalUriTree.getChildren(originalUriTree.getRoot());
        if (!CollectionUtilities.equalsSet(directIncludes, oldDirectIncludes)) {
            ProjectManager.getProjectManager().changeIncludedProjectURIsRequest(directIncludes);
        } else if (activeProject != project.getActiveRootURI()) {
            ProjectManager.getProjectManager().setActiveProjectURI(activeProject);
        }
    }

    public boolean validateContents() {
        boolean isOK = confirmActiveProjectChanged();
        if (isOK) {
            // isOK = confirmProjectReloadOnInclusionChange();
        }
        return isOK;
    }

    private boolean confirmActiveProjectChanged() {
        boolean confirmed = true;
        URI currentActiveProject = project.getActiveRootURI();
        if (!uriTree.getRoot().equals(activeProject) && !currentActiveProject.equals(activeProject)) {
            String text = "Changing the active project will cause projects higher and at the same level in the tree to be hidden.";
            text += "\n\n Is this what you want to do?";
            int rval = ModalDialog.showMessageDialog(this, text, ModalDialog.MODE_YES_NO_CANCEL);
            confirmed = rval == ModalDialog.OPTION_YES;
        }
        return confirmed;
    }

    //	private boolean confirmProjectReloadOnInclusionChange() {
    //        boolean confirmed = true;
    //        Set currentDirectInclusions = originalUriTree.getChildren(originalUriTree.getRoot());
    //        Set directInclusions = uriTree.getChildren(uriTree.getRoot());
    //        if (!directInclusions.equals(currentDirectInclusions)) {
    //            String text = "Changing the included projects will cause the current project to be saved and reloaded.";
    //            text += "\n\n Is this what you want to do?";
    //            int rval = ModalDialog.showMessageDialog(this, text, ModalDialog.MODE_YES_NO_CANCEL);
    //            confirmed = rval == ModalDialog.OPTION_YES;
    //        }
    //        return confirmed;
    //    }

    private boolean isActive(URI projectName) {
        return activeProject != null && activeProject.equals(projectName);
    }

    private boolean isLoaded(URI projectURI) {
        return originalUriTree.isReachable(projectURI);
    }

    private boolean isHidden(URI projectURI) {
        return !isActive(projectURI) && !uriTree.getDescendents(activeProject).contains(projectURI);
    }

    class ProjectRenderer extends DefaultRenderer {

        private static final long serialVersionUID = 6327407152261236880L;

        public void load(Object o) {
            URI projectURI = (URI) o;
            setMainText(URIUtilities.getBaseName(projectURI));
            boolean isReadonly = !isActive(projectURI);
            boolean isHidden = isHidden(projectURI);
            setMainIcon(Icons.getProjectIcon(isReadonly, isHidden));

            if (!isLoaded(projectURI)) {
                appendText("    (not loaded)");
            }
        }

    }
}

class ProjectRoot extends LazyTreeRoot {
    private Tree projectURIs;

    ProjectRoot(Tree projectURIs) {
        super(projectURIs.getRoot());
        this.projectURIs = projectURIs;
    }

    protected LazyTreeNode createNode(Object o) {
        return new ProjectNode(this, projectURIs.getRoot(), projectURIs);
    }

    protected Comparator getComparator() {
        // TODO Auto-generated method stub
        return null;
    }

}

class ProjectNode extends LazyTreeNode {
    private Tree projectURIs;

    ProjectNode(LazyTreeNode parent, Object node, Tree projectURIs) {
        super(parent, node);
        this.projectURIs = projectURIs;
        // Log.getLogger().info("created: " + node);
    }

    public Collection getChildObjects() {
        Collection children = projectURIs.getChildren(getUserObject());
        // Log.getLogger().info("children of " + getUserObject() + ": " +
        // children);
        return children;
    }

    public int getChildObjectCount() {
        return getChildObjects().size();
    }

    protected LazyTreeNode createNode(Object o) {
        return new ProjectNode(this, o, projectURIs);
    }

    protected Comparator getComparator() {
        return null;
    }

}