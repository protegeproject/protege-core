package edu.stanford.smi.protege.model.framestore;

import java.io.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */

public class Sft implements Externalizable, Localizable {
    private Slot _slot;
    private Facet _facet;
    private boolean _isTemplate;
    private int _hashCode;

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(_slot);
        out.writeObject(_facet);
        out.writeBoolean(_isTemplate);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _slot = (Slot) in.readObject();
        _facet = (Facet) in.readObject();
        _isTemplate = in.readBoolean();
        cacheHashCode();
    }
    
    public String toString() {
        return "Sft(" + _slot + ", " + _facet + ", " + _isTemplate + ")";
    }

    public Sft(Slot slot, Facet facet, boolean isTemplate) {
        set(slot, facet, isTemplate);
    }
    public Sft() {
    }

    public Slot getSlot() {
        return _slot;
    }

    public Facet getFacet() {
        return _facet;
    }

    public boolean isOwnSlot() {
        return _facet == null && !_isTemplate;
    }

    public boolean isTemplateSlot() {
        return _facet == null && _isTemplate;
    }

    public boolean isTemplateFacet() {
        return _facet != null && _isTemplate;
    }

    public void set(Slot slot, Facet facet, boolean isTemplate) {
        _slot = slot;
        _facet = facet;
        _isTemplate = isTemplate;
        cacheHashCode();
    }
    
    private void cacheHashCode() {
        _hashCode = HashUtils.getHash(_slot, _facet, _isTemplate);
    }
    
    public int hashCode() {
        return _hashCode;
    }
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Sft) {
            Sft rhs = (Sft) o;
            if (_hashCode == rhs._hashCode) {
                result = equals(_slot, rhs._slot) && equals(_facet, rhs._facet) && _isTemplate == rhs._isTemplate;
            } else {
                Log.getLogger().warning("equal hashs but different sfts");
            }
        }
        return result;
    }
    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }
    
    public void localize(KnowledgeBase kb) {
        LocalizeUtils.localize(_slot, kb);
        LocalizeUtils.localize(_facet, kb);
    }
}
