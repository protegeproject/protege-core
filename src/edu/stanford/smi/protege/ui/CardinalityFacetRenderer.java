package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Renderer for cardinality facet in the template slots pane.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CardinalityFacetRenderer extends DefaultRenderer {
    private static final long serialVersionUID = 1215855408571379495L;
    private Facet _minCardinalityFacet;
    private Facet _maxCardinalityFacet;

    public CardinalityFacetRenderer(KnowledgeBase kb) {
        _minCardinalityFacet = kb.getFacet(Model.Facet.MINIMUM_CARDINALITY);
        _maxCardinalityFacet = kb.getFacet(Model.Facet.MAXIMUM_CARDINALITY);
    }

    private static Integer getFacetValue(Cls cls, Slot slot, Facet facet) {
        return (Integer) CollectionUtilities.getFirstItem(cls.getTemplateFacetValues(slot, facet));
    }

    public void load(Object o) {
        FrameSlotCombination combination = (FrameSlotCombination) o;
        Cls cls = (Cls) combination.getFrame();
        Slot slot = combination.getSlot();
        StringBuffer buffer = new StringBuffer();
        Integer min = getFacetValue(cls, slot, _minCardinalityFacet);
        int minValue = 0;
        if (min != null) {
            buffer.append("required ");
            minValue = min.intValue();
        }
        Integer max = getFacetValue(cls, slot, _maxCardinalityFacet);
        int maxValue = 0;
        if (max == null) {
            buffer.append("multiple");
        } else {
            maxValue = max.intValue();
            if (maxValue == 0) {
                buffer.append("none");
            } else if (maxValue == 1) {
                buffer.append("single");
            } else {
                buffer.append("multiple");
            }
        }
        if (minValue > 1 || maxValue > 1) {
            buffer.append("   (");
            buffer.append(minValue);
            buffer.append(":");
            if (maxValue == 0) {
                buffer.append("*");
            } else {
                buffer.append(maxValue);
            }
            buffer.append(")");
        }
        setMainText(buffer.toString());
        setGrayedText(!cls.isEditable());
        setBackgroundSelectionColor(Colors.getSlotSelectionColor());
    }
}
