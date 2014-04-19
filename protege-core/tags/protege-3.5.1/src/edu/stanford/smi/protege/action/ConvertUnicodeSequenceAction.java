package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.text.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ConvertUnicodeSequenceAction extends TextAction {

    private static final long serialVersionUID = -7504061449538436768L;

    public ConvertUnicodeSequenceAction() {
        super(LocalizedText.getText(ResourceKey.CONVERT_UNICODE_SEQUENCE_ACTION));
    }

    public void actionPerformed(ActionEvent event) {
        JTextComponent component = getTextComponent(event);
        StringBuffer text = new StringBuffer(component.getText());
        int pos = component.getCaretPosition();
        int startPos = Math.max(pos - 4, 0);
        String sequence = text.substring(startPos, pos);
        try {
            char code = (char) Integer.valueOf(sequence, 16).intValue();
            text = text.replace(startPos, pos, Character.toString(code));
            component.setText(text.toString());
            component.setCaretPosition(startPos + 1);
        } catch (NumberFormatException exception) {
            // do nothing
            SystemUtilities.beep();
        }
    }

}
