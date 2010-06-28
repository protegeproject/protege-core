package edu.stanford.smi.protege.util;

import java.util.UUID;

public class IDGenerator {
    public static final String UNIQUE_SESSION_ID = UUID.randomUUID().toString().replace("-", "_");

    public static int localCount = 0;

    public static synchronized String getNextUniqueId() {
        StringBuffer sb = new StringBuffer();
        sb.append(localCount++);
        sb.append('_');
        sb.append(UNIQUE_SESSION_ID);
        return sb.toString();
    }

}
