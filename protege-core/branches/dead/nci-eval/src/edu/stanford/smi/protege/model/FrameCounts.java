package edu.stanford.smi.protege.model;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FrameCounts {
    public abstract int getSystemClsCount();
    public abstract int getSystemSlotCount();
    public abstract int getSystemFacetCount();
    public abstract int getSystemSimpleInstanceCount();
    public abstract int getSystemFrameCount();
    public abstract int getIncludedClsCount();
    public abstract int getIncludedSlotCount();
    public abstract int getIncludedFacetCount();
    public abstract int getIncludedSimpleInstanceCount();
    public abstract int getIncludedFrameCount();
    public abstract int getDirectClsCount();
    public abstract int getDirectSlotCount();
    public abstract int getDirectFacetCount();
    public abstract int getDirectSimpleInstanceCount();
    public abstract int getDirectFrameCount();
    public abstract int getTotalClsCount();
    public abstract int getTotalSlotCount();
    public abstract int getTotalFacetCount();
    public abstract int getTotalSimpleInstanceCount();
    public abstract int getTotalFrameCount();
}