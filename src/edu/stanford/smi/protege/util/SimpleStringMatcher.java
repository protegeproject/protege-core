package edu.stanford.smi.protege.util;


/**
 * A string matcher implementation that can handle a wildcard at the begining and/or the end of a string.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SimpleStringMatcher implements StringMatcher {
    private static final int EXACT 			= 0;
    private static final int STARTS_WITH 	= 1;
    private static final int ENDS_WITH 		= 3;
    private static final int ANY 			= 4;
    private int _matchType;
    private String _matchString;

    private static final char WILDCARD = '*';

    public SimpleStringMatcher(String s) {
        char first = s.charAt(0);
        int len = s.length();
        char last = s.charAt(len-1);
        if (first == WILDCARD) {
            if (last == WILDCARD) {
                _matchType = ANY;
                if (len == 1) {
                    _matchString = "";
                } else {
                    _matchString = s.substring(1, len-1);
                }
            } else {
                _matchType = ENDS_WITH;
                _matchString = s.substring(1, len);
            }
        } else {
            if (last == WILDCARD) {
                _matchType = STARTS_WITH;
                _matchString = s.substring(0, len-1);
            } else {
                _matchType = EXACT;
                _matchString = s;
            }
        }
        _matchString = _matchString.toLowerCase();

    }

    /**
     * isMatch method comment.
     */
    public boolean isMatch(String value) {
        value = value.toLowerCase();
        boolean result;
        switch (_matchType) {
            case EXACT :
                result = value.equals(_matchString);
                break;
            case STARTS_WITH :
                result = value.startsWith(_matchString);
                break;
            case ENDS_WITH :
                result = value.endsWith(_matchString);
                break;
            case ANY :
                result = value.indexOf(_matchString) >= 0;
                break;
            default :
                Assert.fail("bad type: " + _matchType);
                result = false;
        }
        return result;
    }
}
