package edu.stanford.smi.protege.util;

//ESCA*JAVA0130

import edu.stanford.smi.protege.model.framestore.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Tree_Test extends SimpleTestCase {
    public void testCreate() {
        Object o = new Object();
        Tree tree = new Tree(o);
        assertEquals(o, tree.getRoot());
        tree.setRoot(null);
        assertNull(tree.getRoot());
        tree.setRoot(o);
        assertEquals(o, tree.getRoot());
    }

    public void testAddNode() {
        Object foo = "foo";
        Object bar = "bar";
        Object baz = "baz";
        Object bam = "bam";
        Tree tree = new Tree(foo);
        tree.addChild(foo, bar);
        tree.addChild(bar, baz);
        tree.addChild(foo, bam);
        assertEqualsSet(makeList(bar, bam), tree.getChildren(foo));
        assertEqualsSet(makeList(bar, bam, baz), tree.getDescendents(foo));
        assertEqualsSet(makeList(baz), tree.getDescendents(bar));
        assertEqualsSet(makeList(), tree.getDescendents(bam));
    }

    public void testRemoveNode() {
        Object foo = "foo";
        Object bar = "bar";
        Object baz = "baz";
        Tree tree = new Tree(foo);
        tree.addChild(foo, bar);
        tree.addChild(bar, baz);
        assertEqualsSet(makeList(bar, baz), tree.getDescendents(foo));
        tree.removeChild(bar, baz);
        assertEqualsSet(makeList(bar), tree.getDescendents(foo));
        assertEqualsSet(makeList(), tree.getChildren(bar));
        tree.addChild(foo, baz);
        Object all = "all";
        tree.addChild(baz, all);
        tree.addChild(bar, all);
        assertEqualsSet(makeList(bar, baz, all), tree.getDescendents(foo));
        tree.removeChild(baz, all);
        assertEqualsSet(makeList(bar, baz, all), tree.getDescendents(foo));
        tree.removeChild(bar, all);
        assertEqualsSet(makeList(bar, baz), tree.getDescendents(foo));
    }
}
