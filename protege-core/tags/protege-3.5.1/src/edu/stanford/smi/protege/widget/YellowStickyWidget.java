package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for handling annotation text for instance annotations (yellow stickies)
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class YellowStickyWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = 1775374880544905639L;
    private static final Color YELLOW_STICKY_COLOR = new Color(255, 255, 204);
    private JTextArea _textArea;

    public Collection getValues() {
        Collection result;
        String text = _textArea.getText();
        text = text.trim();
        if (text.length() == 0) {
            result = Collections.EMPTY_LIST;
        } else {
            result = CollectionUtilities.createCollection(text);
        }
        return result;
    }

    public void addNotify() {
        super.addNotify();
        ComponentUtilities.requestFocus(_textArea);
    }

    public void initialize() {
        _textArea = ComponentFactory.createTextArea();
        _textArea.setBackground(YELLOW_STICKY_COLOR);
        Font oldFont = _textArea.getFont();
        Font newFont = new Font(oldFont.getName(), Font.ITALIC, oldFont.getSize());
        _textArea.setFont(newFont);
        _textArea.getDocument().addDocumentListener(new DocumentChangedListener() {
            public void stateChanged(ChangeEvent event) {
                valueChanged();
            }
        });
        JScrollPane pane = new JScrollPane(_textArea);
        pane.setBorder(null);
        add(pane);
        setPreferredRows(2);
        setPreferredColumns(2);
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return TextComponentWidget.isSuitable(cls, slot, facet);
    }

    public void setBorder(Border b) {
        /* at design time we need a border because the selection rectangle is a border.
         * At runtime it is not essential and the widget looks better without one
         */
        if (isDesignTime()) {
            super.setBorder(b);
        } else {
            // do nothing
        }
    }

    public void setEditable(boolean editable) {
        _textArea.setEnabled(editable);
    }

    public void setValues(Collection values) {
        String text = (String) CollectionUtilities.getFirstItem(values);
        if (text == null) {
            text = "";
        }
        _textArea.setText(text);
    }
}
