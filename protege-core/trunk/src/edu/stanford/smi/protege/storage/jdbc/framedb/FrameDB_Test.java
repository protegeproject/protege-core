package edu.stanford.smi.protege.storage.jdbc.framedb;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.test.*;

/**
 * Units tests for FrameDB
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameDB_Test extends APITestCase {
    // frames
    int counter = FrameID.INITIAL_USER_FRAME_ID;
    private FrameID generate() {
        return FrameID.createLocal(counter++);
    }

    private final FrameID fr1 = generate();
    private final FrameID fr2 = generate();
    // slots
    private final FrameID sl1 = generate();
    private final FrameID sl2 = generate();
    private final FrameID sl3 = generate();
    // facets
    private final FrameID fa2 = generate();
    // values
    private final Object ob1 = new Object();
    private final Object ob2 = new Object();
    private final Object ob3 = new Object();

    private FrameID getFrame(Object record) {
        return ((Record) record).getFrame();
    }

    public void testCountValues() {
        FrameDB db = new FrameDB();
        assertEquals(0, db.countValues(fr1, sl1, null, false));

        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr1, sl1, null, true, ob3);

        assertEquals(2, db.countValues(fr1, sl1, null, false));
        db.removeFrame(fr1);
        assertEquals(0, db.countValues(fr1, sl1, null, false));
    }

    public void testGetValues() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr1, sl3, null, false, ob3);
        db.addValue(fr2, sl2, null, false, ob2);

        Collection values = db.getValues(fr1, sl1, null, false);
        assertEquals(2, values.size());

        values = db.getValues(fr1, sl2, null, false);
        assertNull(values);
    }

    public void testInsert() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr2, sl2, fa2, true, ob2);

        Collection c1 = db.getRecords(ob1);
        assertEquals(1, c1.size());
        assertSame(fr1, getFrame(c1.iterator().next()));

        Collection c2 = db.getRecords(ob2);
        assertEquals(1, c2.size());
        assertSame(fr2, getFrame(c2.iterator().next()));

        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr1, sl1, null, false, ob3);
        Collection c3 = db.getValues(fr1, sl1, null, false);
        assertEquals(3, c3.size());
        assertSame(ob1, c3.iterator().next());
    }

    public void testInsertAt() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob3);
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValueAt(fr1, sl1, null, false, ob2, 2);

        Collection c1 = db.getValues(fr1, sl1, null, false);
        assertEquals(c1.size(), 4);
        Iterator i = c1.iterator();
        assertSame(ob1, i.next());
        assertSame(ob3, i.next());
        assertSame(ob2, i.next());
        assertSame(ob1, i.next());
    }

    public void testRemoveFrame() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr2, sl1, null, false, ob2);

        db.removeFrame(fr1);
        Collection values = db.getValues(fr1, sl1, null, false);
        assertNull(values);
    }

    public void testRemoveValue() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr2, sl1, null, false, ob2);

        db.removeSingleValue(fr1, sl1, null, false, ob2);
        Collection values = db.getValues(fr1, sl1, null, false);
        assertEquals(1, values.size());
        assertSame(ob1, values.iterator().next());

        db.removeFrame(fr1);
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr1, sl1, null, false, ob1);
        assertEquals(3, db.getValues(fr1, sl1, null, false).size());
        db.removeSingleValue(fr1, sl1, null, false, ob1);
        assertEquals(2, db.getValues(fr1, sl1, null, false).size());
    }

    public void testRemoveValueAt() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        assertEquals(1, db.getValues(fr1, sl1, null, false).size());
        db.removeValueAt(fr1, sl1, null, false, 0);
        assertEquals(0, db.getValues(fr1, sl1, null, false).size());

        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr1, sl1, null, false, ob3);
        db.removeValueAt(fr1, sl1, null, false, 1);
        Collection values = db.getValues(fr1, sl1, null, false);
        assertEquals(2, values.size());
        Iterator i = values.iterator();
        assertSame(ob1, i.next());
        assertSame(ob3, i.next());
    }

    public void testRemoveValues() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr2, sl2, fa2, true, ob2);

        db.removeValues(fr1, sl1, null, false);
        Collection values = db.getValues(fr1, sl1, null, false);
        assertEquals(values.size(), 0);
    }

    public void testSelectRecords() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl2, null, false, ob2);
        db.addValue(fr1, sl3, null, false, ob3);
        db.addValue(fr2, sl2, null, false, ob2);

        Collection values = db.getRecords(ob1);
        assertEquals(1, values.size());

        values = db.getRecords(ob2);
        assertEquals(2, values.size());
    }

    public void testSetValues() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.setValues(fr1, sl1, null, false, ob1);

        Collection values = db.getValues(fr1, sl1, null, false);
        assertEquals(1, values.size());
        assertSame(ob1, values.iterator().next());

        Collection newValues = new ArrayList();
        newValues.add(ob2);
        newValues.add(ob1);
        db.setValues(fr1, sl1, null, false, newValues);

        values = db.getValues(fr1, sl1, null, false);
        assertEquals(2, values.size());
        Iterator i = values.iterator();
        assertSame(ob2, i.next());
        assertSame(ob1, i.next());
    }

    public void testUpdateValue() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr1, sl1, null, false, ob1);
        db.updateValue(fr1, sl1, null, false, ob1, ob3);

        Collection values = db.getValues(fr1, sl1, null, false);
        assertEquals(3, values.size());
        Iterator i = values.iterator();
        assertSame(ob3, i.next());
        assertSame(ob2, i.next());
        assertSame(ob3, i.next());
    }

    public void testUpdateValuePosition() {
        FrameDB db = new FrameDB();
        db.addValue(fr1, sl1, null, false, ob1);
        db.addValue(fr1, sl1, null, false, ob2);
        db.addValue(fr1, sl1, null, false, ob1);
        db.updateValuePosition(fr1, sl1, null, false, 2, 1);

        Collection values = db.getValues(fr1, sl1, null, false);
        assertEquals(3, values.size());
        Iterator i = values.iterator();
        assertSame("t1: 1", ob1, i.next());
        assertSame("t1: 2", ob1, i.next());
        assertSame("t1: 3", ob2, i.next());

        db.updateValuePosition(fr1, sl1, null, false, 0, 2);
        values = db.getValues(fr1, sl1, null, false);
        assertEquals(3, values.size());
        i = values.iterator();
        assertSame("t2: 1", ob1, i.next());
        assertSame("t2: 2", ob2, i.next());
        assertSame("t2: 3", ob1, i.next());

    }
}
