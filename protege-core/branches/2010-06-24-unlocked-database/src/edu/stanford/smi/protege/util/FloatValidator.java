package edu.stanford.smi.protege.util;

/**
 * Utility class to check a string to see if it is a valid floating point number
 * 
 * @author Ray Fergerson
 */

public class FloatValidator implements NumberValidator {
    private static FloatValidator _instance = new FloatValidator();

    public static FloatValidator getInstance() {
        return _instance;
    }

    public Number convertToNumber(String string) {
        return Float.valueOf(string);
    }

    public String getErrorMessage(Object o) {
        return "Not a valid floating point number";
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
