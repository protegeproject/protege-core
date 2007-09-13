package edu.stanford.smi.protege;
//ESCA*JAVA0130

import java.awt.*;
import java.util.*;

import junit.framework.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.test.*;
import edu.stanford.smi.protege.ui.*;

/**
 * Functional-test suite for all of Protege
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AllFunctionalTests extends TestCase {
    private static final String TEST_PROJECT = "tests\\TestProject.pprj";

    public void testLoadTabs() {
        Application.main(new String[] { TEST_PROJECT });
        Component mainFrame = Application.getMainWindow();
        KnowledgeBase kb = ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase();
        Cls thing = kb.getRootCls();
        Cls testClass = kb.getCls("testClass");
        Object[] path = new Object[] { thing, testClass };
        TestUtilities.setSelectionOnTree(mainFrame, Icons.getClsIcon(), path);
        TestUtilities.setSelectionOnTree(mainFrame, Icons.getFormIcon(), path);
        TestUtilities.setSelectionOnTree(mainFrame, Icons.getInstanceIcon(), path);
        TestUtilities.setSelectionOnTree(mainFrame, Icons.getClsAndInstanceIcon(), path);

        Object[] slotPath = new Object[1];
        Iterator i = kb.getSlots().iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            if (!slot.isSystem()) {
                slotPath[0] = slot;
                TestUtilities.setSelectionOnTree(mainFrame, Icons.getSlotIcon(), slotPath);
            }
        }
    }
}
