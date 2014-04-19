package edu.stanford.smi.protege.server.framestore.background;

import java.util.Map;

import edu.stanford.smi.protege.server.RemoteSession;


public interface FrameCalculatorStats {
  
  Map<RemoteSession, Integer> getPreCacheBacklog();
  
  long getPrecalculateTime();

}
