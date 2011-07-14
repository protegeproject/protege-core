package edu.stanford.smi.protege.storage.database;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultCls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase_Test;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.model.framestore.SimpleFrameStore;
import edu.stanford.smi.protege.test.APITestCase;

public class DatabaseKnowledgeBase_Test extends APITestCase {
  
  public void testDBModficationSlots() throws Exception {
    for (DBType dbt : DBType.values()) {
      setDBType(dbt);
      if (!dbConfigured()) {
        continue;
      }
      DefaultKnowledgeBase_Test dkbt = new DefaultKnowledgeBase_Test();
      DefaultKnowledgeBase_Test.setDatabaseProject();
      try {
        dkbt.testModificationSlots();
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  public void testGetDBFramesWithValue() {
    for (DBType dbt : DBType.values()) {
      setDBType(dbt);
      if (!dbConfigured()) {
        continue;
      }
      setDatabaseProject();
      Slot s1 = createMultiValuedSlot(ValueType.STRING);
      Slot s2 = createMultiValuedSlot(ValueType.INSTANCE);
      Cls a = createCls();
      a.addDirectTemplateSlot(s1);
      a.addDirectTemplateSlot(s2);
      Instance inst1 = createInstance(a);
      Instance inst2 = createInstance(a);
      Instance inst3 = createInstance(a);
      
      inst1.addOwnSlotValue(s1, "abc");
      inst2.addOwnSlotValue(s1, "abc");
      inst3.addOwnSlotValue(s1, "abc");
      
      inst2.addOwnSlotValue(s2, inst1);
      
      assertEquals("string match", 3, getDomainKB().getFramesWithValue(s1, null, false, "abc").size());
      assertEquals("frame match", 1, getDomainKB().getFramesWithValue(s2, null, false, inst1).size());
    }
  }
  
  public void testGetMatchingDBReferences() {
    for (DBType dbt : DBType.values()) {
      setDBType(dbt);
      if (!dbConfigured()) {
        continue;
      }
      setDatabaseProject();
      Slot s = createMultiValuedSlot(ValueType.STRING);
      Cls a = createCls();
      a.addDirectTemplateSlot(s);
      Instance inst1 = createInstance(a);
      inst1.addOwnSlotValue(s, "zabcy");
      Instance inst2 = createInstance(a);
      inst2.addOwnSlotValue(s, "abcz");
      Instance inst3 = createInstance(a);
      inst3.addOwnSlotValue(s, "qqq");
      assertEquals("exact", 0, getMatchCount("z"));
      assertEquals("starts", 1, getMatchCount("z*"));
      assertEquals("contains", 2, getMatchCount("*z*"));
      assertEquals("contains insensitive", 2, getMatchCount("*Z*"));
      assertEquals("contains 2", 2, getMatchCount("*abc*"));
    }
  }
  
  
  private int getMatchCount(String s) {
    return getDomainKB().getMatchingReferences(s, KnowledgeBase.UNLIMITED_MATCHES).size();
  }

  public void testMatchOnPercentInDB() {
    for (DBType dbt : DBType.values()) {
      setDBType(dbt);
      if (!dbConfigured()) {
        continue;
      }

      setDatabaseProject();
      String name1 = "foo %";
      String name2 = "foo abc";
      Slot nameSlot = getDomainKB().getSlot(Model.Slot.NAME);

      createCls(name1);
      createCls(name2);

      Collection frames = getDomainKB().getMatchingFrames(nameSlot, null, false, name1, -1);
      assertEquals("matching size", 1, frames.size());

      Collection<Reference> references = getDomainKB().getReferences(name1, -1);
      assertEquals("references size", 1, references.size());
    }
  }
  
  public void testMatchOnQuoteInDB() {
    for (DBType dbt : DBType.values()) {
      setDBType(dbt);
      if (!dbConfigured()) {
        continue;
      }
      setDatabaseProject();
      String name1 = "foo's";
      Slot nameSlot = getDomainKB().getSlot(Model.Slot.NAME);

      createCls(name1);

      Collection frames = getDomainKB().getMatchingFrames(nameSlot, null, false, name1, -1);
      assertEquals("matching size", 1, frames.size());

      Collection<Reference> references = getDomainKB().getReferences(name1, -1);
      assertEquals("references size", 1, references.size());
    }
  }
  
  public void testGetDBDirectInstances() {
    for (DBType dbt : DBType.values()) {
      setDBType(dbt);
      if (!dbConfigured()) {
        continue;
      }
      setDatabaseProject();
      Cls cls = createCls();
      String name = cls.getName();
      createInstance(cls);
      createInstance(cls);
      createInstance(cls);
      assertEquals("direct instance count", 3, cls.getDirectInstanceCount());
      saveAndReload();
      cls = getCls(name);
      createInstance(cls);
      assertEquals("direct instance count after reload", 4, cls.getDirectInstanceCount());
    }
  }
  
  public void testSwizzleCaseSensitivity() {
      for (DBType dbt : DBType.values()) {
          setDBType(dbt);
          if (!dbConfigured()) {
            continue;
          }
          setDatabaseProject();
          KnowledgeBase kb = getDomainKB();
          String prefix = "PrEFiX";
          String suffix = UUID.randomUUID().toString();
          SystemFrames sframes = kb.getSystemFrames();
          Instance i = createInstance(sframes.getRootCls());
          Slot s = createSlot();
          
          Cls c1 = createCls(prefix + suffix);
          i.addOwnSlotValue(s, c1);
          Cls c2 = createCls(prefix.toLowerCase()  + suffix);
          i.addOwnSlotValue(s, c2);
          
          SimpleFrameStore sfs = kb.getFrameStoreManager().getFrameStoreFromClass(SimpleFrameStore.class);
          assertTrue(sfs != null);
          sfs.setDirectOwnSlotValues(c1, sframes.getDirectTypesSlot(), Collections.singleton(sframes.getStandardSlotMetaCls()));
          kb.flushCache();
          
          assertTrue(kb.getFrame(prefix + suffix) instanceof Cls);
          assertTrue(kb.getFrame(prefix.toLowerCase() + suffix) instanceof Cls);
          int count = 0;
          for (Object o : i.getOwnSlotValues(s)) {
              assertTrue(o instanceof Cls);
              count++;
          }
          assertTrue(count == 2);
          
          sfs.swizzleInstance(c1);
          kb.flushCache();
          
          assertTrue(kb.getFrame(prefix + suffix) instanceof Slot);
          assertTrue(kb.getFrame(prefix.toLowerCase() + suffix) instanceof Cls);
          count = 0;
          for (Object o : i.getOwnSlotValues(s)) {
              if  (((Frame) o).getName().equals(prefix.toLowerCase() + suffix)) {
                  assertTrue(o instanceof Cls);
              }
              else {
                  assertTrue(o instanceof Slot);
              }
              count++;
          }
          assertTrue(count == 2);
      }
  }
  
  public void testRenameCaseSensitivity() {
      for (DBType dbt : DBType.values()) {
          setDBType(dbt);
          if (!dbConfigured()) {
            continue;
          }
          setDatabaseProject();
          KnowledgeBase kb = getDomainKB();
          String prefix = "PrEFiX";
          String suffix = UUID.randomUUID().toString();
          SystemFrames sframes = kb.getSystemFrames();
          Instance i = createInstance(sframes.getRootCls());
          Slot s = createSlot();
          
          Cls c1 = createCls(prefix + suffix);
          i.addOwnSlotValue(s, c1);
          Cls c2 = createCls(prefix.toLowerCase()  + suffix);
          i.addOwnSlotValue(s, c2);
          
          SimpleFrameStore sfs = kb.getFrameStoreManager().getFrameStoreFromClass(SimpleFrameStore.class);
          assertTrue(sfs != null);
          sfs.setDirectOwnSlotValues(c1, sframes.getDirectTypesSlot(), Collections.singleton(sframes.getStandardSlotMetaCls()));
          kb.flushCache();
          
          boolean foundC1 = false;
          boolean foundC2 = false;
          for (Object o : i.getOwnSlotValues(s)) {
              if (((Frame) o).getName().equals(c1.getName()))  {
                  foundC1 = true;
              }
              else if (((Frame) o).getName().equals(c2.getName())) {
                  foundC2 = true;
              }
          }
          assertTrue(foundC1);
          assertTrue(foundC2);
          
          sfs.replaceFrame(c1, new DefaultCls(kb, new FrameID(suffix)));
          kb.flushCache();
          
          foundC1 = false;
          foundC2 = false;
          for (Object o : i.getOwnSlotValues(s)) {
              if (((Frame) o).getName().equals(suffix))  {
                  foundC1 = true;
              }
              else if (((Frame) o).getName().equals(c2.getName())) {
                  foundC2 = true;
              }
          }
          assertTrue(foundC1);
          assertTrue(foundC2);
      }
  }

}
