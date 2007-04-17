package edu.stanford.smi.protege.model;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FrameCounts {
    int getSystemClsCount();

    int getSystemSlotCount();

    int getSystemFacetCount();

    int getSystemSimpleInstanceCount();

    int getSystemFrameCount();

    int getIncludedClsCount();

    int getIncludedSlotCount();

    int getIncludedFacetCount();

    int getIncludedSimpleInstanceCount();

    int getIncludedFrameCount();

    int getDirectClsCount();

    int getDirectSlotCount();

    int getDirectFacetCount();

    int getDirectSimpleInstanceCount();

    int getDirectFrameCount();

    int getTotalClsCount();

    int getTotalSlotCount();

    int getTotalFacetCount();

    int getTotalSimpleInstanceCount();

    int getTotalFrameCount();
}