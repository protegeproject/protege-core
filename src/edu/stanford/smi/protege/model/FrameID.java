package edu.stanford.smi.protege.model;

import java.io.*;

import edu.stanford.smi.protege.util.*;

/**
 * A wrapper around a numeric value that uniquely identifies the frame.  The frame name also uniquely identifies it but
 * the frame id is unchanging, unlike the name.  This frame id is typically not used much by clients but it is used 
 * internally.  For file based projects the frame id is only unchanging while the project is loaded.  The next time
 * it is loaded the frames will all get other ids.  The frame ids are not stored in the files.  For the database 
 * backend the frame ids are unchanging because they are store in the database.
 * 
 * Frame ids below 10000 are reserved for system frames.  The particular ids assigned to system frame are available 
 * in {@link Model} 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameID implements Externalizable {
    public static final int NULL_FRAME_ID_VALUE = 0;
    public static final int INITIAL_USER_FRAME_ID = 10000;
    public static final int SYSTEM_PROJECT_ID = 0;
    public static final int LOCAL_PROJECT_ID = 1;
    private int localPart;
    private int projectPart;
    private int hashCode;
    
    public FrameID() {
        
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(localPart);
        out.writeInt(projectPart);
    }
    
    public void readExternal(ObjectInput in) throws IOException{
        localPart = in.readInt();
        projectPart = in.readInt();
        cacheHashCode();
    }

    public static FrameID createSystem(int value) {
        Assert.assertTrue("value=" + value, 0 < value && value < INITIAL_USER_FRAME_ID);
        return create(SYSTEM_PROJECT_ID, value);
    }

    public static FrameID createLocal(int value) {
        Assert.assertTrue("value=" + value, INITIAL_USER_FRAME_ID <= value);
        return create(LOCAL_PROJECT_ID, value);
    }

    public static FrameID create(int projectPart, int localPart) {
        return (localPart == NULL_FRAME_ID_VALUE) ? null : new FrameID(projectPart, localPart);
    }

    private FrameID(int projectPart, int localPart) {
        if (projectPart > 1) {
            Log.getLogger().severe("unknown project part: " + projectPart);
        }
        this.projectPart = projectPart;
        this.localPart = localPart;
        cacheHashCode();
    }
    
    private void cacheHashCode() {
        hashCode = HashUtils.getHash(String.valueOf(projectPart), String.valueOf(localPart));
    }

    public boolean isSystem() {
        return projectPart == SYSTEM_PROJECT_ID;
    }

    public int getLocalPart() {
        return localPart;
    }

    public int getProjectPart() {
        return projectPart;
    }

    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof FrameID) {
            FrameID other = (FrameID) o;
            result = projectPart == other.projectPart && localPart == other.localPart;
        }
        return result;
    }

    public final int hashCode() {
        return hashCode;
    }

    public boolean isUser() {
        return !isSystem();
    }

    public String toString() {
        return "FrameID(" + projectPart + ":" + localPart + ")";
    }
}
