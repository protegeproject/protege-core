package edu.stanford.smi.protege.event;

public interface Event {
    
    Object getSource();
    
    long getTimeStamp();
    
    void setTimeStamp(long timeStamp);

    String getUserName();
    
    boolean isReplacementEvent();
    
    void setReplacementEvent(boolean replacementEvent);
}
