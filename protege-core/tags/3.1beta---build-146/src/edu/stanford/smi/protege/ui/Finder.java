package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * A generic base class for the "find box" ui feature. This class handles all of
 * the UI work and just delegates the actual find and related task to some
 * template methods.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class Finder extends JComponent {
    private static List searchedForStrings = new ArrayList();
    private JComboBox _textField;
    private Action _findAction;

    public Finder(String description) {
        this(description, Icons.getFindIcon());
    }

    public Finder(String description, Icon icon) {
        _findAction = new StandardAction(description, icon) {
            public void actionPerformed(ActionEvent e) {
                if (isFindAction(e)) {
                    doFind();
                }
            }
        };
        initialize();
    }
    
    private boolean isFindAction(ActionEvent event) {
        return !event.getSource().equals(_textField) || "comboBoxChanged".equals(event.getActionCommand());
    }

    public Finder(ResourceKey key) {
        _findAction = new StandardAction(key) {
            public void actionPerformed(ActionEvent e) {
                if (isFindAction(e)) {
	                doFind();
                }
            }
        };
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        add(createTextField(), BorderLayout.CENTER);
        add(createFindButton(), BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private JComponent createFindButton() {
        JToolBar toolBar = ComponentFactory.createToolBar();
        ComponentFactory.addToolBarButton(toolBar, _findAction);
        return toolBar;
    }

    private JComponent createTextField() {
        _textField = ComponentFactory.createComboBox();
        _textField.setEditable(true);
        _textField.addActionListener(_findAction);
        _textField.addPopupMenuListener(createPopupMenuListener());
        return _textField;
    }
    
    private PopupMenuListener createPopupMenuListener() {
        return new PopupMenuListener() {

            public void popupMenuCanceled(PopupMenuEvent e) {
                // do nothing
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // do nothing
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                _textField.setModel(new DefaultComboBoxModel(searchedForStrings.toArray()));
            }
            
        };
    }

    private void recordItem(String text) {
        searchedForStrings.remove(text);
        searchedForStrings.add(0, text);
    }

    private void doFind() {
        String text = (String) _textField.getSelectedItem();
        if (text != null && text.length() != 0) {
            List matches = getMatches(text, 1000);
            if (matches.isEmpty()) {
                getToolkit().beep();
            } else if (matches.size() == 1) {
                WaitCursor cursor = new WaitCursor(this);
                recordItem(text);
                select(matches.get(0));
                cursor.hide();
            } else {
                recordItem(text);
                String title = "Select from search results (" + matches.size() + " matches)";
                int initialSelection = getBestMatch(matches, text);
                Object o = DisplayUtilities.pickInstanceFromCollection(this, matches,
                        initialSelection, title);
                if (o != null) {
                    WaitCursor cursor = new WaitCursor(this);
                    select(o);
                    cursor.hide();
                }
            }
        }
    }
    
    protected abstract int getBestMatch(List matches, String text);

    protected abstract List getMatches(String text, int maxMatches);

    protected abstract void select(Object o);

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}