package edu.stanford.smi.protege.storage.xml;

/**
 * Escape special XML characters.  
 * 
 * This code was inspired by that from the Apache Xerces project
 */
public class XMLUtil {

    private static final int FIRST_PRINTABLE = ' ';
    private static final int LAST_PRINTABLE = 0x7E;

    /**
     * Encode special XML characters into the equivalent character references.
     */
    private static String getEntityRef(char ch) {
        String ref;
        switch (ch) {
            case '<':
                ref = "&lt;";
                break;
            case '>':
                ref = "&gt;";
                break;
            case '"':
                ref = "&quot;";
                break;
            case '\'':
                ref = "&apos;";
                break;
            case '&':
                ref = "&amp;";
                break;
            default:
                ref = null;
                break;
        }
        return ref;
    }

    /**
     * If there is a suitable entity reference for this
     * character, return it. The list of available entity
     * references is almost but not identical between
     * XML and HTML.
     */
    public static String escape(char ch) {
        String charRef = getEntityRef(ch);
        if (charRef == null) {
            if (isPrintable(ch)) {
                charRef = "" + ch;
            } else {
                charRef = "&#" + Integer.toString(ch) + ";";
            }
        }
        return charRef;
    }

    private static boolean isPrintable(char ch) {
        return (FIRST_PRINTABLE <= ch && ch <= LAST_PRINTABLE && ch != 0xF7) || ch == '\n' || ch == '\r' || ch == '\t';
    }

    /**
     * Escapes a string so it may be returned as text content or attribute
     * value. Non printable characters are escaped using character references.
     * Where the format specifies a deault entity reference, that reference
     * is used (e.g. <tt>&amp;lt;</tt>).
     *
     * @param   source   the string to escape.
     */
    public static String escape(String source) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < source.length(); ++i) {
            buffer.append(escape(source.charAt(i)));
        }
        return buffer.toString();
    }

}
