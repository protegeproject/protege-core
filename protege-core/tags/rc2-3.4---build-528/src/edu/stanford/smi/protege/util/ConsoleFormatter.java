package edu.stanford.smi.protege.util;

import java.util.logging.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ConsoleFormatter extends AbstractFormatter {
    
    public String format(LogRecord record) {
    	int level = record.getLevel().intValue();
        boolean showMethod =  level >= Level.WARNING.intValue();
        boolean showLevel = (level != Level.INFO.intValue()) &&
        					(level != Level.CONFIG.intValue());
        return format(record, null, false, showMethod, true, showLevel);
    }
}

