package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Some utilities for working with Strings.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class StringUtilities {

    public static String capitalize(String words) {
        StringBuffer buffer = new StringBuffer();
        boolean isNewWord = true;
        int length = words.length();
        for (int i = 0; i < length; ++i) {
            char c = words.charAt(i);
            if (Character.isWhitespace(c)) {
                isNewWord = true;
            } else if (isNewWord) {
                c = Character.toUpperCase(c);
                isNewWord = false;
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    public static String replace(String text, String macro, String value) {
        StringBuffer buffer = new StringBuffer();
        int macroLen = macro.length();
        int start = 0;
        int end;
        while ((end = text.indexOf(macro, start)) != -1) {
            buffer.append(text.substring(start, end));
            buffer.append(value);
            start = end + macroLen;
        }
        buffer.append(text.substring(start));
        return buffer.toString();
    }

    public static String symbolToLabel(String symbol) {
        String words = symbol.replace('_', ' ');
        words = capitalize(words);
        return words;
    }

    public static String commaSeparatedList(Collection c) {
        StringBuffer text = new StringBuffer();
        Iterator i = c.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(o.toString());
        }
        return text.toString();
    }

    public static String getShortClassName(String classname) {
        int index = classname.lastIndexOf('.') + 1;
        return classname.substring(index);
    }

    public static String getClassName(Class clas) {
        String s = clas.getName();
        return getShortClassName(s);
    }

    public static String getClassName(Object o) {
        return getClassName(o.getClass());
    }

}