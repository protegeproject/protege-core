package edu.stanford.smi.protege.server.framestore;

public interface RemoteClientStats {
  
  public int getCacheHits();
  
  public int getCacheMisses();
  
  public int getClosureCacheHits();
  
  public int getClosureCacheMisses();

}
