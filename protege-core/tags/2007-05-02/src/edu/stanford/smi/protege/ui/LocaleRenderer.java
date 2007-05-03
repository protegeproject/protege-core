package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LocaleRenderer extends DefaultRenderer {
    public void load(Object o) {
        Locale locale = (Locale) o;
        setMainText(locale.getDisplayName(Locale.ENGLISH));
    }
}
