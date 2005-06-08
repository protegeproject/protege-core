package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Startup dialog that is displayed when a user starts Protege (without
 * double clicking on a project file).  The dialog is displayed on top of the
 * main window and gives the user the option to create a new project, open
 * a recently used project, or launch one of several help topics.
 *
 * @author Jennifer Vendetti
 */
public class WelcomeDialog extends JDialog {

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    JPanel centerPanel = new JPanel(new BorderLayout());
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

    // Panels for create new project button and protege logo.
    JPanel newButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel iconPanel = new JPanel(new FlowLayout());
    JPanel topPanel = new JPanel(new BorderLayout());

    // Panels for opening recently used/other projects.
    JPanel mruPanel = new JPanel(new BorderLayout());
    JPanel openButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel openPanel = new JPanel(new BorderLayout());

    // Panels for help buttons.
    JPanel helpPanel = new JPanel(new BorderLayout());
    JPanel hspHolder = new JPanel(new FlowLayout());
    JPanel helpSubPanel = new JPanel(new GridLayout(4, 1, 0, 2));

    TitledBorder titledBorder1;
    TitledBorder titledBorder2;

    JButton closeButton = createButton(ResourceKey.CLOSE_BUTTON_LABEL, Icons.getCloseIcon());
    JButton faqButton = createButton(ResourceKey.WELCOME_DIALOG_FAQ);
    JButton newButton = createButton(ResourceKey.WELCOME_DIALOG_NEW, Icons.getNewProjectIcon());
    JButton openButton = createButton(ResourceKey.WELCOME_DIALOG_OPEN);
    JButton openOtherButton = createButton(ResourceKey.WELCOME_DIALOG_OPEN_OTHER, Icons.getOpenProjectIcon());
    JButton topicsButton = createButton(ResourceKey.WELCOME_DIALOG_ALL_TOPICS);
    JButton tutorialButton = createButton(ResourceKey.WELCOME_DIALOG_GETTING_STARTED);
    JButton usersGuideButton = createButton(ResourceKey.WELCOME_DIALOG_USERS_GUIDE);

    ButtonGroup group = new ButtonGroup();
    ProjectList mruList;
    JScrollPane mruScrollPane;
    JLabel iconLabel;
    JRadioButton[] rbArray;
    List projectURIList = new ArrayList(ApplicationProperties.getMRUProjectList());

    // Extended the JList class in order to have tool tips for each
    // individual list item.
    private class ProjectList extends JList {

        public ProjectList(DefaultListModel model) {
            this.setModel(model);
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        public String getToolTipText(MouseEvent event) {
            String toolTip = null;
            int index = locationToIndex(event.getPoint());
            if (index >= 0) {
                toolTip = projectURIList.get(index).toString();
            }
            return toolTip;
        }
    }

    public WelcomeDialog(java.awt.Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            jbInit();
            pack();

            Runnable doFocus = new Runnable() {
                public void run() {
                    closeButton.requestFocus();
                }
            };
            SwingUtilities.invokeLater(doFocus);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        titledBorder1 = createBorder(ResourceKey.WELCOME_DIALOG_OPEN_RECENT_PROJECT_TITLE);
        titledBorder2 = createBorder(ResourceKey.WELCOME_DIALOG_HELP_TITLE);
        openPanel.setBorder(titledBorder1);
        hspHolder.setBorder(titledBorder2);

        /**
         * Build top panel
         */

        setToolTipText(newButton, ResourceKey.WELCOME_DIALOG_NEW_TOOLTIP);
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newButton_actionPerformed(ae);
            }
        });
        newButtonPanel.add(newButton);

        setToolTipText(openOtherButton, ResourceKey.WELCOME_DIALOG_OPEN_OTHER_TOOLTIP);
        openOtherButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openOtherButton_actionPerformed(ae);
            }
        });
        newButtonPanel.add(openOtherButton);

        iconLabel = new JLabel("   ", Icons.getLogo(), SwingConstants.LEFT);
        iconPanel.add(iconLabel);

        topPanel.add(newButtonPanel, BorderLayout.CENTER);
        topPanel.add(iconPanel, BorderLayout.EAST);

        /**
         * Build Open Recent/Other Project panel
         */

        initList();
        mruScrollPane = new JScrollPane(mruList);
        mruPanel.add(mruScrollPane, BorderLayout.CENTER);

        setToolTipText(openButton, ResourceKey.WELCOME_DIALOG_OPEN_TOOLTIP);
        openButton.setEnabled(false);
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openButton_actionPerformed(ae);
            }
        });
        openButtons.add(openButton);

        openPanel.add(mruPanel, BorderLayout.CENTER);
        openPanel.add(openButtons, BorderLayout.SOUTH);

        /**
         * Build help panel
         */

        helpSubPanel.add(tutorialButton);
        tutorialButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                tutorialButton_actionPerformed(ae);
            }
        });

        helpSubPanel.add(faqButton);
        faqButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                faqButton_actionPerformed(ae);
            }
        });

        helpSubPanel.add(usersGuideButton);
        usersGuideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                usersGuideButton_actionPerformed(ae);
            }
        });

        helpSubPanel.add(topicsButton);
        topicsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                topicsButton_actionPerformed(ae);
            }
        });

        hspHolder.add(helpSubPanel);
        helpPanel.add(hspHolder, BorderLayout.CENTER);

        /**
         * Build main dialog
         */

        centerPanel.add(openPanel, BorderLayout.CENTER);
        centerPanel.add(helpPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeButton_actionPerformed(e);
            }
        });
        bottomPanel.add(closeButton);

        this.getContentPane().setLayout(new BorderLayout(0, 10));
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(ResourceKey key) {
        return new JButton(LocalizedText.getText(key));
    }

    private JButton createButton(ResourceKey key, Icon icon) {
        return new JButton(LocalizedText.getText(key), icon);
    }

    private TitledBorder createBorder(ResourceKey key) {
        String text = LocalizedText.getText(key);
        return new TitledBorder(BorderFactory.createEtchedBorder(), text);
    }

    private void setToolTipText(AbstractButton button, ResourceKey key) {
        String text = LocalizedText.getText(key);
        button.setToolTipText(text);
    }

    private void initList() {
        DefaultListModel model = new DefaultListModel();

        // Populate the list's model with data.
        for (int i = 0; i < projectURIList.size(); i++) {
            URI uri = (URI) projectURIList.get(i);
            String projectName = URIUtilities.getBaseName(uri);
            model.addElement(projectName);
        }

        mruList = new ProjectList(model);
        mruList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        mruList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                if (!mruList.isSelectionEmpty()) {
                    openButton.setEnabled(true);
                }
            }
        });

        mruList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = mruList.locationToIndex(e.getPoint());
                    doOpenProject(index);
                }
            }
        });
    }

    private void doOpenProject(int index) {
        if (index >= 0) {
            URI uri = (URI) projectURIList.get(index);
            WaitCursor cursor = new WaitCursor(this.getRootPane());
            setVisible(false);
            ProjectManager.getProjectManager().loadProject(uri);
            ApplicationProperties.addProjectToMRUList(uri);
            cursor.hide();
        }
    }

    public void newButton_actionPerformed(ActionEvent ae) {
        boolean succeeded = ProjectManager.getProjectManager().createNewProjectRequest();
        if (succeeded) {
            setVisible(false);
        }
    }

    public void openButton_actionPerformed(ActionEvent ae) {
        int index = mruList.getSelectedIndex();
        doOpenProject(index);
    }

    public void openOtherButton_actionPerformed(ActionEvent ae) {
        boolean opened = ProjectManager.getProjectManager().openProjectRequest(this);
        if (opened) {
            setVisible(false);
        }
    }

    public void faqButton_actionPerformed(ActionEvent ae) {
        SystemUtilities.showHTML(ApplicationProperties.getFAQURLString());
    }

    public void topicsButton_actionPerformed(ActionEvent ae) {
        SystemUtilities.showHTML(ApplicationProperties.getAllHelpURLString());
    }

    public void tutorialButton_actionPerformed(ActionEvent ae) {
        SystemUtilities.showHTML(ApplicationProperties.getGettingStartedURLString());
    }

    public void usersGuideButton_actionPerformed(ActionEvent ae) {
        SystemUtilities.showHTML(ApplicationProperties.getUsersGuideURLString());
    }

    public void closeButton_actionPerformed(ActionEvent e) {
        setVisible(false);
    }
}
