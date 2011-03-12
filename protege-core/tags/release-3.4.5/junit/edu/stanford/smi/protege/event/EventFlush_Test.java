package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.EventDispatchFrameStore;
import edu.stanford.smi.protege.test.APITestCase;

public class EventFlush_Test extends APITestCase {
  public final static int INTERNAL_WAIT = 221;

  private boolean eventFound = false;
  private Object lock = new Object();  // there should be no race actually
  
  public void testEventFlushMechanism1() throws InterruptedException {
    doEventFlushMechanism(true);
  }
  
  public void testEventFlushMechanism2() throws InterruptedException {
    doEventFlushMechanism(false);
  }
  
  public void doEventFlushMechanism(boolean poll) throws InterruptedException {
    Cls container = createCls();
    Instance instance = createInstance(container);
    final Slot slot = createSlotOnCls(container);
    DefaultKnowledgeBase kb = (DefaultKnowledgeBase) getDomainKB();
    kb.setEventDispatchEnabled(true);
    kb.setPollForEvents(poll);
    FrameListener listener = new FrameAdapter() {
      public void ownSlotValueChanged(FrameEvent event) {
        synchronized (lock)  {
          if (event.getSlot().equals(slot)) {
            eventFound = true;
          }
        }
      }
    };
    instance.addFrameListener(listener);
    
    int repetitions = (2 * EventDispatchFrameStore.DELAY_MSEC) / INTERNAL_WAIT;
    for (int count = 0; count <  repetitions; count++) {
      instance.setDirectOwnSlotValue(slot, createInstance(container));
      kb.flushEvents();
      synchronized (lock) {
        assertTrue(eventFound);
        eventFound = false;
      }
      Thread.sleep(INTERNAL_WAIT);
    }
    
  }

}
