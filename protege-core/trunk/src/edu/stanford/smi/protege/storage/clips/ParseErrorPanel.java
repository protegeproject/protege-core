package edu.stanford.smi.protege.storage.clips;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MessageError;

/**
 * Panel to display the error messages resulting from parsing a clips file.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ParseErrorPanel extends JComponent {

    private static final long serialVersionUID = -6138721273189019180L;


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
        
        buffer.append("Errors found performing operation.\n\n");
        
        for (int i = 0; i < errorsList.size(); i++) {;
			Object o = errorsList.get(i);
		
            String text = (i + 1) + ". ";
       
            if (o instanceof MessageError) {
            	text = text + getMessageErrorText((MessageError)o);
            } else if (o instanceof Exception) {
                Exception ex = (Exception) o;
                
    			text = text + "Exception " + ex.getClass().toString() + 
				(ex.getMessage() == null ? "" : ". \nMessage: " + ex.getMessage());

                Log.getLogger().log(Level.WARNING, "Exception caught", ex);
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
				
		Throwable ex = error.getException();
		if (ex != null)
			message = message + "    " + "Exception " + ex.getClass().toString() + 
				(ex.getMessage() == null ? "" : ".  Message: " + ex.getMessage());
				
		return message;
	}
}
