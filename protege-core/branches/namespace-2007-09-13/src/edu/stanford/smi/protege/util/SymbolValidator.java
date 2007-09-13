package edu.stanford.smi.protege.util;

/**
 * A validator that tests whether a string is a valid Protege symbol type.  Symbols may not contain spaces or parenthesis.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SymbolValidator implements Validator {

    private static boolean containsAny(String s, String test) {
        boolean contains = false;
        int len = test.length();
        for (int i = 0; !contains && i < len; ++i) {
            contains = s.indexOf(test.charAt(i)) >= 0;
        }
        return contains;
    }

    public String getErrorMessage(Object o) {
        return "Contains invalid characters";
    }

    public boolean isValid(Object o) {
        boolean isValid = true;
        if (o instanceof String) {
            String s = (String) o;
            if (containsAny(s, " \t\n\r()")) {
                isValid = false;
            }
        }
        return isValid;
    }
}
