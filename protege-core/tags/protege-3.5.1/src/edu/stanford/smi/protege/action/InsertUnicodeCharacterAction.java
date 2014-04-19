package edu.stanford.smi.protege.action;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.text.*;

import com.catalysoft.swing.unicode.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class InsertUnicodeCharacterAction extends TextAction {
    private static final long serialVersionUID = 738458294463754734L;
    private UnicodeChooser chooser;

    public InsertUnicodeCharacterAction() {
        super(null);
        StandardAction.initialize(this, ResourceKey.INSERT_UNICODE_ACTION, false);
    }

    public void actionPerformed(ActionEvent event) {
        final JTextComponent textComponent = getTextComponent(event);
        if (textComponent != null) {
	        if (chooser == null) {
	            Window window = SwingUtilities.windowForComponent(textComponent);
	            if (window == null) {
	                Log.getLogger().warning("no window for unicode panel");
	            } 
	            try {
	            	chooser = UnicodeChooser.instance(window);	
				} catch (RuntimeException e) {
					Log.getLogger().warning(e.getMessage());
				}	                        
	        }
	        
	        chooser.setVisible(true);
	        
			chooser.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					// This check is currently not necessary as only one property change occurs,
					// but it is good practice anyway
					if (UnicodeChooser.INSERT_CHARACTER_PROPERTY.equals(evt.getPropertyName())) {
						Character symbol = (Character) evt.getNewValue();
						if (symbol != null) {
							try {
								textComponent.getDocument().insertString(textComponent.getCaretPosition(), symbol.toString(), null);
							} catch (BadLocationException e) {								
								 // do nothing
							}
						}
					}
				}
	        });	    
        }
    }
}
