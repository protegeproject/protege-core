package edu.stanford.smi.protege.widget;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.test.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotsTab_Test extends UITestCase {
    public void testAddSuperslot() {
        Slot s1 = createSlot();
        Slot s2 = createSlot();
        s2.addDirectSuperslot(s1);
        JTree tree = getTreeOnTab(Icons.getSlotsIcon());
        tree.getModel();
    }

}
