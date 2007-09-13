package edu.stanford.smi.protege.util;

/**
 * Utility class to check a string to see if it is a valid integer.
 * 
 * @author Ray Fergerson
 */

public class IntegerValidator implements NumberValidator {

    private static IntegerValidator _instance = new IntegerValidator();

    public static IntegerValidator getInstance() {
        return _instance;
    }

    public Number convertToNumber(String string) {
        return Integer.valueOf(string);
    }

    public String getErrorMessage(Object o) {
        return "Not a valid integer";
    }

    public boolean isValid(Object o) {
        boolean isValid = false;
        try {
            convertToNumber((String) o);
            isValid = true;
        } catch (NumberFormatException e) {
            // do nothing
        }
        return isValid;
    }

}
