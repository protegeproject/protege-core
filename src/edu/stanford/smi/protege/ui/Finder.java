package edu.stanford.smi.protege.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.StandardAction;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.WaitCursor;

/**
 * A generic base class for the "find box" ui feature. This class handles all of
 * the UI work and just delegates the actual find and related task to some
 * template methods.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class Finder extends JComponent {
    private static final long serialVersionUID = 5434797981046601159L;
    private static List searchedForStrings = new ArrayList();
    private JComboBox _comboBox;
    private Action _findButtonAction;
    private static final int MAX_MATCHES;
    private static final int DEFAULT_MAX_MATCHES = 100;
    private ListCellRenderer cellRenderer;

    static {
        MAX_MATCHES = ApplicationProperties.getIntegerProperty(Finder.class.getName() + ".max_matches",
                DEFAULT_MAX_MATCHES);
    }

    protected Finder(String description) {
        this(description, Icons.getFindIcon());
    }

    protected Finder(String description, Icon icon) {
        _findButtonAction = new StandardAction(description, icon) {
            private static final long serialVersionUID = 8031258886171785419L;

            public void actionPerformed(ActionEvent e) {
                doFind();
            }
        };
        initialize();
    }

    protected Finder(ResourceKey key) {
        _findButtonAction = new StandardAction(key) {
            private static final long serialVersionUID = 3573450038349614433L;

            public void actionPerformed(ActionEvent e) {
                doFind();
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
        ComponentFactory.addToolBarButton(toolBar, _findButtonAction);
        return toolBar;
    }

    private JComponent createTextField() {
        _comboBox = ComponentFactory.createComboBox();
        _comboBox.setEditable(true);
        _comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String command = event.getActionCommand();
                int modifiers = event.getModifiers();
                if (command.equals("comboBoxChanged") && (modifiers & InputEvent.BUTTON1_MASK) != 0) {
                    doFind();
                }

            }
        });
        _comboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    doFind();
                }
            }
        });
        _comboBox.addPopupMenuListener(createPopupMenuListener());
        return _comboBox;
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
                _comboBox.setModel(new DefaultComboBoxModel(searchedForStrings.toArray()));
            }

        };
    }

    private static void recordItem(String text) {
        searchedForStrings.remove(text);
        searchedForStrings.add(0, text);
    }

    private void doFind() {
        String text = (String) _comboBox.getSelectedItem();
        if (text != null && text.length() != 0) {
            List matches = getMatches(text, MAX_MATCHES);
            if (matches.isEmpty()) {
                getToolkit().beep();
                ModalDialog.showMessageDialog(this, "No matches found");
            } else if (matches.size() == 1) {
                WaitCursor cursor = new WaitCursor(this);
                recordItem(text);
                select(matches.get(0));
                cursor.hide();
            } else {
                recordItem(text);
                if (matches.size() == MAX_MATCHES) {
                    ModalDialog.showMessageDialog(this, "Search results limited to " + MAX_MATCHES);
                }
                String title = "Select from search results (" + matches.size() + " matches)";
                int initialSelection = getBestMatch(matches, text);
                Object o = DisplayUtilities.pickInstanceFromCollection(this, matches, initialSelection, title, cellRenderer);
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
    
    /**
     * Sets the enabled state of the Finder control.
     * Setting enabled to false will disable the finder combobox and button.
     */
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	_comboBox.setEnabled(enabled);
    	_findButtonAction.setEnabled(enabled);
    }
    
    public void setCellRenderer(ListCellRenderer cellRenderer) {
        this.cellRenderer = cellRenderer;
    }
    
}