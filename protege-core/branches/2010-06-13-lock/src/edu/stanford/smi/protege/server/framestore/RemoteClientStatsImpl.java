package edu.stanford.smi.protege.server.framestore;

public class RemoteClientStatsImpl implements RemoteClientStats {
    private int miss = 0;
    private int hit = 0;
    private int closureMiss = 0;
    private int closureHit = 0;
    
    public synchronized void cacheHit() {
    	hit++;
    }
    
    public synchronized void cacheMiss() {
    	miss++;
    }
    
    public synchronized void cacheClosureHit() {
    	closureHit++;
    }
    
    public synchronized void cacheClosureMiss() {
    	closureMiss++;
    }

    public synchronized int getCacheHits() {
      return hit;
    }

    public synchronized int getCacheMisses() {
      return miss;
    }

    public synchronized int getClosureCacheHits() {
      return closureHit;
    }

    public synchronized int getClosureCacheMisses() {
      return closureMiss;
    }

  }
