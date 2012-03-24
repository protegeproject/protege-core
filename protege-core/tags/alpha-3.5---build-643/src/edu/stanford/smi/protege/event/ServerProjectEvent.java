package edu.stanford.smi.protege.event;

public interface ServerProjectEvent extends Event {
    
    public static final int BASE = 1000;
    
    public enum ServerEventTypes {
        PROJECT_NOTIFICATION_EVENT, PROJECT_STATUS_CHANGE_EVENT;

        
        public int getTypeAsInt() {
            return BASE + ordinal();
        }
    }


}
