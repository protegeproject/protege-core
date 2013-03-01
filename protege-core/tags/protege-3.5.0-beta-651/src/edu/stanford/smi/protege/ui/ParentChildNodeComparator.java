package edu.stanford.smi.protege.ui;

import java.util.Comparator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.StringUtilities;

/**
 * Special comparator for Parent-Child tree (class-subclass tree), in which 
 * the system frames have a lower value than the user frames.
 * Assumes that the user objects are frames.
 *
 */
public class ParentChildNodeComparator implements Comparator {
	private static final char SPECIAL_NAME_CHAR_1 = ':';
	private static final char SPECIAL_NAME_CHAR_2 = '\'';
	
    public int compare(Object o1, Object o2) {
        if (o1 instanceof LazyTreeNode) {
            o1 = ((LazyTreeNode) o1).getUserObject();
        }
        if (o2 instanceof LazyTreeNode) {
            o2 = ((LazyTreeNode) o2).getUserObject();
        }
        
        Frame f1 = (Frame) o1;
        Frame f2 = (Frame) o2;
        
        int result;
        String t1 = f1.getBrowserText();
        String t2 = f2.getBrowserText();
        if (isSpecialName(t1)) {
            result = isSpecialName(t2) ? compareStrings(t1, t2) : -1;
        } else if (isSpecialName(t2)) {
            result = +1;
        } else {
            result = compareStrings(t1, t2);
        }
        return result;
    };
    
    private static boolean isSpecialName(String s) {
        return s.length() > 0 && s.charAt(0) == SPECIAL_NAME_CHAR_1;
    }
    
    private static int compareStrings(String s1, String s2) {
    	s1 = StringUtilities.unquote(s1);
    	s2 = StringUtilities.unquote(s2);
        int result = s1.compareToIgnoreCase(s2); 
        if (result == 0) {
            result = s1.compareTo(s2);           
        }
        return result;
    }
   
}
