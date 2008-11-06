package edu.stanford.smi.protege.server.framestore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;

public class BandWidthPolicy {
    private static transient Logger log = Log.getLogger(BandWidthPolicy.class);
    
    public static final String BANDWIDTH_CAP_PROPERTY = "edu.stanford.smi.protege.server.max_value_update_rate";
    
    private long samplingInterval = 1000; // in milliseconds
    private int surgeCap;
    private int bandwidthCap;
    
    private int itemsWaitingToSend = 0;
    private List<SentItems> itemsSent = new ArrayList<SentItems>();
    
    public BandWidthPolicy() {
        this(getCap(), getCap());
    }
    
    public BandWidthPolicy(int surgeCap, int bandwidthCap) {
        this.surgeCap = surgeCap;
        this.bandwidthCap = bandwidthCap;
    }
    
    public static int getCap() {
        return ApplicationProperties.getIntegerProperty(BANDWIDTH_CAP_PROPERTY, 500);
    }
    
    public synchronized void addItemToWaitList() {
        itemsWaitingToSend++;
    }
    
    public synchronized void addItemsSent(int sent) {
        itemsSent.add(new SentItems(sent));
        itemsWaitingToSend = 0;
    }
    
    public synchronized boolean stopSending() {
        if (itemsWaitingToSend > surgeCap) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("throttling back");
            }
            return true;
        }
        long now = System.currentTimeMillis();
        List<SentItems> sentItemsToForget = new ArrayList<SentItems>();
        int itemsSentInInterval = 0;
        for (SentItems si  : itemsSent) {
            if (si.whenInMillis < now - samplingInterval) {
                sentItemsToForget.add(si);
            }
            else {
                itemsSentInInterval += si.count;
            }
        }
        itemsSent.removeAll(sentItemsToForget);
        if (log.isLoggable(Level.FINE) && itemsSentInInterval > bandwidthCap) {
            log.fine("Throttling back");
        }
        return itemsSentInInterval > bandwidthCap;
    }
    
    private static class SentItems {
        long whenInMillis;
        int count;
        
        public SentItems(int count) {
            this.count = count;
            this.whenInMillis =  System.currentTimeMillis();;
        }
    }
    

}
