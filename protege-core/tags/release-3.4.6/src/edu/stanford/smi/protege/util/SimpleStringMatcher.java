package edu.stanford.smi.protege.util;

import java.util.regex.Pattern;

/**
 * A string matcher implementation.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SimpleStringMatcher implements StringMatcher {
    private Pattern pattern;
    private static final char WILDCARD = '*';
    private static final String SPECIAL = "[]{}()^+.?-:,&\\";

    public SimpleStringMatcher(String s) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == WILDCARD) {
                buffer.append(".*");
            } else if (SPECIAL.indexOf(c) != -1) {
                buffer.append('\\');
                buffer.append(c);
            } else {
                buffer.append(c);
            }
        }
        pattern = Pattern.compile(buffer.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    public boolean isMatch(String value) {    	
        return pattern.matcher(value).matches();
    }
}