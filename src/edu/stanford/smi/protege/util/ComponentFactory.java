package edu.stanford.smi.protege.util;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import edu.stanford.smi.protege.action.ConvertUnicodeSequenceAction;
import edu.stanford.smi.protege.action.InsertUnicodeCharacterAction;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;

/**
 * Factory class for making swing components, and their variants. The use of this class is not required for Protege
 * widgets. It is encouraged though. This allows for a single place to address swing bugs and look and feel issues.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 * @author Monica Crubezy <crubezy@smi.stanford.edu>
 */
public class ComponentFactory {
    public static final int STANDARD_BUTTON_HEIGHT = 25;
    public static final Dimension STANDARD_BUTTON_SIZE = new Dimension(STANDARD_BUTTON_HEIGHT, STANDARD_BUTTON_HEIGHT);
    public static final int LARGE_BUTTON_HEIGHT = 33;
    public static final int STANDARD_FIELD_HEIGHT = STANDARD_BUTTON_HEIGHT;

    private static int _offset;
    private static final int OFFSET_SIZE = 25;
    private static final int MIN_LEFT_SPLIT_PANE_WIDTH = getMinLeftSplitPaneWidth();

    private static int getMinLeftSplitPaneWidth() {
        String property = ComponentFactory.class.getName() + ".min_left_split_pane_width";
        String value = ApplicationProperties.getApplicationOrSystemProperty(property);
        int i = (value == null) ? 275 : Integer.parseInt(value);
        return i;
    }

    private static class DisposableFrame extends JFrame implements Disposable {
        private static final long serialVersionUID = 1208382900963856453L;

        DisposableFrame() {
            ComponentUtilities.registerWindow(this);
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        }

        @Override
		public void processWindowEvent(WindowEvent event) {
            if (event.getID() == WindowEvent.WINDOW_CLOSED) {
                ComponentUtilities.deregisterWindow(this);
            }
            super.processWindowEvent(event);
        }
    }

    public static void addMenuItem(JMenu menu, final Action action) {
        JMenuItem item = new JMenuItem(action) {
            private static final long serialVersionUID = 5987721943542141158L;

            @Override
			public String getText() {
                String text = null;
                if (action == null) {
                    text = super.getText();
                } else {
                    text = (String) action.getValue(Action.NAME);
                }
                return text;
            }

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableTextAntialiasing(g);
                super.paint(g);
            }
        };
        menu.add(item);
        item.setDisabledIcon((Icon) action.getValue(Action.SMALL_ICON));
    }

    public static void addMenuItemNoIcon(JMenu menu, Action action) {
        // menu.add(action);
        addMenuItem(menu, action);
    }

    public static void addSubmenu(JMenu menu, JMenu submenu) {
        menu.add(submenu);
    }

    public static JToggleButton addToggleToolBarButton(JToolBar toolBar, Action action) {
        return addToggleToolBarButton(toolBar, action, STANDARD_BUTTON_HEIGHT);
    }

    public static JToggleButton addLargeToggleToolBarButton(JToolBar toolBar, Action action) {
        return addToggleToolBarButton(toolBar, action, LARGE_BUTTON_HEIGHT);
    }

    public static JToggleButton addToggleToolBarButton(JToolBar toolBar, Action action, int width) {
        JToggleButton button = new JToggleButton(action);
        button.setToolTipText((String) action.getValue(Action.NAME));
        addToolBarButton(toolBar, action, button);
        return button;
    }

    public static void addToolBarButton(JToolBar toolBar, final Action action, final AbstractButton button) {
        button.setText(null);
        toolBar.add(button);
    }

    public static JButton addToolBarButton(JToolBar bar, Action action) {
        return addToolBarButton(bar, action, STANDARD_BUTTON_HEIGHT);
    }

    public static JButton addLargeToolBarButton(JToolBar bar, Action action) {
        return addToolBarButton(bar, action, LARGE_BUTTON_HEIGHT);
    }

    public static JButton addToolBarButton(JToolBar bar, Action action, int width) {
        JButton button = new JButton(action);
        addButton(bar, action, button, width);
        return button;
    }

    private static void addButton(JToolBar bar, Action action, AbstractButton button, int width) {
        button.setText(null);
        button.setToolTipText(StandardAction.getName(action));
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.setMnemonic(0);
        action.putValue("protege.component", button);

        Dimension size = new Dimension();
        size.width = size.height = width;
        button.setSize(size);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);

        button.setDisabledIcon((Icon) action.getValue(StandardAction.DISABLED_ICON));
        if (button.getIcon() == null) {
            button.setIcon(Icons.getUglyIcon());
        }
        bar.add(button);
    }

    private static void adjustPosition(Component c) {
        _offset = (_offset + 1) % 4;
        Point p = c.getLocation();
        p.x += _offset * OFFSET_SIZE;
        p.y += _offset * OFFSET_SIZE;
        c.setLocation(p);
    }

    private static Dimension buttonPreferredHeightSize(Dimension d) {
        d.height = Math.max(STANDARD_BUTTON_HEIGHT, d.height);
        return d;
    }

    private static int getHeight(Component c) {
        return c.getFontMetrics(c.getFont()).getHeight() + 4;
    }

    private static void configureList(JList list, Action action, boolean enableDragAndDrop) {
        list.setModel(new SimpleListModel());
        if (action != null) {
            list.addMouseListener(new DoubleClickActionAdapter(action));
        }
        if (enableDragAndDrop) {
            setupDragAndDrop(list);
            ComponentUtilities.setDragAndDropEnabled(list, enableDragAndDrop);
        }
        list.setCellRenderer(new DefaultRenderer());
        list.setFixedCellHeight(getHeight(list));
    }

    public static void configureTable(JTable table) {
        table.setRowHeight(getHeight(table));
    }

    public static void configureTree(JTree tree, Action action) {
        if (action != null) {
            tree.addMouseListener(new DoubleClickActionAdapter(action));
        }

        tree.setRootVisible(false);
        tree.setShowsRootHandles(false);
        tree.setRowHeight(getHeight(tree));
    }

    public static JButton createButton(Action action) {
        JButton button = new JButton(action);
        // initializeAbstractButton(button, action);
        return button;
    }

    //    private static JButton createButton(Action action) {
    //        JButton button = new JButton() {
    //            public Dimension getPreferredSize() {
    //                return buttonPreferredHeightSize(super.getPreferredSize());
    //            }
    //        };
    //        initializeAbstractButton(button, action);
    //        return button;
    //    }
    //
    //    private static JPanel createButtonPreferredHeightPanel() {
    //        JPanel panel = new JPanel() {
    //            public Dimension getPreferredSize() {
    //                return buttonPreferredHeightSize(super.getPreferredSize());
    //            }
    //        };
    //        return panel;
    //    }

    public static JCheckBox createCheckBox() {
        return createCheckBox("");
    }

    public static JCheckBox createCheckBox(String s) {
        JCheckBox checkBox = new JCheckBox(s) {
            private static final long serialVersionUID = -4867160758865380345L;

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableTextAntialiasing(g);
                super.paint(g);
            }
        };
        return checkBox;
    }

    public static JComboBox createComboBox() {
        JComboBox comboBox = new JComboBox() {
            private static final long serialVersionUID = 5278650003666881110L;

            @Override
			public Dimension getPreferredSize() {
                return fieldPreferredHeightSize(super.getPreferredSize());
            }

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableTextAntialiasing(g);
                super.paint(g);
            }
        };
        return comboBox;
    }

    public static JFileChooser createFileChooser(String description, String extension) {
        return createFileChooser(description, null, extension);
    }

    public static JFileChooser createFileChooser(String title, String fileDescription, String fileExtension) {
        File lastDirectory = ApplicationProperties.getLastFileDirectory();
        JFileChooser chooser = new JFileChooser(lastDirectory) {
            private static final long serialVersionUID = -8638528742036901626L;

            @Override
			public int showDialog(Component c, String s) {
                int rval = super.showDialog(c, s);
                if (rval == APPROVE_OPTION) {
                    ApplicationProperties.setLastFileDirectory(getCurrentDirectory());
                }
                return rval;
            }
        };
        chooser.setDialogTitle(title);
        if (fileExtension == null) {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else if (fileExtension.length() > 0){
            String text = fileDescription;
            chooser.setFileFilter(new ExtensionFilter(fileExtension, text));
        }
        return chooser;
    }

    public static JFileChooser createFileChooser(String title, ExtensionFilter extensionFilter) {
        File lastDirectory = ApplicationProperties.getLastFileDirectory();
        JFileChooser chooser = new JFileChooser(lastDirectory) {
            private static final long serialVersionUID = 3135157582164095101L;

            @Override
			public int showDialog(Component c, String s) {
                int rval = super.showDialog(c, s);
                if (rval == APPROVE_OPTION) {
                    ApplicationProperties.setLastFileDirectory(getCurrentDirectory());
                }
                return rval;
            }
        };
        chooser.setDialogTitle(title);
        if (extensionFilter == null) {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            chooser.setFileFilter(extensionFilter);
        }
        return chooser;
    }


    public static JFileChooser createSaveFileChooser(String title, String fileDescription, String fileExtension, final boolean overwrite) {
        File lastDirectory = ApplicationProperties.getLastFileDirectory();
        JFileChooser chooser = new JFileChooser(lastDirectory) {
            private static final long serialVersionUID = 2958086335982181478L;

            @Override
			public int showDialog(Component c, String s) {
                int rval = super.showDialog(c, s);
                if (rval == APPROVE_OPTION) {
                    ApplicationProperties.setLastFileDirectory(getCurrentDirectory());
                }
                return rval;
            }

            @Override
			public void approveSelection() {
            	if (!overwrite) {
                    return;
                }

            	File f = getSelectedFile();

            	if ( f.exists() ) {
            		String msg = "The file '"+ f.getName() + "' already exists!\nDo you want to replace it?";
            		String title = getDialogTitle();
            		int option = JOptionPane.showConfirmDialog( this, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
            		if ( option == JOptionPane.NO_OPTION ) {
            			return;
            		}
            	}
            	super.approveSelection();
            }

        };

        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle(title);
        if (fileExtension != null) {
            String text = fileDescription;
            chooser.setFileFilter(new ExtensionFilter(fileExtension, text));
        }
        return chooser;
    }


    public static JFrame createMainFrame() {
        JFrame frame = new JFrame();
        initializeFrame(frame);
        return frame;
    }

    public static JFrame createFrame() {
        JFrame frame = new DisposableFrame();
        initializeFrame(frame);
        return frame;
    }

    private static void initializeFrame(JFrame frame) {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        Image logo = ((ImageIcon) Icons.getLogoIcon()).getImage();
        frame.setIconImage(logo);
    }

    public static JLabel createLabel() {
        JLabel label = new JLabel() {
            private static final long serialVersionUID = -7070554182697163025L;

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableTextAntialiasing(g);
                super.paint(g);
            }
        };
        return label;
    }

    public static JLabel createLabel(String s) {
        JLabel label = createLabel();
        label.setText(s);
        return label;
    }

    public static JLabel createLabel(Icon icon) {
        JLabel label = createLabel();
        label.setIcon(icon);
        return label;
    }

    public static JLabel createLabel(Icon icon, int alignment) {
        JLabel label = createLabel(icon);
        label.setHorizontalAlignment(alignment);
        return label;
    }

    public static JLabel createLabel(String s, int alignment) {
        JLabel label = createLabel(s);
        label.setHorizontalAlignment(alignment);
        return label;
    }

    public static JLabel createLabel(String s, Icon icon, int alignment) {
        JLabel label = createLabel(icon, alignment);
        label.setText(s);
        return label;
    }

    public static LabeledComponent createLabeledScrollComponent(String label, JComponent basicComponent,
            Dimension preferredSize) {
        LabeledComponent component = new LabeledComponent(label, ComponentFactory.createScrollPane(basicComponent));
        if (preferredSize != null) {
            component.setPreferredSize(preferredSize);
        }
        return component;
    }

    public static LabeledComponent createLabeledScrollComponent(String label, JComponent basicComponent,
            Dimension preferredSize, JComponent headerComponent, Collection headerButtons, JComponent footerComponent) {
        LabeledComponent component = createLabeledScrollComponent(label, basicComponent, preferredSize);
        if (headerComponent != null) {
            component.setHeaderComponent(headerComponent);
        }
        if (headerButtons != null) {
            Iterator buttonList = headerButtons.iterator();
            Action button;
            while (buttonList.hasNext()) {
                if (buttonList != null) {
                    button = (Action) buttonList.next();
                    component.addHeaderButton(button);
                }
            }
        }
        if (footerComponent != null) {
            component.setFooterComponent(footerComponent);
        }
        return component;
    }

    public static JSplitPane createLeftRightSplitPane() {
        return createLeftRightSplitPane(true);
    }

    public static JSplitPane createLeftRightSplitPane(boolean autoResize) {
        JSplitPane pane = createSplitPane(JSplitPane.HORIZONTAL_SPLIT, autoResize, 0.0);
        pane.setOneTouchExpandable(true);
        return pane;
    }

    public static JSplitPane createLeftRightSplitPane(Component left, Component right) {
        return createLeftRightSplitPane(left, right, true);
    }

    public static JSplitPane createLeftRightSplitPane(Component left, Component right, boolean autoResize) {
        JSplitPane pane = createLeftRightSplitPane(autoResize);
        pane.setLeftComponent(left);
        pane.setRightComponent(right);
        return pane;
    }

    public static JList createList(Action action) {
        return createList(action, false);
    }

    public static JList createList(Action action, boolean enableDragAndDrop) {
        return createSelectableList(action, enableDragAndDrop);
    }

    public static JMenuItem createMenuItem(String s) {
        JMenuItem item = new JMenuItem(s) {
            private static final long serialVersionUID = 1486086160611316619L;

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableTextAntialiasing(g);
                super.paint(g);
            }
        };
        return item;
    }

    public static JMenu createMenu() {
        JMenu menu = new JMenu();
        return menu;
    }

    public static JMenu createMenu(String text) {
        JMenu menu = createMenu();
        menu.setText(text);
        return menu;
    }

    public static JMenu createMenu(ResourceKey key) {
        JMenu menu = createMenu();
        menu.setText(LocalizedText.getText(key));
        menu.setMnemonic(LocalizedText.getMnemonic(key));
        return menu;
    }

    public static JMenu createMenu(String text, int mnemonic) {
        JMenu menu = createMenu(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    public static JPanel createPanel() {
        JPanel panel = new JPanel();
        return panel;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField passwordField = new JPasswordField() {
            private static final long serialVersionUID = 8866087407475720099L;

            @Override
			public Dimension getPreferredSize() {
                return fieldPreferredHeightSize(super.getPreferredSize());
            }
        };
        return passwordField;
    }

    public static JRadioButton createRadioButton(String string) {
        return new JRadioButton(string);
    }

    public static JRadioButton createRadioButton(Action action) {
        return new JRadioButton(action);
    }

    public static JRadioButtonMenuItem createRadioButtonMenuItem(Action action) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem();
        initializeAbstractButton(item, action);
        return item;
    }

    public static JCheckBoxMenuItem createCheckBoxMenuItem(Action action, boolean selected) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
        item.setSelected(selected);
        initializeAbstractButton(item, action);
        return item;
    }

    public static JCheckBoxMenuItem addCheckBoxMenuItem(JMenu menu, Action action, boolean selected) {
        JCheckBoxMenuItem item = createCheckBoxMenuItem(action, selected);
        menu.add(item);
        return item;
    }

    public static JScrollPane createScrollPane() {
        JScrollPane pane = new JScrollPane();
        return pane;
    }

    public static JScrollPane createScrollPane(JComponent c) {
        JScrollPane pane = new JScrollPane(c);
        return pane;
    }

    public static JScrollPane createScrollPane(final JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.getViewport().setBackground(table.getBackground());
        return pane;
    }

    public static SelectableList createSelectableList(Action action) {
        return createSelectableList(action, false);
    }

    public static SelectableList createSelectableList(Action action, boolean enableDragAndDrop) {
        SelectableList list = new SelectableList();
        configureList(list, action, enableDragAndDrop);
        return list;
    }

    public static SelectableTable createSelectableTable(Action action) {
        SelectableTable table = new SelectableTable();
        if (action != null) {
            table.addMouseListener(new DoubleClickActionAdapter(action));
        }
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setColumnSelectionAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setAutoCreateColumnsFromModel(false);
        table.setDefaultEditor(Object.class, null);
        return table;
    }

    public static SelectableTree createSelectableTree(Action action) {
        return createSelectableTree(action, null);
    }

    public static SelectableTree createSelectableTree(Action action, LazyTreeRoot root) {
        SelectableTree tree = new SelectableTree(action, root);
        return tree;
    }

    public static SelectableList createSingleItemList(Action action) {
        SelectableList list = (SelectableList) createList(action);
        list.setPreferredSize(new Dimension(1, STANDARD_BUTTON_HEIGHT));
        list.setFixedCellHeight(STANDARD_BUTTON_HEIGHT - 4);
        list.setBorder(BorderFactory.createEtchedBorder());
        return list;
    }

    private static void setSplitPaneComponentMinimumSize(Component c) {
        if (c instanceof JComponent) {
            ((JComponent) c).setMinimumSize(new Dimension(0, 0));
        }
    }

    private static JSplitPane createSplitPane(int direction, final boolean autoResize, double resizeWeight) {
        JSplitPane pane = new JSplitPane(direction, autoResize) {
            private static final long serialVersionUID = 6260228646159934377L;

            @Override
			public void addImpl(Component component, Object constraint, int i) {
                super.addImpl(component, constraint, i);
                setSplitPaneComponentMinimumSize(component);
            }

            private boolean initialized;

            /**
             * @deprecated
             */
            @Deprecated
			@Override
			public void reshape(int x, int y, int w, int h) {
                super.reshape(x, y, w, h);
                if (!initialized && w != 0 && h != 0) {
                    initialized = true;
                    if (getOrientation() == VERTICAL_SPLIT) {
                        int location = getHeight() - getBottomComponent().getPreferredSize().height;
                        setDividerLocation(location);
                    } else {
                        int location = getLeftComponent().getPreferredSize().width;
                        location = Math.max(location, MIN_LEFT_SPLIT_PANE_WIDTH);
                        setDividerLocation(location);
                    }
                }
            }
        };
        pane.setBorder(null);
        pane.setResizeWeight(resizeWeight);
        return pane;
    }

    public static Border createStandardBorder() {
        return BorderFactory.createEmptyBorder(5, 5, 5, 5);
    }

    public static Border createThinStandardBorder() {
        return BorderFactory.createEmptyBorder(3, 3, 3, 3);
    }

    public static JTabbedPane createTabbedPane(final boolean addBorder) {
        JTabbedPane pane = new JTabbedPane() {
            private static final long serialVersionUID = 2618479387074156744L;

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableTextAntialiasing(g);
                super.paint(g);
            }

            @Override
			public void addImpl(Component component, Object constraints, int index) {
                if (addBorder) {
                    JComponent c = (JComponent) component;
                    c.setBorder(BorderFactory.createCompoundBorder(createThinStandardBorder(), c.getBorder()));
                }
                super.addImpl(component, constraints, index);
            }

        };
        return pane;
    }

    public static JTable createTable(Action action) {
        return createSelectableTable(action);
    }

    public static JTextArea createTextArea() {
        JTextArea area = new JTextArea() {
            private static final long serialVersionUID = 2549315674090075218L;

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableTextAntialiasing(g);
                super.paint(g);
            }

            @Override
			public void setText(String text) {
                super.setText(text);
                setCaretPosition(0);
                repaint();
            }
        };
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        configureTextComponent(area);
        return area;
    }

    public static JTextField createTextField(String s) {
        JTextField field = createTextField();
        field.setText(s);
        return field;
    }

    public static JTextField createTextField() {
        JTextField textField = new JTextField() {
            private static final long serialVersionUID = 6399707890142979289L;

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableTextAntialiasing(g);
                super.paint(g);
            }

            @Override
			public Dimension getPreferredSize() {
                return fieldPreferredHeightSize(super.getPreferredSize());
            }
        };
        configureTextComponent(textField);
        return textField;
    }

    private static final String UNICODE_CHOOSER_CLASS = "com.catalysoft.swing.unicode.UnicodeChooser";

    private static void configureTextComponent(JTextComponent component) {
        addAction(component, KeyEvent.VK_X, InputEvent.ALT_MASK, new ConvertUnicodeSequenceAction());
        if (SystemUtilities.forName(UNICODE_CHOOSER_CLASS) != null) {
            addAction(component, KeyEvent.VK_I, InputEvent.ALT_MASK, new InsertUnicodeCharacterAction());
        }
    }

    private static void addAction(JTextComponent component, int keyCode, int modifiers, Action action) {
        Keymap keymap = component.getKeymap();
        KeyStroke stroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        keymap.addActionForKeyStroke(stroke, action);
    }

    public static JTextPane createTextPane() {
        JTextPane pane = new JTextPane();
        return pane;
    }

    public static JToggleButton createToggleButton(Action action) {
        JToggleButton button = new JToggleButton() {
            private static final long serialVersionUID = 8473450251620423797L;

            @Override
			public Dimension getPreferredSize() {
                return buttonPreferredHeightSize(super.getPreferredSize());
            }
        };
        initializeAbstractButton(button, action);
        return button;
    }

    public static JToolBar createToolBar() {
        JToolBar bar = new JToolBar() {
            private static final long serialVersionUID = -4194085314294949784L;

            @Override
			public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = STANDARD_BUTTON_HEIGHT;
                return d;
            }
        };
        bar.setOpaque(false);
        bar.setRollover(true);
        bar.setFloatable(false);
        bar.setBorderPainted(false);
        bar.setBorder(null);
        return bar;
    }

    public static JSplitPane createTopBottomSplitPane(boolean autoResize) {
        return createSplitPane(JSplitPane.VERTICAL_SPLIT, autoResize, 1.0);
    }

    public static JSplitPane createTopBottomSplitPane() {
        return createTopBottomSplitPane(true);
    }

    public static JSplitPane createTopBottomSplitPane(Component top, Component bottom) {
        return createTopBottomSplitPane(top, bottom, true);
    }

    public static JSplitPane createTopBottomSplitPane(Component top, Component bottom, boolean autoResize) {
        JSplitPane pane = createTopBottomSplitPane(autoResize);
        pane.setTopComponent(top);
        pane.setBottomComponent(bottom);
        return pane;
    }

    public static JTree createTree(Action action) {
        return createSelectableTree(action);
    }

    public static JWindow createWindow() {
        JWindow window = new JWindow();
        return window;
    }

    private static Dimension fieldPreferredHeightSize(Dimension d) {
        d.height = STANDARD_FIELD_HEIGHT;
        return d;
    }

    public static JComponent getCloseButtonPanel(final JFrame frame) {
        JComponent c = new JPanel();
        c.setLayout(new FlowLayout());
        JButton button = createButton(new AbstractAction("Close", Icons.getCloseIcon()) {
            private static final long serialVersionUID = 7477504460910818473L;

            public void actionPerformed(ActionEvent event) {
                ComponentUtilities.closeWindow(frame);
            }
        });
        c.add(button);
        return c;
    }

    private static void initializeAbstractButton(final AbstractButton button, final Action action) {
        action.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                button.setEnabled(action.isEnabled());
            }
        });
        button.setEnabled(action.isEnabled());
        button.addActionListener(action);
        button.setIcon((Icon) action.getValue(Action.SMALL_ICON));
        button.setAlignmentX(0.5f);
        button.setAlignmentY(0.5f);
        button.setText((String) action.getValue(Action.NAME));
        button.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
    }

    private static void setupDragAndDrop(JList list) {

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_COPY_OR_MOVE,
                new DefaultListDragSourceListener());
        new DropTarget(list, DnDConstants.ACTION_COPY_OR_MOVE, new ListTarget());
    }

    public static JFrame showInFrame(Component panel, String title) {
        JFrame frame = createFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setTitle(title);
        frame.pack();
        //ComponentUtilities.center(frame);
        ComponentUtilities.centerInMainWindow(frame);
        adjustPosition(frame);

        frame.setVisible(true);
        return frame;
    }


    public static JFrame showMessageInFrame(String message, String title) {
        JFrame frame = createFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container c = frame.getContentPane();
        c.setLayout(new BorderLayout());
        JPanel innerPane = new JPanel(new BorderLayout());
        innerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel label = new JLabel(message);
        innerPane.add(label);
        c.add(innerPane, BorderLayout.CENTER);
        frame.pack();
        frame.setTitle(title);
        ComponentUtilities.centerInMainWindow(frame);
        adjustPosition(frame);
        frame.setVisible(true);
        return frame;
    }


	public static void showTableRowInDialog(TableModel tableModel, int row, Component parentComp) {
		if (row < 0 || row > tableModel.getRowCount()) {
			return;
		}
		LinkedHashMap<String, String> colNameToValue = new LinkedHashMap<String, String>();
		for (int col = 0; col < tableModel.getColumnCount(); col++) {
			String colName = tableModel.getColumnName(col);
			if (colName == null) {
				colName = "Column " + (col + 1);
			}
			colNameToValue.put(colName, tableModel.getValueAt(row, col).toString());
		}

		JPanel panel = new JPanel(new GridLayout(tableModel.getColumnCount(), 1, 5, 5));

		for (String colName : colNameToValue.keySet()) {
			panel.add(new JLabel("<html>" + colName + ": <b>" + colNameToValue.get(colName) + "</b></html>"));
		}

		ModalDialog.showDialog(parentComp, panel, "Table row details", ModalDialog.MODE_CLOSE);

	}


    public static JEditorPane createEditorPane() {
        return new JEditorPane();
    }

    public static JEditorPane createHTMLBrowser(URL url) {
        JEditorPane pane = new JEditorPane() {
            private static final long serialVersionUID = -6684758167149030263L;

            @Override
			public void paint(Graphics g) {
                ComponentUtilities.enableAllAntialiasing(g);
                super.paint(g);
            }
        };
        pane.setEditable(false);
        pane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    SystemUtilities.showHTML(e.getURL().toString());
                }
            }
        });
        if (url != null) {
            setPage(pane, url);
        }
        return pane;
    }

    public static void setPage(JEditorPane pane, URL url) {
        try {
            pane.setPage(url);
        } catch (IOException e) {
            Log.getLogger().warning(e.toString());
        }
    }

    public static JLabel createSmallFontLabel(String text) {
        JLabel label = createLabel(text);
        ComponentUtilities.setSmallLabelFont(label);
        return label;
    }

    public static JLabel createTitleFontLabel(String text) {
        JLabel label = createLabel(text);
        ComponentUtilities.setTitleLabelFont(label);
        return label;
    }
}