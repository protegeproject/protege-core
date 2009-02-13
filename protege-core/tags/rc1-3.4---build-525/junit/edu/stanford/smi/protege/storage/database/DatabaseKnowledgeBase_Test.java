package edu.stanford.smi.protege.storage.database;

import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase_Test;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
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

}
