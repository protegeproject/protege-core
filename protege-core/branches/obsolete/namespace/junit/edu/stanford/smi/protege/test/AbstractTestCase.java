package edu.stanford.smi.protege.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import junit.framework.TestCase;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.Log;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractTestCase extends TestCase {

    private static final File tempDirectory = createTempDirectory();

    private static File createTempDirectory() {
        File dir = null;
        try {
            File file = File.createTempFile("test", "tmp");
            File parent = file.getParentFile();
            file.delete();
            dir = new File(parent, "test" + File.separator);
            FileUtilities.deleteDirectory(dir);
            dir.mkdirs();
        } catch (IOException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return dir;
    }

    protected static File getTempDirectory() {
        return tempDirectory;
    }

    protected static File getTempSubdirectory(String subDirectory) {
        File dir = new File(tempDirectory, subDirectory + File.separator);
        dir.mkdirs();
        return dir;
    }

    protected static void deleteTempSubdirectory(String subDirectory) {
        File dir = new File(tempDirectory, subDirectory);
        FileUtilities.deleteDirectory(dir);
    }

    protected static void checkErrors(Collection errors) {
        if (!errors.isEmpty()) {
            Iterator i = errors.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof Throwable) {
                  Log.getLogger().log(Level.WARNING, "Exception found", (Throwable) o);
                } else {
                  Log.getLogger().warning(o.toString());
                }
            }
            fail();
        }
    }

    protected abstract Project getProject();

    protected KnowledgeBase getDomainKB() {
        return getProject().getKnowledgeBase();
    }

    protected static List makeList() {
        return new ArrayList();
    }

    public static List makeList(Object o) {
        assertNotNull(o);
        List list = makeList();
        list.add(o);
        return list;
    }

    public static List makeList(Object o1, Object o2) {
        assertNotNull(o2);
        List list = makeList(o1);
        list.add(o2);
        return list;
    }

    public static List makeList(Object o1, Object o2, Object o3) {
        assertNotNull(o3);
        List list = makeList(o1, o2);
        list.add(o3);
        return list;
    }

    public static List makeList(Object o1, Object o2, Object o3, Object o4) {
        assertNotNull(o4);
        List list = makeList(o1, o2, o3);
        list.add(o4);
        return list;
    }

    public static void assertEqualsList(Collection c1, Collection c2) {
        assertEquals(c1.size(), c2.size());
        Iterator i1 = c1.iterator();
        Iterator i2 = c2.iterator();
        while (i1.hasNext()) {
            Object o1 = i1.next();
            //ESCA-JAVA0282 
            Object o2 = i2.next();
            assertEquals(o1, o2);
        }
    }

    public static void assertEqualsSet(Collection c1, Collection c2) {
        assertEquals(c1.size(), c2.size());
        Set s = new HashSet(c1);
        s.removeAll(c2);
        assertEquals(0, s.size());
    }

    protected Cls createCls() {
        return createCls(null);
    }

    protected Cls createCls(String name) {
        return createCls(name, getDomainKB().getRootCls());
    }

    protected Cls createCls(String name, Cls parent) {
        return getDomainKB().createCls(name, CollectionUtilities.createCollection(parent));
    }

    protected Cls createClsWithType(Cls type) {
        return getDomainKB().createCls(null, getDomainKB().getRootClses(), type);
    }

    protected Cls createSubClsWithType(Cls superclass, Cls type) {
        return getDomainKB().createCls(null, makeList(superclass), type);
    }

    protected Facet createFacet() {
        return getDomainKB().createFacet(null);
    }

    protected Frame createFrame() {
        return createCls();
    }

    protected void delete(Frame frame) {
        getDomainKB().deleteFrame(frame);
    }

    protected Instance createInstance(Cls cls) {
        return createInstance(null, cls);
    }

    protected Instance createInstance(String name, Cls cls) {
        Instance instance = getDomainKB().createInstance(name, cls);
        if (instance instanceof Cls) {
            ((Cls) instance).addDirectSuperclass(getDomainKB().getRootCls());
            // Log.trace("instance=" + instance, this, "createInstance", cls);
        }
        return instance;
    }

    protected Slot createSlot() {
        return getDomainKB().createSlot(null);
    }

    protected Slot createSlotOnCls(Cls cls) {
        Slot slot = createSlot();
        cls.addDirectTemplateSlot(slot);
        return slot;
    }

    protected Slot createMultiValuedSlot(ValueType type) {
        Slot slot = getDomainKB().createSlot(null);
        slot.setValueType(type);
        slot.setAllowsMultipleValues(true);
        return slot;
    }

    protected Slot createMultiValuedSlot(ValueType type, Cls cls) {
        Slot slot = createMultiValuedSlot(type);
        setCompleteValueType(cls, slot, type);
        return slot;
    }

    protected Slot createSingleValuedSlot(ValueType type) {
        Slot slot = getDomainKB().createSlot(null);
        slot.setValueType(type);
        slot.setAllowsMultipleValues(false);
        return slot;
    }

    protected Slot createSingleValuedSlot(ValueType type, Cls cls) {
        Slot slot = createSingleValuedSlot(type);
        setCompleteValueType(cls, slot, type);
        return slot;
    }

    protected Cls createSubCls(Cls parent) {
        return createCls(null, parent);
    }

    protected Slot createSubSlot(Slot parent) {
        return getDomainKB().createSlot(null, parent.getDirectType(), Collections.singleton(parent), true);
    }

    protected void deleteFrame(Frame frame) {
        getDomainKB().deleteFrame(frame);
    }

    protected Cls getCls(String name) {
        return getDomainKB().getCls(name);
    }

    protected Facet getFacet(String name) {
        return getDomainKB().getFacet(name);
    }

    public Frame getFrame(String name) {
        return getDomainKB().getFrame(name);
    }

    protected int getFrameCount() {
        return getDomainKB().getFrameCount();
    }

    protected Instance getInstance(String name) {
        return getDomainKB().getInstance(name);
    }

    protected static void setCompleteValueType(Cls cls, Slot slot, ValueType type) {
        if (type == ValueType.INSTANCE) {
            slot.setAllowedClses(Collections.singleton(cls));
        } else if (type == ValueType.CLS) {
            slot.setAllowedParents(Collections.singleton(cls));
        } else {
            fail("bad type: " + type);
        }
    }

    public static void assertEqualsArray(Object[] array1, Object[] array2) {
        assertEqualsList(Arrays.asList(array1), Arrays.asList(array2));
    }
}
