package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Startup dialog that's displayed when a user starts Protege-2000 (without
 * double clicking on a project file).  The dialog is displayed on top of the
 * main window and gives the user the option to create a new project, open
 * a recently used project, or launch one of several help topics.
 *
 * @author Jennifer Vendetti
 */
public class WelcomeDialog extends JDialog {

    // Top level panel.
    JPanel panel = new JPanel(new BorderLayout(10, 0));

    // New Project panel and sub-panels.
    JPanel newPanel = new JPanel(new BorderLayout());
    JPanel ptpHolder = new JPanel(new FlowLayout());
    JPanel projectTypePanel;
    JPanel formatButtons = new JPanel(new FlowLayout());
    JPanel newButtonPanel = new JPanel(new GridLayout(1, 2));

    // Open Project panel and sub-panels.
    JPanel openPanel = new JPanel(new BorderLayout());
    JPanel mruPanel = new JPanel(new BorderLayout());
    JPanel openButtons = new JPanel(new FlowLayout());
    JPanel openButtonPanel = new JPanel(new GridLayout(1, 2));

    // Help Resources panel and sub-panels.
    JPanel helpPanel = new JPanel(new BorderLayout());
    JPanel hspHolder = new JPanel(new FlowLayout());
    JPanel helpSubPanel = new JPanel(new GridLayout(4, 1, 0, 2));
    JPanel iconPanel = new JPanel(new GridLayout(1, 1));

    TitledBorder titledBorder;
    TitledBorder titledBorder1;
    TitledBorder titledBorder2;

    JButton newButton = createButton(ResourceKey.WELCOME_DIALOG_NEW);
    JButton importButton = createButton(ResourceKey.WELCOME_DIALOG_BUILD);
    JButton openOtherButton = createButton(ResourceKey.WELCOME_DIALOG_OPEN_OTHER);
    JButton openButton = createButton(ResourceKey.WELCOME_DIALOG_OPEN);
    JButton topicsButton = createButton(ResourceKey.WELCOME_DIALOG_ALL_TOPICS);
    JButton tutorialButton = createButton(ResourceKey.WELCOME_DIALOG_GETTING_STARTED);
    JButton usersGuideButton = createButton(ResourceKey.WELCOME_DIALOG_USERS_GUIDE);
    JButton faqButton = createButton(ResourceKey.WELCOME_DIALOG_FAQ);

    ButtonGroup group = new ButtonGroup();
    ProjectList mruList;
    JScrollPane mruScrollPane;
    JLabel iconLabel = ComponentFactory.createLabel();
    JRadioButton[] rbArray;
    List factoryList;
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
        titledBorder = createBorder(ResourceKey.WELCOME_DIALOG_PROJECT_FORMAT_TITLE);
        titledBorder1 = createBorder(ResourceKey.WELCOME_DIALOG_OPEN_RECENT_PROJECT_TITLE);
        titledBorder2 = createBorder(ResourceKey.WELCOME_DIALOG_HELP_TITLE);
        ptpHolder.setBorder(titledBorder);
        mruPanel.setBorder(titledBorder1);
        hspHolder.setBorder(titledBorder2);

        // Initialize JLabel that holds the Protege-2000 icon.
        iconLabel.setIcon(Icons.getLogo());
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);

        /* Build New Project panel ********************************************/
        ptpHolder.add(getProjectTypePanel());
        newPanel.add(ptpHolder, BorderLayout.CENTER);

        newButtonPanel.add(newButton);
        setToolTipText(newButton, ResourceKey.WELCOME_DIALOG_NEW_TOOLTIP);
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newButton_actionPerformed(ae);
            }
        });
        newButtonPanel.add(importButton);
        setToolTipText(importButton, ResourceKey.WELCOME_DIALOG_BUILD_TOOLTIP);
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                importButton_actionPerformed(ae);
            }
        });
        formatButtons.add(newButtonPanel);
        newPanel.add(formatButtons, BorderLayout.SOUTH);

        /* Build Open Project panel *******************************************/
        initList();
        mruScrollPane = new JScrollPane(mruList);
        mruPanel.add(mruScrollPane, BorderLayout.CENTER);

        openPanel.add(mruPanel, BorderLayout.CENTER);

        setToolTipText(openButton, ResourceKey.WELCOME_DIALOG_OPEN_TOOLTIP);
        openButton.setEnabled(false);
        openButtonPanel.add(openButton);
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

        iconPanel.add(iconLabel);
        helpPanel.add(iconPanel, BorderLayout.SOUTH);

        /* Build main dialog **************************************************/
        panel.add(newPanel, BorderLayout.WEST);
        panel.add(openPanel, BorderLayout.CENTER);
        panel.add(helpPanel, BorderLayout.EAST);
        this.getContentPane().add(panel);
    }

    private JPanel getProjectTypePanel() {
        // Build the top section of the "New Project" panel:

        // Get a collection of the available project types.
        // a.k.a. back-ends
        // a.k.a. knowledge-base factories
        factoryList = new ArrayList(PluginUtilities.getAvailableFactories());

        projectTypePanel = new JPanel(new GridLayout(factoryList.size(), 1, 0, 2));

        // Create a radio button for each new project type and assign all the
        // buttons to a radio group.
        group = new ButtonGroup();
        rbArray = new JRadioButton[factoryList.size()];
        for (int i = 0; i < factoryList.size(); i++) {
            KnowledgeBaseFactory factory = (KnowledgeBaseFactory) factoryList.get(i);
            JRadioButton rb = new JRadioButton(factory.getDescription());
            rbArray[i] = rb;
            rbArray[0].setSelected(true);
            group.add(rb);
            projectTypePanel.add(rb);
        }
        return projectTypePanel;
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
        WaitCursor cursor = new WaitCursor(this.getRootPane());
        // Find out which radio button is selected in the "New Project"
        // panel and open the appropriate project type.
        for (int i = 0; i < rbArray.length; i++) {
            if (rbArray[i].isSelected()) {
                KnowledgeBaseFactory factory = (KnowledgeBaseFactory) factoryList.get(i);
                ProjectManager.getProjectManager().loadProject(null, factory);
            }
        }
        cursor.hide();
        this.setVisible(false);
    }

    public void importButton_actionPerformed(ActionEvent ae) {
        // Find out which radio button is selected in the "New Project"
        // panel and import the appropriate project type.
        for (int i = 0; i < rbArray.length; i++) {
            if (rbArray[i].isSelected()) {
                KnowledgeBaseFactory factory = (KnowledgeBaseFactory) factoryList.get(i);
                boolean succeeded = ProjectManager.getProjectManager().buildProject(factory);
                if (succeeded) {
                    this.setVisible(false);
                }
            }
        }
    }

    public void openButton_actionPerformed(ActionEvent ae) {
        int index = mruList.getSelectedIndex();
        doOpenProject(index);
    }

    public void openOtherButton_actionPerformed(ActionEvent ae) {
        // Don't want to use this line of code because it opens
        // the JFileChooser dialog with the application's main frame as the
        // parent.  If the main frame is the parent and the user clicks the
        // Cancel button, focus goes to the main frame instead of back to
        // the welcome dialog.
        //ProjectManager.getProjectManager().openProjectRequest();

        String title = LocalizedText.getText(ResourceKey.OPEN_PROJECT_DIALOG_TITLE);
        JFileChooser chooser = ComponentFactory.createFileChooser(title, ".pprj");
        int rval = chooser.showOpenDialog(this);
        if (rval == JFileChooser.APPROVE_OPTION) {
            URI uri = chooser.getSelectedFile().toURI();
            WaitCursor cursor = new WaitCursor(this.getRootPane());
            this.setVisible(false);
            ProjectManager.getProjectManager().loadProject(uri);
            ApplicationProperties.addProjectToMRUList(uri);
            cursor.hide();
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
