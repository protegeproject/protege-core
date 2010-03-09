package edu.stanford.smi.protege.model;

import java.util.*;

public class FrameCountsImpl implements FrameCounts {
    private static class CountRecord {
        //ESCA-JAVA0098 
        int clsCount;
        int slotCount;
        int facetCount;
        int simpleInstanceCount;
    }

    private KnowledgeBase _kb;
    private CountRecord _systemCountRecord = new CountRecord();
    private CountRecord _includedCountRecord = new CountRecord();

    public void updateSystemFrameCounts(KnowledgeBase kb) {
        Iterator i = kb.getFrames().iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (frame.isSystem()) {
                updateRecord(_systemCountRecord, frame);
            }
        }
    }

    private static void updateRecord(CountRecord record, Frame frame) {
        if (frame instanceof Cls) {
            ++record.clsCount;
        } else if (frame instanceof Slot) {
            ++record.slotCount;
        } else if (frame instanceof Facet) {
            ++record.facetCount;
        } else {
            ++record.simpleInstanceCount;
        }
    }

    public void updateIncludedFrameCounts(KnowledgeBase kb) {
        Iterator i = kb.getFrames().iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (frame.isIncluded() && !frame.isSystem()) {
                updateRecord(_includedCountRecord, frame);
            }
        }
    }

    public void updateDirectFrameCounts(KnowledgeBase kb) {
        _kb = kb;
    }

    public int getSystemClsCount() {
        return _systemCountRecord.clsCount;
    }

    public int getSystemSlotCount() {
        return _systemCountRecord.slotCount;
    }

    public int getSystemFacetCount() {
        return _systemCountRecord.facetCount;
    }

    public int getSystemSimpleInstanceCount() {
        return _systemCountRecord.simpleInstanceCount;
    }

    public int getSystemFrameCount() {
        return getSystemClsCount() + getSystemSlotCount() + getSystemFacetCount() + getSystemSimpleInstanceCount();
    }

    public int getIncludedClsCount() {
        return _includedCountRecord.clsCount;
    }

    public int getIncludedSlotCount() {
        return _includedCountRecord.slotCount;
    }

    public int getIncludedFacetCount() {
        return _includedCountRecord.facetCount;
    }

    public int getIncludedSimpleInstanceCount() {
        return _includedCountRecord.simpleInstanceCount;
    }

    public int getIncludedFrameCount() {
        return getIncludedClsCount() + getIncludedSlotCount() + getIncludedFacetCount()
                + getIncludedSimpleInstanceCount();
    }

    public int getDirectClsCount() {
        return _kb.getClsCount() - (getSystemClsCount() + getIncludedClsCount());
    }

    public int getDirectSlotCount() {
        return _kb.getSlotCount() - (getSystemSlotCount() + getIncludedSlotCount());
    }

    public int getDirectFacetCount() {
        return _kb.getFacetCount() - (getSystemFacetCount() + getIncludedFacetCount());
    }

    public int getDirectSimpleInstanceCount() {
        return _kb.getSimpleInstanceCount() - (getSystemSimpleInstanceCount() + getIncludedSimpleInstanceCount());
    }

    public int getDirectFrameCount() {
        return _kb.getFrameCount() - (getSystemFrameCount() + getIncludedFrameCount());
    }

    public int getTotalClsCount() {
        return getSystemClsCount() + getIncludedClsCount() + getDirectClsCount();
    }

    public int getTotalSlotCount() {
        return getSystemSlotCount() + getIncludedSlotCount() + getDirectSlotCount();
    }

    public int getTotalFacetCount() {
        return getSystemFacetCount() + getIncludedFacetCount() + getDirectFacetCount();
    }

    public int getTotalSimpleInstanceCount() {
        return getSystemSimpleInstanceCount() + getIncludedSimpleInstanceCount() + getDirectSimpleInstanceCount();
    }

    public int getTotalFrameCount() {
        return getSystemFrameCount() + getIncludedFrameCount() + getDirectFrameCount();
    }
}