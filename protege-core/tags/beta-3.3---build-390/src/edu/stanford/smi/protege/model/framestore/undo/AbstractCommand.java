package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
abstract class AbstractCommand implements Command {
    private FrameStore delegate;
    private String description;

    protected AbstractCommand(FrameStore delegate) {
        this.delegate = delegate;
    }

    protected FrameStore getDelegate() {
        return delegate;
    }

    public final String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected static String getText(Frame frame) {
        String s = frame.getBrowserText();
        
        if (s == null) 
        	s = frame.getName();
        
        if (s.indexOf(' ') != -1) {
            s = '\'' + s + '\'';
        }
        return s;
    }

    protected static String getText(Collection values) {
        return CollectionUtilities.toString(values);
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}
