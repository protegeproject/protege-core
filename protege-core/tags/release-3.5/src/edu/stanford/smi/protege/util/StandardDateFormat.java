package edu.stanford.smi.protege.util;

import java.text.*;
import java.util.*;

/**
 * The formatter defines the standard way that dates/times are "stringified"
 * for storage in Protege.  The representation is meant to be simple to parse and
 * to sort.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class StandardDateFormat extends SimpleDateFormat {
    private static final long serialVersionUID = -3688220389358730348L;
    private static final String FORMAT_PATTERN = "yyyy.MM.dd HH:mm:ss.SSS zzz";
    private static final String OLD_FORMAT_PATTERN = "yyyy.MM.dd HH:mm:ss.SSS";

    private static SimpleDateFormat _oldFormat = new SimpleDateFormat(OLD_FORMAT_PATTERN);

    public StandardDateFormat() {
        super(FORMAT_PATTERN);
    }
    
    public static String now() {
        return new StandardDateFormat().format(new Date());
    }

    public Date parse(String text) throws ParseException {
        Date result;
        try {
            result = super.parse(text);
        } catch (ParseException e) {
            result = _oldFormat.parse(text);
        }
        return result;
    }
}
