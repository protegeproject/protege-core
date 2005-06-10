package edu.stanford.smi.protege.storage.clips;

import java.io.*;
import java.net.*;

import edu.stanford.smi.protege.util.*;

/**
 * Utilities for handling text io in clips file format.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClipsUtil {

    public static final String TOP_LEVEL_SLOT_CLASS = ":CLIPS_TOP_LEVEL_SLOT_CLASS";
    public static final String FALSE = "FALSE";
    public static final String TRUE = "TRUE";

    private static char REAL_SPACE = ' ';
    private static char FAKE_SPACE = '^';

    private static String replace(String s, char inchar, char outchar) {
        String newString;
        int index = s.indexOf(inchar);
        if (index == -1) {
            newString = s;
        } else {
            StringBuffer buffer = new StringBuffer(s);
            for (int i = index; i != -1; i = s.indexOf(inchar, i + 1)) {
                buffer.setCharAt(i, outchar);
            }
            newString = buffer.toString();
        }
        return newString.intern();
    }

    public static String toExternalString(String s) {
        StringBuffer buffer = new StringBuffer();
        buffer.append('"');
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\r':
                    // discard
                    break;
                case '\"':
                    buffer.append("\\\"");
                    break;
                case '\\':
                    buffer.append("\\\\");
                    break;
                default:
                    buffer.append(c);
                    break;
            }
        }
        buffer.append('"');
        return buffer.toString();
    }

    public static String toExternalSymbol(String s) {
        String symbol;
        try {
            symbol = URLEncoder.encode(s, FileUtilities.getWriteEncoding());
        } catch (UnsupportedEncodingException e) {
            symbol = s;
            Log.getLogger().warning(e.getMessage());
        }
        return symbol;
    }

    public static String toInternalString(String s) {
        String newString;
        if (s.charAt(0) == '"') {
            int len = s.length();
            int bufferLen = 0;
            char[] buffer = new char[len];
            for (int i = 1; i < len - 1; ++i) {
                char c = s.charAt(i);
                if (c == '\\') {
                    //ESCA-JAVA0119 
                    ++i;
                    c = s.charAt(i);
                    if (c == 'n') {
                        c = '\n';
                    } else if (c == 'r') {
                        // simply discard '\r'
                        continue;
                    }
                }
                buffer[bufferLen] = c;
                ++bufferLen;
            }
            newString = new String(buffer, 0, bufferLen);
        } else {
            newString = s;
        }
        return newString.intern();
    }

    public static String toInternalSymbol(String s) {
        // needed for backward compatibility
        String result = replace(s, FAKE_SPACE, REAL_SPACE);

        /* We have to be careful here.  Legacy strings contain '%' characters that are not part of 
         * a URL encoded sequence.  If the decoding fails then we assume that that problem is that
         * it is operating on a legacy string.  Thus we can just skip then entire decoding.
         */
        try {
            result = URLDecoder.decode(result, FileUtilities.getReadEncoding()).intern();
        } catch (Exception e) {
            // do nothing
        }
        return result.intern();
    }
}
