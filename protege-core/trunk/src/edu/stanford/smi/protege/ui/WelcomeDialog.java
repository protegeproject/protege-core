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

    // Top level panel.
    JPanel panel = new JPanel(new BorderLayout(5, 5));

    // Create New Project panel.
    JPanel newButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    //JPanel iconPanel = new JPanel(new GridLayout(1, 1));
    JPanel iconPanel = new JPanel(new FlowLayout());
    JPanel createNewPanel = new JPanel(new BorderLayout());

    // Open Project panel and sub-panels.
    JPanel openPanel = new JPanel(new BorderLayout());
    JPanel mruPanel = new JPanel(new BorderLayout());
    JPanel openButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel openButtonPanel = new JPanel(new GridLayout(1, 2, 3, 0));

    // Help Resources panel and sub-panels.
    JPanel helpPanel = new JPanel(new BorderLayout());
    JPanel hspHolder = new JPanel(new FlowLayout());
    JPanel helpSubPanel = new JPanel(new GridLayout(4, 1, 0, 2));

    TitledBorder titledBorder1;
    TitledBorder titledBorder2;

    JButton newButton = createButton(ResourceKey.WELCOME_DIALOG_NEW);
    JButton openOtherButton = createButton(ResourceKey.WELCOME_DIALOG_OPEN_OTHER);
    JButton openButton = createButton(ResourceKey.WELCOME_DIALOG_OPEN);
    JButton topicsButton = createButton(ResourceKey.WELCOME_DIALOG_ALL_TOPICS);
    JButton tutorialButton = createButton(ResourceKey.WELCOME_DIALOG_GETTING_STARTED);
    JButton usersGuideButton = createButton(ResourceKey.WELCOME_DIALOG_USERS_GUIDE);
    JButton faqButton = createButton(ResourceKey.WELCOME_DIALOG_FAQ);

    ButtonGroup group = new ButtonGroup();
    ProjectList mruList;
    JScrollPane mruScrollPane;
    //JLabel iconLabel = ComponentFactory.createLabel();
    JLabel iconLabel;
    JRadioButton[] rbArray;

    // List factoryList;
    List projectURIList = new ArrayList(ApplicationProperties.getMRUProjectList());

    private JButton createButton(ResourceKey key) {
        return new JButton(LocalizedText.getText(key));
    }

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private TitledBorder createBorder(ResourceKey key) {
        String text = LocalizedText.getText(key);
        return new TitledBorder(BorderFactory.createEtchedBorder(), text);
    }

    private void setToolTipText(AbstractButton button, ResourceKey key) {
        String text = LocalizedText.getText(key);
        button.setToolTipText(text);
    }

    void jbInit() throws Exception {
        titledBorder1 = createBorder(ResourceKey.WELCOME_DIALOG_OPEN_RECENT_PROJECT_TITLE);
        titledBorder2 = createBorder(ResourceKey.WELCOME_DIALOG_HELP_TITLE);
        mruPanel.setBorder(titledBorder1);
        hspHolder.setBorder(titledBorder2);

        /* Build New Project panel ********************************************/
        newButtonPanel.add(newButton);
        setToolTipText(newButton, ResourceKey.WELCOME_DIALOG_NEW_TOOLTIP);
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newButton_actionPerformed(ae);
            }
        });

        // Initialize JLabel that holds the Protege icon.
        iconLabel = new JLabel("   ", Icons.getLogo(), JLabel.LEFT);
        iconPanel.add(iconLabel);

        createNewPanel.add(newButtonPanel, BorderLayout.CENTER);
        createNewPanel.add(iconPanel, BorderLayout.EAST);

        /* Build Open Project panel *******************************************/

	// Set up the list of most recently used projects.
        initList();
        mruScrollPane = new JScrollPane(mruList);
        mruPanel.add(mruScrollPane, BorderLayout.CENTER);

	// Set up the "Open Selected Project" and "Open Other Project..."
        // buttons.
        openButtonPanel.add(openButton);
        setToolTipText(openButton, ResourceKey.WELCOME_DIALOG_OPEN_TOOLTIP);
        openButton.setEnabled(false);
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openButton_actionPerformed(ae);
            }
        });

        openButtonPanel.add(openOtherButton);
        setToolTipText(openOtherButton, ResourceKey.WELCOME_DIALOG_OPEN_OTHER_TOOLTIP);
        openOtherButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openOtherButton_actionPerformed(ae);
            }
        });

        openButtons.add(openButtonPanel);

        openPanel.add(mruPanel, BorderLayout.CENTER);
        openPanel.add(openButtons, BorderLayout.SOUTH);

        /* Build Help Resources panel *****************************************/
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
        openPanel.add(helpPanel, BorderLayout.EAST);

        /* Build main dialog **************************************************/
        panel.add(createNewPanel, BorderLayout.NORTH);
        panel.add(openPanel, BorderLayout.CENTER);
        this.getContentPane().add(panel);
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
            this.setVisible(false);
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
}
