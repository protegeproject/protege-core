package edu.stanford.smi.protege.ui;

//ESCA*JAVA0007

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Calculates "interesting" statistics for a Project.  These can be displayed by the metrics panel.
 */

class Statistics {

    // -- Frames
    //ESCA-JAVA0098 
    public int nFrames;
    public int nSystemFrames;
    public int nIncludedFrames;
    public int nDirectFrames;

    // -- Cls
    public int nClses;
    public int nSystemClses;
    public int nIncludedClses;
    public int nDirectClses;

    // -- Slot
    public int nSlots;
    public int nSystemSlots;
    public int nIncludedSlots;
    public int nDirectSlots;

    // Facet
    public int nFacets;
    public int nSystemFacets;
    public int nIncludedFacets;
    public int nDirectFacets;

    // Simple Instance
    public int nInstances;
    public int nSystemInstances;
    public int nIncludedInstances;
    public int nDirectInstances;

    Statistics(KnowledgeBase kb) {
        Iterator i = new ArrayList(kb.getFrames()).iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            updateFrameStatistics(frame);
            if (frame instanceof Cls) {
                updateClsStatistics((Cls) frame);
            } else if (frame instanceof Slot) {
                updateSlotStatistics((Slot) frame);
            } else if (frame instanceof Facet) {
                updateFacetStatistics((Facet) frame);
            } else {
                updateInstanceStatistics((Instance) frame);
            }
        }
    }

    private void updateClsStatistics(Cls cls) {
        ++nClses;
        if (cls.isSystem()) {
            ++nSystemClses;
        } else if (cls.isIncluded()) {
            ++nIncludedClses;
        } else {
            ++nDirectClses;
        }
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    private void updateFacetStatistics(Facet facet) {
        ++nFacets;
        if (facet.isSystem()) {
            ++nSystemFacets;
        } else if (facet.isIncluded()) {
            ++nIncludedFacets;
        } else {
            ++nDirectFacets;
        }
    }

    private void updateFrameStatistics(Frame frame) {
        ++nFrames;
        if (frame.isSystem()) {
            ++nSystemFrames;
        } else if (frame.isIncluded()) {
            ++nIncludedFrames;
        } else {
            ++nDirectFrames;
        }
    }

    // <Instance, nreferencers>
    private void updateInstanceStatistics(Instance instance) {
        ++nInstances;
        if (instance.isSystem()) {
            ++nSystemInstances;
        } else if (instance.isIncluded()) {
            ++nIncludedInstances;
        } else {
            ++nDirectInstances;
        }
    }

    private void updateSlotStatistics(Slot slot) {
        ++nSlots;
        if (slot.isSystem()) {
            ++nSystemSlots;
        } else if (slot.isIncluded()) {
            ++nIncludedSlots;
        } else {
            ++nDirectSlots;
        }
    }
}
