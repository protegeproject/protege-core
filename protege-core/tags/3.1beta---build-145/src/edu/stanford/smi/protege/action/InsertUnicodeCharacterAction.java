package edu.stanford.smi.protege.action;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import com.catalysoft.swing.unicode.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class InsertUnicodeCharacterAction extends TextAction {
    private UnicodeChooser chooser;

    public InsertUnicodeCharacterAction() {
        super(null);
        StandardAction.initialize(this, ResourceKey.INSERT_UNICODE_ACTION, false);
    }

    public void actionPerformed(ActionEvent event) {
        JTextComponent textComponent = getTextComponent(event);
        if (textComponent != null) {
	        if (chooser == null) {
	            Window window = SwingUtilities.windowForComponent(textComponent);
	            if (window == null) {
	                Log.getLogger().warning("no window");
	            } else if (window instanceof JDialog) {
	                chooser = new UnicodeChooser((Dialog) window);
	            } else if (window instanceof Frame) {
	                chooser = new UnicodeChooser((Frame) window);
	            } else {
	                Log.getLogger().warning("unknown window type " + window);
	            }
	        }
	        chooser.setVisible(true);
	        Character symbol = chooser.getSelectedCharacter();
	        if (symbol != null) {
	            try {
	                int pos = textComponent.getCaretPosition();
	                textComponent.getDocument().insertString(pos, symbol.toString(), null);
	            } catch (BadLocationException e) {
	                // do nothing
	            }
	        }
        }
    }
}
