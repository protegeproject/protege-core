package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameIDHelper {
    private static final String SEPARATOR = ":";
    private Map localToGlobalProjectMap = new HashMap();
    private Map globalToLocalProjectMap = new HashMap();

    public FrameIDHelper() {
        addProject(FrameID.SYSTEM_PROJECT_ID, FrameID.SYSTEM_PROJECT_ID);
        addProject(FrameID.LOCAL_PROJECT_ID, FrameID.LOCAL_PROJECT_ID);
    }

    public void addProject(int localProjectPart, int globalProjectPart) {
        localToGlobalProjectMap.put(new Integer(localProjectPart), new Integer(globalProjectPart));
        globalToLocalProjectMap.put(new Integer(globalProjectPart), new Integer(localProjectPart));
    }

    private int getLocalProjectPart(int projectPart) {
        return getMapValue(projectPart, globalToLocalProjectMap);
    }

    private int getGlobalProjectPart(int localProjectPart) {
        return getMapValue(localProjectPart, localToGlobalProjectMap);
    }

    private static int getMapValue(int key, Map map) {
        Integer value = (Integer) map.get(new Integer(key));
        if (value == null) {
            Log.error("No value", FrameIDHelper.class, "getMapValue", new Integer(key), map);
        }
        return value.intValue();
    }

    public int getLocalProjectPart(FrameID id) {
        return getLocalProjectPart(id.getProjectPart());
    }

    public FrameID createFrameID(int localProjectPart, int localPart) {
        int projectPart = getGlobalProjectPart(localProjectPart);
        return FrameID.create(projectPart, localPart);
    }

    public FrameID createFrameID(String s) {
        int index = s.indexOf(SEPARATOR);
        String localProjectPartString = s.substring(0, index);
        int localProjectPart = Integer.valueOf(localProjectPartString).intValue();

        String localPartString = s.substring(index + 1);
        int localPart = Integer.valueOf(localPartString).intValue();

        return createFrameID(localProjectPart, localPart);
    }

    public String createString(FrameID id) {
        int localPart = id.getLocalPart();
        int globalProjectPart = id.getProjectPart();
        int localProjectPart = getLocalProjectPart(globalProjectPart);
        return localProjectPart + SEPARATOR + localPart;
    }
}
