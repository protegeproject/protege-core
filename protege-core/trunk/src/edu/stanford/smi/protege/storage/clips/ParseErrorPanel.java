package edu.stanford.smi.protege.storage.clips;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import javax.swing.*;
import javax.xml.transform.ErrorListener;

import edu.stanford.smi.protege.util.MessageError;

/**
 * Panel to display the error messages resulting from parsing a clips file.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ParseErrorPanel extends JComponent {

    public ParseErrorPanel(Collection errors) {
        setLayout(new BorderLayout());
        JTextArea area = new JTextArea();
        add(new JScrollPane(area));
        area.setText(getText(errors));
        area.setEditable(false);
        setPreferredSize(new Dimension(500, 400));
    }

    private static String getText(Collection errors) {
    	if (errors.size() == 0)
    		return new String();
    	
        StringBuffer buffer = new StringBuffer();        
        ArrayList errorsList = new ArrayList(errors);
        
        buffer.append("There were errors at performing operation.\n\n");
        
        for (int i = errorsList.size()-1; i >= 0; i--) {;
			Object o = errorsList.get(i);
		
            String text = (errorsList.size() - i) + ". ";
       
            if (o instanceof MessageError) {
            	text = text + getMessageErrorText((MessageError)o);
            } else if (o instanceof Exception) {
                Exception ex = (Exception) o;
                
    			text = text + "    " + "Exception " + ex.getClass().toString() + 
				(ex.getMessage() == null ? "" : ". \nMessage: " + ex.getMessage());
                               
                //StringWriter s = new StringWriter();
                //e.printStackTrace(new PrintWriter(s));
                //text = text + s.toString();
            } else {
                text = text + ((o == null) ? "missing message" : o.toString());
            }
            buffer.append(text);
            buffer.append("\n\n");
        }
        
        buffer.append("See console and log for more details.");
        
        return buffer.toString();
    }

  
	private static String getMessageErrorText(MessageError error) {
		String message = new String();
		
		if (error.getMessage() != null) 
			message = message + error.getMessage() + "\n";			
				
		Exception ex = error.getException();
		if (ex != null)
			message = message + "    " + "Exception " + ex.getClass().toString() + 
				(ex.getMessage() == null ? "" : ".  Message: " + ex.getMessage());
				
		return message;
	}
}
