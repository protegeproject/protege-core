package edu.stanford.smi.protege.util;

import java.util.logging.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FileFormatter extends AbstractFormatter {
    
    public String format(LogRecord record) {
        return format(record, null, true, true);
    }

}
