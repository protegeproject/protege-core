package edu.stanford.smi.protege.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;

/**
 * A class to handle all modal dialog processing.  This class just wraps the JDialog modal dialog implementation but adds
 * some additional features such as a call back mechanism to stop an "OK".  This class was originally written to work
 * around the JDK 1.0 modal dialogs that didn't work at all.  It also predates the JOptionPane stuff that is similar.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ModalDialog extends JDialog implements Disposable {
    private static final long serialVersionUID = 7759975948915176435L;
    public static final int OPTION_OK = 1;
    public static final int OPTION_YES = 2;
    public static final int OPTION_NO = 3;
    public static final int OPTION_CANCEL = 4;
    public static final int OPTION_CLOSE = 5;
    public static final int RESULT_ERROR = 6;

    public static final int MODE_OK_CANCEL = 11;
    public static final int MODE_YES_NO_CANCEL = 12;
    public static final int MODE_YES_NO = 13;
    public static final int MODE_CLOSE = 14;

    private int _result;
    private Component _panel;
    private JPanel _buttonsPanel;
    private CloseCallback _closeCallback;
    private boolean _enableCloseButton;

    private static ModalDialog _currentDialog; // used for testing

    public static interface CloseCallback {
        boolean canClose(int result);
    }

    private class WindowCloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent event) {
            int option = OPTION_CANCEL;
            if (!_enableCloseButton) {
                int result = ModalDialog.showMessageDialog(ModalDialog.this, LocalizedText
                        .getText(ResourceKey.DIALOG_SAVE_CHANGES_TEXT), ModalDialog.MODE_YES_NO);
                if (result == OPTION_YES) {
                    option = OPTION_OK;
                }
            }
            attemptClose(option);
        }
    }

    private ModalDialog(Dialog parent, Component panel, String title, int mode, CloseCallback callback,
            boolean enableClose) {
        super(parent, title, true);
        init(panel, mode, callback, enableClose);
    }

    private ModalDialog(Frame parentFrame, Component panel, String title, int mode, CloseCallback callback,
            boolean enableCloseButton) {
        super(parentFrame, title, true);
        init(panel, mode, callback, enableCloseButton);
    }

    public static void attemptDialogClose(int result) {
        ModalDialog dialog = getCurrentDialog();
        if (dialog != null) {
            dialog.attemptClose(result);
        }
    }

    public void attemptClose(int result) {
        boolean canClose;
        if (_closeCallback == null) {
            canClose = true;
        } else {
            canClose = _closeCallback.canClose(result);
        }
        if (canClose && result == OPTION_OK && _panel instanceof Validatable) {
            Validatable validatable = (Validatable) _panel;
            canClose = validatable.validateContents();
            if (canClose) {
                validatable.saveContents();
            }
        }
        if (canClose) {
            _result = result;
            close();
        }
    }

    private void close() {
        ComponentUtilities.dispose(this);
        _currentDialog = null;
    }

    private JButton createButton(final int result, ResourceKey key) {
        Action action = new StandardAction(key) {
            private static final long serialVersionUID = 1079564194674970833L;

            public void actionPerformed(ActionEvent event) {
                attemptClose(result);
            }
        };
        JButton button = ComponentFactory.createButton(action);
        button.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        attemptClose(result);
                        break;
                    case KeyEvent.VK_ESCAPE:
                        attemptClose(OPTION_CANCEL);
                        break;
                    default:
                        // do nothing
                        break;
                }
            }
        });
        return button;
    }

    private JPanel createButtonsPanel(int mode) {
        JPanel buttonsGrid = ComponentFactory.createPanel();
        buttonsGrid.setLayout(new GridLayout(1, 3, 10, 10));
        switch (mode) {
            case MODE_OK_CANCEL:
                buttonsGrid.add(createButton(OPTION_OK, ResourceKey.OK_BUTTON_LABEL));
                buttonsGrid.add(createButton(OPTION_CANCEL, ResourceKey.CANCEL_BUTTON_LABEL));
                break;
            case MODE_YES_NO:
                buttonsGrid.add(createButton(OPTION_YES, ResourceKey.YES_BUTTON_LABEL));
                buttonsGrid.add(createButton(OPTION_NO, ResourceKey.NO_BUTTON_LABEL));
                break;
            case MODE_YES_NO_CANCEL:
                buttonsGrid.add(createButton(OPTION_YES, ResourceKey.YES_BUTTON_LABEL));
                buttonsGrid.add(createButton(OPTION_NO, ResourceKey.NO_BUTTON_LABEL));
                buttonsGrid.add(createButton(OPTION_CANCEL, ResourceKey.CANCEL_BUTTON_LABEL));
                break;
            case MODE_CLOSE:
                buttonsGrid.add(createButton(OPTION_CLOSE, ResourceKey.CLOSE_BUTTON_LABEL));
                break;
            default:
                // do nothing
                break;
        }

        JPanel panel = ComponentFactory.createPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonsGrid);

        return panel;
    }

    public static ModalDialog getCurrentDialog() {
        return _currentDialog;
    }

    //  Doesn't work!
    private void getFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JButton button = (JButton) ((Container) _buttonsPanel.getComponent(0)).getComponent(0);
                button.requestFocus();
            }
        });
    }

    private void init(Component panel, int mode, CloseCallback callback, boolean enableCloseButton) {
        _currentDialog = this;
        _closeCallback = callback;
        _enableCloseButton = enableCloseButton;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowCloseListener());

        switch (mode) {
            case MODE_OK_CANCEL:
                _result = OPTION_CANCEL;
                break;
            case MODE_YES_NO_CANCEL:
                _result = OPTION_CANCEL;
                break;
            case MODE_CLOSE:
                _result = OPTION_CLOSE;
                break;
            default:
                // do nothing
                break;
        }

        _panel = panel;
        _buttonsPanel = createButtonsPanel(mode);

        layoutWidgets();

        pack();
        //ComponentUtilities.center(this);
        ComponentUtilities.centerInMainWindow(this);
        getFocus();
        setVisible(true);
    }

    private void layoutWidgets() {
        JPanel borderedPanel = ComponentFactory.createPanel();
        borderedPanel.setLayout(new BorderLayout());
        borderedPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        borderedPanel.add(_panel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(borderedPanel, BorderLayout.CENTER);
        getContentPane().add(_buttonsPanel, BorderLayout.SOUTH);
    }

    public static int showDialog(Component parent, Component panel, String title, int mode) {
        return showDialog(parent, panel, title, mode, null);
    }

    public static int showDialog(Component parent, Component panel, String title, int mode, CloseCallback callback) {
        return showDialog(parent, panel, title, mode, callback, true);
    }

    public static int showDialog(Component parent, Component panel, String title, int mode, CloseCallback callback,
            boolean enableCloseButton) {
        ModalDialog dialog;
        Window window;
        if (parent == null || parent instanceof Window) {
            window = (Window) parent;
        } else {
            window = SwingUtilities.windowForComponent(parent);
        }
        if (window instanceof Frame || window == null) {
            dialog = new ModalDialog((Frame) window, panel, title, mode, callback, enableCloseButton);
        } else {
            dialog = new ModalDialog((Dialog) window, panel, title, mode, callback, enableCloseButton);
        }
        int result;
        if (dialog == null) {
            result = RESULT_ERROR;
        } else {
            result = dialog._result;
        }
        return result;
    }

    public static void showMessageDialog(Component parent, String message) {
        showMessageDialog(parent, message, ModalDialog.MODE_CLOSE);
    }

    public static void showMessageDialog(Component parent, String message, String title) {
        showMessageDialog(parent, message, title, ModalDialog.MODE_CLOSE);
    }

    public static int showMessageDialog(Component parent, String message, int mode) {
        return showDialog(parent, new MessagePanel(message), "", mode);
    }

    public static int showMessageDialog(Component parent, String message, String title, int mode) {
        return showDialog(parent, new MessagePanel(message), title, mode);
    }

    public static Action getCloseAction(final Component c) {
        return new AbstractAction() {
            private static final long serialVersionUID = -3726864269823423931L;

            public void actionPerformed(ActionEvent event) {
                Component root = SwingUtilities.getRoot(c);
                if (root instanceof ModalDialog) {
                    ModalDialog dialog = (ModalDialog) root;
                    dialog.attemptClose(ModalDialog.OPTION_OK);
                }
            }
        };
    }

}
