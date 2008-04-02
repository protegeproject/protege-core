package edu.stanford.smi.protege.action;

import edu.stanford.smi.protege.test.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AutosynchronizeTrees_Test extends UITestCase {

    public void tearDown() {
        // unpressToolBarButton(Icons.getAutoSynchronizeIcon());
    }

    public void testSynchonize() {
//        pressToolBarButton(Icons.getAutoSynchronizeIcon());
//        KnowledgeBase kb = getDomainKB();
//        Cls superCls = kb.createCls(null, kb.getRootClses());
//        Collection superClses = CollectionUtilities.createCollection(superCls);
//        kb.createCls(null, superClses);
//        Cls sub1 = kb.createCls(null, superClses);
//        kb.createCls(null, superClses);
//
//        Object[] clsesPath = new Object[] { kb.getRootCls(), superCls, sub1 };
//        setSelectionOnTree(Icons.getClsesIcon(), clsesPath);
//        Object[] formsPath = getSelectionOnTree(Icons.getFormsIcon());
//        assertEqualsArray(clsesPath, formsPath);
//        Object[] instancesPath = getSelectionOnTree(Icons.getInstancesIcon());
//        assertEqualsArray(clsesPath, instancesPath);
    }

    public void testUnsynchronize() {
    }

}
