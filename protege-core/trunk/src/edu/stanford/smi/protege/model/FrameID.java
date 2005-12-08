package edu.stanford.smi.protege.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.HashUtils;

/**
 * A wrapper around a numeric value that uniquely identifies the frame. The frame name also uniquely identifies it but
 * the frame id is unchanging, unlike the name. This frame id is typically not used much by clients but it is used
 * internally. For file based projects the frame id is only unchanging while the project is loaded. The next time it is
 * loaded the frames will all get other ids. The frame ids are not stored in the files. For the database backend the
 * frame ids are unchanging because they are store in the database.
 * 
 * Frame ids below 10000 are reserved for system frames. The particular ids assigned to system frame are available in
 * {@link Model}
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameID implements Externalizable {
    public static final int NULL_FRAME_ID_VALUE = 0;
    public static final int NULL_FRAME_ID_LOCAL_VALUE = NULL_FRAME_ID_VALUE;
    public static final int INITIAL_USER_FRAME_ID = 10000;
    public static final int SYSTEM_PROJECT_ID = 0;
    public static final int LOCAL_PROJECT_ID = 1;
    public static final int NULL_FRAME_ID_PROJECT_VALUE = SYSTEM_PROJECT_ID;
    private int localPart;
    private int diskProjectPart;
    private int memoryProjectPart;
    private int hashCode;

    public FrameID() {

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(localPart);
        out.writeInt(diskProjectPart);
        out.writeInt(memoryProjectPart);
    }

    public void readExternal(ObjectInput in) throws IOException {
        localPart = in.readInt();
        diskProjectPart = in.readInt();
        memoryProjectPart = in.readInt();
        cacheHashCode();
    }

    public static FrameID createSystem(int value) {
        Assert.assertTrue("value=" + value, 0 < value && value < INITIAL_USER_FRAME_ID);
        return create(SYSTEM_PROJECT_ID, SYSTEM_PROJECT_ID, value);
    }

    public static FrameID createLocal(int value) {
        Assert.assertTrue("value=" + value, INITIAL_USER_FRAME_ID <= value);
        return create(LOCAL_PROJECT_ID, LOCAL_PROJECT_ID, value);
    }

    public static FrameID create(int diskProjectPart, int memoryProjectPart, int localPart) {
        return (localPart == NULL_FRAME_ID_VALUE) ? null : new FrameID(diskProjectPart, memoryProjectPart, localPart);
    }
    
    public static String toStringRepresentation(FrameID id) {
        String text;
        if (id == null) {
            text = toStringRepresentation(NULL_FRAME_ID_PROJECT_VALUE, NULL_FRAME_ID_LOCAL_VALUE);
        } else {
            text = id.toStringRepresentation();
        }
        return text;
    }
    public String toStringRepresentation() {
        return toStringRepresentation(diskProjectPart, localPart);
    }
    
    private static String toStringRepresentation(int projectPart, int localPart) {
        return projectPart + ":" + localPart;
    }
    public static FrameID fromStringRepresentation(int memoryProjectPart, String text) {
        int index = text.indexOf(":");
        int diskProjectPart = Integer.parseInt(text.substring(0, index));
        int localPart = Integer.parseInt(text.substring(index+1));
        return create(diskProjectPart, memoryProjectPart, localPart);
    }
    

    private FrameID(int diskProjectPart, int memoryProjectPart, int localPart) {
        this.diskProjectPart = diskProjectPart;
        this.memoryProjectPart = memoryProjectPart;
        this.localPart = localPart;
        cacheHashCode();
    }

    private void cacheHashCode() {
        hashCode = HashUtils.getHash(String.valueOf(memoryProjectPart), String.valueOf(localPart));
    }

    public boolean isSystem() {
        return memoryProjectPart == SYSTEM_PROJECT_ID;
    }

    public int getLocalPart() {
        return localPart;
    }

    public int getMemoryProjectPart() {
        return memoryProjectPart;
    }

    public int getDiskProjectPart() {
        return memoryProjectPart;
    }

    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof FrameID) {
            FrameID other = (FrameID) o;
            result = memoryProjectPart == other.memoryProjectPart && localPart == other.localPart;
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
        return "FrameID(" + memoryProjectPart + ":" + localPart + " " + diskProjectPart + ")";
    }
}