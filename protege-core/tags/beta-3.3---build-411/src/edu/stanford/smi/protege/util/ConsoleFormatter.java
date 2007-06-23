package edu.stanford.smi.protege.util;

import java.util.logging.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ConsoleFormatter extends AbstractFormatter {
    
    public String format(LogRecord record) {
        boolean showMethod = record.getLevel().intValue() >= Level.WARNING.intValue();
        return format(record, null, false, showMethod);
    }
}

