package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * TODO Class Comment
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class Wizard extends JDialog {
    private static final long serialVersionUID = -8629186483763293509L;
    public static final int RESULT_FINISH = 1;
    public static final int RESULT_CANCEL = 2;

    private JComponent cardHolder;
    private CardLayout layout;
    private JComponent buttonPanel;
    private JButton finishButton;
    private JButton nextButton;
    private JButton backButton;
    private JButton cancelButton;
    private int result;

    protected Wizard(JComponent owner, String title) {
        super(getFrame(owner), title, true);
        initialize();
    }

    protected Wizard(Dialog owner, String title) {
        super(owner, title, true);
        initialize();
    }

    protected void initialize() {
        getContentPane().setLayout(new BorderLayout());
        layout = new CardLayout();
        cardHolder = new JPanel(layout);
        buttonPanel = new JPanel();
        getContentPane().add(cardHolder, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        createButtons();
    }

    private static JFrame getFrame(JComponent c) {
        return (JFrame) SwingUtilities.getRoot(c);
    }

    private void createButtons() {
        finishButton = createFinishButton();
        nextButton = createNextButton();
        backButton = createBackButton();
        backButton.setEnabled(false);
        cancelButton = createCancelButton();
        Box holder = Box.createHorizontalBox();
        holder.add(backButton);
        holder.add(nextButton);
        holder.add(Box.createHorizontalStrut(3));
        holder.add(finishButton);
        holder.add(Box.createHorizontalStrut(3));
        holder.add(cancelButton);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.add(holder);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 5));
    }

    private JButton createFinishButton() {
        StandardAction action = new StandardAction("Finish") {
            private static final long serialVersionUID = -5324226196646849927L;

            public void actionPerformed(ActionEvent event) {
                result = RESULT_FINISH;
                onFinish();
                setVisible(false);
            }
        };
        action.setMnemonic('F');
        return createButton(action);
    }

    private JButton createButton(Action action) {
        JButton button = ComponentFactory.createButton(action);
        button.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {
                updateDefaultButton();
            }
            public void focusLost(FocusEvent e) {
                updateDefaultButton();
            }
        });

        return button;
    }

    private JButton createNextButton() {
        StandardAction action = new StandardAction("Next >") {
            private static final long serialVersionUID = 2762045259315677129L;

            public void actionPerformed(ActionEvent event) {
                showNextPage();
            }
        };
        action.setMnemonic('N');
        return createButton(action);
    }

    private JButton createBackButton() {
        StandardAction action = new StandardAction("< Back") {
            private static final long serialVersionUID = 8561663556453396878L;

            public void actionPerformed(ActionEvent event) {
                showPreviousPage();
            }
        };
        action.setMnemonic('B');
        return createButton(action);
    }

    private JButton createCancelButton() {
        Action action = new AbstractAction("Cancel") {
            private static final long serialVersionUID = -1294899151946474858L;

            public void actionPerformed(ActionEvent event) {
                result = RESULT_CANCEL;
                onCancel();
                setVisible(false);
            }
        };
        return createButton(action);
    }

    private int getCurrentPageIndex() {
        int currentPageIndex = 0;
        for (int i = 0; i < cardHolder.getComponentCount(); ++i) {
            Component c = cardHolder.getComponent(i);
            if (c.isVisible()) {
                currentPageIndex = i;
                break;
            }
        }
        return currentPageIndex;
    }

    private int getPageCount() {
        return cardHolder.getComponentCount();
    }

    public void addPage(WizardPage page) {
        if (page != null) {
            addPageToCardHolder(page);
            WizardPage nextPage = page.getNextPage();
            if (nextPage != null) {
                addPage(nextPage);
            }
        }
        updateButtons();
    }

    public void updateNextPage(WizardPage page) {
        if (removeFollowingPages(page)) {
            addPage(page.getNextPage());
        }
    }

    private void addPageToCardHolder(WizardPage page) {
        // Log.getLogger().fine("adding " + page);
        cardHolder.add(page, page.getName());
    }

    private void removePageFromCardHolder(int i) {
        // Log.getLogger().fine("removing " + cardHolder.getComponent(i));
        cardHolder.remove(i);
    }

    private boolean removeFollowingPages(WizardPage page) {
        int index = getPageIndex(page);
        if (index != -1) {
            for (int i = cardHolder.getComponentCount() - 1; i > index; --i) {
                removePageFromCardHolder(i);
            }
        }
        return index != -1;
    }

    private int getPageIndex(WizardPage page) {
        int index = -1;
        for (int i = 0; i < cardHolder.getComponentCount(); ++i) {
            Component c = cardHolder.getComponent(i);
            if (c == page) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int execute() {
        layout.first(cardHolder);
        ComponentUtilities.center(this);
        setVisible(true);
        return result;
    }

    public void notifyChanged(WizardPage page) {
        updateButtons();
    }

    private void updateButtons() {
        backButton.setEnabled(canGoBack());
        nextButton.setEnabled(canGoNext());
        finishButton.setEnabled(canFinish());
        updateDefaultButton();
    }

    private void updateDefaultButton() {
        if (backButton.hasFocus()) {
            setDefaultButton(backButton);
        } else if (nextButton.hasFocus()) {
            setDefaultButton(nextButton);
        } else if (finishButton.hasFocus()) {
            setDefaultButton(finishButton);
        } else if (cancelButton.hasFocus()) {
            setDefaultButton(cancelButton);
        } else if (finishButton.isEnabled()) {
            setDefaultButton(finishButton);
        } else if (nextButton.isEnabled()) {
            setDefaultButton(nextButton);
        } else {
            setDefaultButton(null);
        }
    }

    private void setDefaultButton(JButton button) {
        getRootPane().setDefaultButton(button);
    }

    protected void onFinish() {
        for (int i = 0; i < cardHolder.getComponentCount(); ++i) {
            WizardPage page = (WizardPage) cardHolder.getComponent(i);
            page.onFinish();
        }
    }

    protected void onCancel() {
        for (int i = 0; i < cardHolder.getComponentCount(); ++i) {
            WizardPage page = (WizardPage) cardHolder.getComponent(i);
            page.onCancel();
        }
    }

    public boolean canGoBack() {
        return !isShowingFirstPage();
    }

    protected boolean isShowingFirstPage() {
        return getCurrentPageIndex() == 0;
    }

    protected boolean isShowingLastPage() {
        return getCurrentPageIndex() == getPageCount() - 1;
    }

    protected WizardPage getCurrentPage() {
        return (WizardPage) cardHolder.getComponent(getCurrentPageIndex());
    }

    public boolean canGoNext() {
        WizardPage currentPage = getCurrentPage();
        return currentPage.isPageComplete() && !isShowingLastPage();
    }

    public boolean canFinish() {
        boolean canFinish = true;
        for (int i = 0; i < cardHolder.getComponentCount(); ++i) {
            WizardPage page = (WizardPage) cardHolder.getComponent(i);
            if (!page.isPageComplete()) {
                canFinish = false;
                break;
            }
        }
        return canFinish;
    }

    protected void showPreviousPage() {
        layout.previous(cardHolder);
        getCurrentPage().requestFocus();
        updateButtons();
    }

    protected void showNextPage() {
        layout.next(cardHolder);
        getCurrentPage().requestFocus();
        updateButtons();
    }
}
