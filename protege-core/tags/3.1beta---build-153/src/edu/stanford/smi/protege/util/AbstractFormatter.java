package edu.stanford.smi.protege.util;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

import edu.stanford.smi.protege.model.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractFormatter extends Formatter {
    private static final String lineSeparator = SystemUtilities.getLineSeparator();
    private static final DateFormat dateFormat = new StandardDateFormat();
    
    protected String format(LogRecord record, String user, boolean showDate, boolean showMethod) {
        StringBuffer buffer = new StringBuffer();
        if (showDate) {
            buffer.append(getDateString());
            buffer.append(" ");
        }
        if (user != null) {
            buffer.append(user);
            buffer.append(" ");
        }
        Level level = record.getLevel();
        buffer.append(level.toString());
        buffer.append(": ");
        buffer.append(record.getMessage());
        Throwable throwable = record.getThrown();
        if (throwable != null) {
            buffer.append(" -- ");
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            buffer.append(writer.getBuffer());
        } else {
	        String className = record.getSourceClassName();
	        if (showMethod && className != null) {
	            buffer.append(" -- ");
	            buffer.append(StringUtilities.getShortClassName(className));
	            buffer.append(".");
	            buffer.append(record.getSourceMethodName());
	            buffer.append("(");
	            Object[] arguments = record.getParameters();
		        if (arguments != null) {
			        for (int i = 0; i < arguments.length; ++i) {
			            if (i != 0) {
			                buffer.append(", ");
			            }
			            buffer.append(toString(arguments[i]));
			        }
		        }
		        buffer.append(")");
	        }
        }
        buffer.append(lineSeparator);
        return buffer.toString();
     }
    
    protected String getLineSeparator() {
        return lineSeparator;
    }
    
    protected String getDateString() {
        return dateFormat.format(new Date());        
    }
    
     protected String toString(Object o) {
        String string;
        try {
            if (o == null) {
                string = null;
            } else if (o instanceof Collection) {
                string = "[" + CollectionUtilities.toString((Collection) o) + "]";
            } else if (o.getClass().isArray()) {
                string = "{" + CollectionUtilities.toString(Arrays.asList((Object[]) o)) + "}";
            } else if (o instanceof Frame) {
                string = ((Frame)o).getBrowserText();
            } else {
                string = o.toString();
            }
        } catch (Exception e) {
            string = "<<toString() exception>>";
        }
        return string;
    }

}

