package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.test.*;
import edu.stanford.smi.protege.util.*;
/**
 * Unit tests for the InstanceDisplay class
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InstanceDisplay_Test extends APITestCase {

    private void deleteAllStickies() {
        KnowledgeBase kb = getDomainKB();
        Cls cls = kb.getCls(Model.Cls.INSTANCE_ANNOTATION);
        Iterator i = new ArrayList(cls.getInstances()).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
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

    private void pressMessageOK() {
        Thread t = new Thread() {
            public void run() {
                JDialog dialog = null;
                while (dialog == null) {
                    dialog = ModalDialog.getCurrentDialog();
                    if (dialog != null) {
                        pressButton(dialog, Icons.getYesIcon());
                        return;
                    }
                    SystemUtilities.sleepMsec(100);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private void pressButton(Component c, Icon icon) {
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
        JInternalFrame iFrame = (JInternalFrame) ComponentUtilities.getDescendentOfClass(JInternalFrame.class, frame1);
        JTextArea area = (JTextArea) ComponentUtilities.getDescendentOfClass(JTextArea.class, iFrame);
        area.setText("This is a test - " + System.currentTimeMillis());
        Point loc = iFrame.getLocation();
        loc.x += 100;
        iFrame.setLocation(loc);
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
