package edu.stanford.smi.protege.test;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import edu.stanford.smi.protege.Application;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class UITestCase extends AbstractTestCase {

    static {
        ProjectFactory factory = new ClipsProjectFactory();
        Project p = factory.createProject();
        p = factory.saveAndReloadProject(p);
        URI uri = p.getProjectURI();
        p.dispose();
        Application.main(new String[] { uri.toString() });
    }

    public Project getProject() {
        return ProjectManager.getProjectManager().getCurrentProject();
    }

    public static void pressButton(Component c, Icon icon) {
        assertNotNull(c);
        assertNotNull(icon);
        TestUtilities.pressButton(c, icon);
    }

    protected static void pressToolBarButton(Icon icon) {
        JComponent c = getSystemToolBar();
        assertNotNull(c);
        pressButton(c, icon);
    }

    protected static JToolBar getSystemToolBar() {
        return ProjectManager.getProjectManager().getCurrentProjectMainToolBar();
    }

    protected static Component getTopWindow() {
        Window window = FocusManager.getCurrentManager().getActiveWindow();
        assertNotNull(window);
        return window;
    }

    protected static Component getMainWindow() {
        return ProjectManager.getProjectManager().getMainPanel();
    }

    protected static void delaySeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    protected static void sync() {
        Toolkit.getDefaultToolkit().sync();
        try {
            // wait until the event queue is empty
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    // do nothing
                }
            });
        } catch (InterruptedException e1) {
            // do nothing
        } catch (InvocationTargetException ex) {
            // do nothing
        }
    }

    protected static void setLabeledComponentText(Component window, String labelText, String text) {
        assertNotNull(window);
        sync();
        boolean set = false;
        Iterator i = ComponentUtilities.getDescendentsOfClass(LabeledComponent.class, window).iterator();
        while (i.hasNext()) {
            LabeledComponent labeledComponent = (LabeledComponent) i.next();
            String label = labeledComponent.getHeaderLabel().toLowerCase();
            if (label.startsWith(labelText.toLowerCase())) {
                Component c = labeledComponent.getCenterComponent();
                if (c instanceof JScrollPane) {
                    c = ((JScrollPane) c).getViewport().getView();
                }
                assertTrue(c instanceof JTextComponent);
                JTextComponent textComponent = (JTextComponent) c;
                textComponent.setText(text);
                set = true;
                break;
            }
        }
        assertTrue(set);
        sync();
    }

    protected static void executeOnNextModalDialog(final Runnable runnable) {
        Thread thread = new Thread() {
            public void run() {
              try {
                ModalDialog dialog = null;
                while (dialog == null) {
                    dialog = ModalDialog.getCurrentDialog();
                    if (dialog != null && dialog.isVisible()) {
                        sync();
                        SystemUtilities.sleepMsec(250);
                        runnable.run();
                        return;
                    }
                    SystemUtilities.sleepMsec(250);
                }
              } catch (Throwable t) {
                Log.getLogger().log(Level.INFO, "Exception caught", t);
              }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    protected static void setSelectionOnTree(Icon icon, Object[] path) {
        TestUtilities.setSelectionOnTree(getMainWindow(), icon, path);
    }

    protected static Object[] getSelectionOnTree(Icon icon) {
        return TestUtilities.getSelectionOnTree(getMainWindow(), icon);
    }

    protected static JTree getTreeOnTab(Icon icon) {
        return TestUtilities.getTreeOnTab(getMainWindow(), icon);
    }

    protected static void unpressToolBarButton(Icon icon) {
        sync();
        AbstractButton button = getToolBarButton(icon);
        assertNotNull(button);
        if (button.isSelected()) {
            button.doClick();
        }
    }

    protected static AbstractButton getToolBarButton(Icon icon) {
        return TestUtilities.getButton(getSystemToolBar(), icon);
    }
}
