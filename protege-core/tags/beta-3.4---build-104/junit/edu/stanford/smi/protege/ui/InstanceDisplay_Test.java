package edu.stanford.smi.protege.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.test.TestUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * Unit tests for the InstanceDisplay class
 * 
 * Warning - these tests have an infinite number of race conditions.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceDisplay_Test extends APITestCase {

    private void deleteAllStickies() {
        KnowledgeBase kb = getDomainKB();
        Cls cls = kb.getCls(Model.Cls.INSTANCE_ANNOTATION);
        Iterator<Instance> i = new ArrayList<Instance>(cls.getInstances()).iterator();
        while (i.hasNext()) {
            Instance instance = i.next();
            kb.deleteInstance(instance);
        }
    }

    private JFrame loadIntoFrame(String instanceName) {
        JFrame frame1 = new JFrame();
        InstanceDisplay id = new InstanceDisplay(getProject());
        frame1.getContentPane().add(id);
        Instance instance = getInstance(instanceName);
        assertNotNull("instance exists: " + instanceName, instance);
        id.setInstance(instance, null);
        frame1.pack();
        frame1.setVisible(true);
        Toolkit.getDefaultToolkit().sync();
        SystemUtilities.sleepMsec(100);
        return frame1;
    }

    private static void pressMessageOK() {
        Thread t = new Thread() {
            public void run() {
              try {
                JDialog dialog = null;
                while (dialog == null) {
                    dialog = ModalDialog.getCurrentDialog();
                    if (dialog != null) {
                        pressButton(dialog, Icons.getYesIcon());
                        return;
                    }
                    SystemUtilities.sleepMsec(100);
                }
              } catch (Throwable t) {
                Log.getLogger().log(Level.INFO, "Exception caught", t);
              }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private static void pressButton(Component c, Icon icon) {
        TestUtilities.pressButton(c, icon);
    }

    public void testYellowStickyCreation() {
        deleteAllStickies();
        String frameName = createCls().getName();
        JFrame frame1 = loadIntoFrame(frameName);
        int frameCount = getFrameCount();
        pressButton(frame1, Icons.getCreateClsNoteIcon());
        assertEquals("frame count", frameCount + 1, getFrameCount());
        JInternalFrame inf = (JInternalFrame) ComponentUtilities.getDescendentOfClass(JInternalFrame.class, frame1);
        assertNotNull("internal frame", inf);
        JTextArea area = (JTextArea) ComponentUtilities.getDescendentOfClass(JTextArea.class, inf);
        assertNotNull("text area", area);
        area.setText("This is a test - " + System.currentTimeMillis());

        JFrame frame2 = loadIntoFrame(frameName);
        Point loc = frame2.getLocation();
        loc.x += 200;
        frame2.setLocation(loc);

        ComponentUtilities.dispose(frame1);
        ComponentUtilities.dispose(frame2);
        deleteAllStickies();
    }

    public void testYellowStickyDeletion() {
        deleteAllStickies();
        String frameName = createCls().getName();
        JFrame frame = loadIntoFrame(frameName);
        int count = getFrameCount();
        pressButton(frame, Icons.getCreateClsNoteIcon());
        assertEquals("added sticky", count + 1, getFrameCount());
        pressMessageOK();
        pressButton(frame, Icons.getDeleteClsNoteIcon());
        assertEquals("deleted sticky", count, getFrameCount());
        ComponentUtilities.dispose(frame);
        deleteAllStickies();
    }

    public void testYellowStickyLocationSave() {
        String frameName = createCls().getName();
        JFrame frame1 = loadIntoFrame(frameName);
        pressButton(frame1, Icons.getCreateClsNoteIcon());
        final JInternalFrame iFrame = (JInternalFrame) ComponentUtilities.getDescendentOfClass(JInternalFrame.class, frame1);
        JTextArea area = (JTextArea) ComponentUtilities.getDescendentOfClass(JTextArea.class, iFrame);
        area.setText("This is a test - " + System.currentTimeMillis());
        final Point loc = iFrame.getLocation();
        loc.x += 100;    
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
               public void run() {
                   iFrame.setLocation(loc);
               } 
            });
        } catch (InterruptedException e) {
            fail();
        } catch (InvocationTargetException e) {
            fail();
        }
        try { // wait for the swing queue to process  the above change.
            SwingUtilities.invokeAndWait(new Runnable() {
               public void run() {
                   iFrame.setLocation(loc);
               } 
            });
        } catch (InterruptedException e) {
            fail();
        } catch (InvocationTargetException e) {
            fail();
        }
        ComponentUtilities.dispose(frame1);

        JFrame frame2 = loadIntoFrame(frameName);
        JInternalFrame iFrame2 = (JInternalFrame) ComponentUtilities.getDescendentOfClass(JInternalFrame.class, frame2);
        assertEquals("within project", loc, iFrame2.getLocation());
        ComponentUtilities.dispose(frame2);

        saveAndReload();
        JFrame frame3 = loadIntoFrame(frameName);
        JInternalFrame iFrame3 = (JInternalFrame) ComponentUtilities.getDescendentOfClass(JInternalFrame.class, frame3);
        assertEquals("after reload", loc, iFrame3.getLocation());
        ComponentUtilities.dispose(frame3);

        deleteAllStickies();
    }
}
