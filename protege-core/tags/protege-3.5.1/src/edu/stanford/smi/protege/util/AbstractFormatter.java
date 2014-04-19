package edu.stanford.smi.protege.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import edu.stanford.smi.protege.model.Frame;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractFormatter extends Formatter {
    private static final String lineSeparator = SystemUtilities.getLineSeparator();
    private static final DateFormat dateFormat = new StandardDateFormat();
       

    protected String format(LogRecord record, String user, boolean showDate, boolean showMethod) {
    	return format(record, user, showDate, showMethod, true, true);
    }
    
    //ESCA-JAVA0130 
    protected String format(LogRecord record, String user, boolean showDate, boolean showMethod, boolean printLineSeparator, boolean showLevel) {
        StringBuffer buffer = new StringBuffer();
        if (showDate) {
            buffer.append(getDateString());
            buffer.append(" ");
        }
        if (user != null) {
            buffer.append(user);
            buffer.append(" ");
        }
        
        if (showLevel) {
        	Level level = record.getLevel();
        	buffer.append(level.toString());
        	buffer.append(": ");
        }
        buffer.append(record.getMessage());
        Throwable throwable = record.getThrown();
        if (throwable == null) {
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
        } else {
            buffer.append(" -- ");
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            buffer.append(writer.getBuffer());
        }
        if (printLineSeparator) {
        	buffer.append(lineSeparator);
        }
        return buffer.toString();
    }

    protected static String getLineSeparator() {
        return lineSeparator;
    }

    protected static String getDateString() {
        return dateFormat.format(new Date());
    }

    protected static String toString(Object o) {
        String string;
        try {
            if (o == null) {
                string = null;
            } else if (o instanceof Collection) {
                string = "[" + CollectionUtilities.toString((Collection) o) + "]";
            } else if (o.getClass().isArray()) {
                string = "{" + CollectionUtilities.toString(Arrays.asList((Object[]) o)) + "}";
            } else if (o instanceof Frame) {
                string = ((Frame) o).getBrowserText();
            } else {
                string = o.toString();
            }
        } catch (Exception e) {
            string = "<<toString() exception>>";
        }
        return string;
    }

}
