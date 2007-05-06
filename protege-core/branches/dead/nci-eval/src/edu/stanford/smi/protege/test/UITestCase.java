package edu.stanford.smi.protege.test;

import java.awt.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.FocusManager;
import javax.swing.text.*;

import edu.stanford.smi.protege.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

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
        Application.main(new String[] { uri.toString()});
    }

    public Project getProject() {
        return ProjectManager.getProjectManager().getCurrentProject();
    }

    public static void pressButton(Component c, Icon icon) {
        assertNotNull(c);
        assertNotNull(icon);
        TestUtilities.pressButton(c, icon);
    }

    protected void pressToolBarButton(Icon icon) {
        JComponent c = getSystemToolBar();
        assertNotNull(c);
        pressButton(c, icon);
    }

    protected JToolBar getSystemToolBar() {
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
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {

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
        } catch (InvocationTargetException ex) {
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

    protected void executeOnNextModalDialog(final Runnable runnable) {
        Thread thread = new Thread() {
            public void run() {
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
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    protected void setSelectionOnTree(Icon icon, Object[] path) {
        TestUtilities.setSelectionOnTree(getMainWindow(), icon, path);
    }

    protected Object[] getSelectionOnTree(Icon icon) {
        return TestUtilities.getSelectionOnTree(getMainWindow(), icon);
    }

    protected JTree getTreeOnTab(Icon icon) {
        return TestUtilities.getTreeOnTab(getMainWindow(), icon);
    }

    protected void unpressToolBarButton(Icon icon) {
        sync();
        AbstractButton button = getToolBarButton(icon);
        assertNotNull(button);
        if (button.isSelected()) {
            button.doClick();
        }
    }

    protected AbstractButton getToolBarButton(Icon icon) {
        return TestUtilities.getButton(getSystemToolBar(), icon);
    }
}
