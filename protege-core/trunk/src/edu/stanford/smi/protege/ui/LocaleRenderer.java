package edu.stanford.smi.protege.ui;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LocaleRenderer extends DefaultRenderer {
    private static final long serialVersionUID = -7860504931657851678L;

    public void load(Object o) {
        Locale locale = (Locale) o;
        setMainText(locale.getDisplayName(Locale.ENGLISH));
    }
}
