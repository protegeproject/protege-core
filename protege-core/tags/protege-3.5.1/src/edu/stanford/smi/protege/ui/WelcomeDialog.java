package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.*;

/**
 * Startup dialog that is displayed when a user starts Protege (without
 * double clicking on a project file).  The dialog is displayed on top of the
 * main window and gives users the option to create a new project, open
 * a recently used project, browse for an existing file that is not in the
 * recently used list, or launch one of several help topics.
 *
 * @author Jennifer Vendetti
 */
public class WelcomeDialog extends JDialog {
    private static final long serialVersionUID = 3986911949227734433L;
    JButton cancelButton;
    JButton faqButton;
    JButton newButton;
    JButton openOtherButton;
    JButton openRecentButton;
    JButton topicsButton;
    JButton tutorialButton;
    JButton usersGuideButton;
    JPanel helpButtonsPanel;
    JPanel projectsPanel;
    JLabel protegeIconLabel;
    List projectURIs;
    ProjectList projectList;

    // Extended the JList class in order to have tool tips for each
    // individual list item.
    private class ProjectList extends JList {

        private static final long serialVersionUID = 8193202451714386857L;

        ProjectList(DefaultListModel model) {
            this.setModel(model);
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        public String getToolTipText(MouseEvent event) {
            String toolTip = null;
            int index = locationToIndex(event.getPoint());
            if (index >= 0) {
                toolTip = projectURIs.get(index).toString();
            }
            return toolTip;
        }
    }

    public WelcomeDialog(java.awt.Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setSize(500, 300);
            projectURIs = new ArrayList(ApplicationProperties.getMRUProjectList());
            initializeUI();
        } catch (Exception ex) {
            Log.getLogger().severe(Log.toString(ex));
        }
    }

    private void initializeUI() {
		// Build sub panels
        buildProjectsPanel();
        buildHelpPanel();

        // Build main dialog
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Protege logo
        protegeIconLabel = new JLabel(Icons.getLogo());
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(7, 0, 0, 0);
        c.anchor = GridBagConstraints.CENTER;
        contentPane.add(protegeIconLabel, c);

        // Add Project & File Access panel
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(7, 2, 0, 0);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 1.0;
        c.weighty = 1.0;
        contentPane.add(projectsPanel, c);

        // Add Help panel
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        contentPane.add(helpButtonsPanel, c);

        // Cancel button
        cancelButton = createButton(ResourceKey.CANCEL_BUTTON_LABEL,
        							Icons.getCancelIcon());
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeButton_actionPerformed(e);
            }
        });
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(11, 0, 7, 0);
        c.anchor = GridBagConstraints.CENTER;
        contentPane.add(cancelButton, c);

        getRootPane().setDefaultButton(cancelButton);
    }

    private void buildProjectsPanel() {
        projectsPanel = new JPanel(new GridBagLayout());
        projectsPanel.setBorder(createBorder(ResourceKey.WELCOME_DIALOG_OPEN_RECENT_PROJECT_TITLE));
        GridBagConstraints c = new GridBagConstraints();

		// List of recently accessed project & files
        initializeList();
        JScrollPane projectScrollPane = new JScrollPane(projectList);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 4;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 2, 5, 2); // top, left, bottom, right
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 1.0;
        c.weighty = 1.0;
        projectsPanel.add(projectScrollPane, c);

        // Open Recent button
        openRecentButton = createButton(ResourceKey.WELCOME_DIALOG_OPEN);
        openRecentButton.setMnemonic(LocalizedText.getMnemonic(ResourceKey.WELCOME_DIALOG_OPEN));
        setToolTipText(openRecentButton, ResourceKey.WELCOME_DIALOG_OPEN_TOOLTIP);
        openRecentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openButton_actionPerformed(ae);
            }
        });
        if ((projectList.getModel().getSize()) == 0) {
            openRecentButton.setEnabled(false);
        }
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
    	c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 0);
        c.weightx = 0.0;
        c.weighty = 0.0;
        projectsPanel.add(openRecentButton, c);

        // Open Other button
        openOtherButton = createButton(ResourceKey.WELCOME_DIALOG_OPEN_OTHER, Icons.getOpenProjectIcon());
        openOtherButton.setMnemonic(LocalizedText.getMnemonic(ResourceKey.WELCOME_DIALOG_OPEN_OTHER));
        setToolTipText(openOtherButton, ResourceKey.WELCOME_DIALOG_OPEN_OTHER_TOOLTIP);
        openOtherButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openOtherButton_actionPerformed(ae);
            }
        });
        c.gridx = 1;
        c.gridy = 1;
        projectsPanel.add(openOtherButton, c);

        // Filler
        JLabel label = new JLabel("");
        c.gridx = 1;
        c.gridy = 2;
        c.weighty = 1.0;
        projectsPanel.add(label, c);

        // New Project button
        newButton = createButton(ResourceKey.WELCOME_DIALOG_NEW, Icons.getNewProjectIcon());
        newButton.setMnemonic(LocalizedText.getMnemonic(ResourceKey.WELCOME_DIALOG_NEW));
        setToolTipText(newButton, ResourceKey.WELCOME_DIALOG_NEW_TOOLTIP);
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newButton_actionPerformed(ae);
            }
        });
        c.gridx = 1;
        c.gridy = 3;
        c.weighty = 0.0;
        projectsPanel.add(newButton, c);
    }

    private void buildHelpPanel() {
        helpButtonsPanel = new JPanel();
        helpButtonsPanel.setLayout(new BoxLayout(helpButtonsPanel, BoxLayout.PAGE_AXIS));
        helpButtonsPanel.setBorder(createBorder(ResourceKey.WELCOME_DIALOG_HELP_TITLE));
        Dimension verticalSpace = new Dimension(0, 5);

        // Add "Getting Started" button
        tutorialButton = createButton(ResourceKey.WELCOME_DIALOG_GETTING_STARTED);
        tutorialButton.setMnemonic(LocalizedText.getMnemonic(ResourceKey.WELCOME_DIALOG_GETTING_STARTED));
        tutorialButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                tutorialButton_actionPerformed(ae);
            }
        });
        helpButtonsPanel.add(tutorialButton);
        helpButtonsPanel.add(Box.createRigidArea(verticalSpace));

        // Add "FAQ" button
        faqButton = createButton(ResourceKey.WELCOME_DIALOG_FAQ);
        faqButton.setMnemonic(LocalizedText.getMnemonic(ResourceKey.WELCOME_DIALOG_FAQ));
        faqButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                faqButton_actionPerformed(ae);
            }
        });
        helpButtonsPanel.add(faqButton);
        helpButtonsPanel.add(Box.createRigidArea(verticalSpace));

        // Add "User's Guide" button
        usersGuideButton = createButton(ResourceKey.WELCOME_DIALOG_USERS_GUIDE);
        usersGuideButton.setMnemonic(LocalizedText.getMnemonic(ResourceKey.WELCOME_DIALOG_USERS_GUIDE));
        usersGuideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                usersGuideButton_actionPerformed(ae);
            }
        });
        helpButtonsPanel.add(usersGuideButton);
        helpButtonsPanel.add(Box.createRigidArea(verticalSpace));

        // Add "All Topics" button
        topicsButton = createButton(ResourceKey.WELCOME_DIALOG_ALL_TOPICS);
        topicsButton.setMnemonic(LocalizedText.getMnemonic(ResourceKey.WELCOME_DIALOG_ALL_TOPICS));
        topicsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                topicsButton_actionPerformed(ae);
            }
        });
        helpButtonsPanel.add(topicsButton);
        equalizeHelpButtons();
    }

    private static JButton createButton(ResourceKey key) {
        return new JButton(LocalizedText.getText(key));
    }

    private static JButton createButton(ResourceKey key, Icon icon) {
        return new JButton(LocalizedText.getText(key), icon);
    }

    private static TitledBorder createBorder(ResourceKey key) {
        String text = LocalizedText.getText(key);
        return new TitledBorder(BorderFactory.createEtchedBorder(), text);
    }

    private static void setToolTipText(AbstractButton button, ResourceKey key) {
        String text = LocalizedText.getText(key);
        button.setToolTipText(text);
    }

    private void initializeList() {
        DefaultListModel model = new DefaultListModel();

        // Populate the list's model with data.
        for (int i = 0; i < projectURIs.size(); i++) {
            URI uri = (URI) projectURIs.get(i);
            String projectName = URIUtilities.getName(uri);
            model.addElement(projectName);
        }

        projectList = new ProjectList(model);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (projectList.getModel().getSize() != 0) {
            projectList.setSelectedIndex(0);
        }

        projectList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
				/*if (!projectList.isSelectionEmpty()) {
                    openRecentButton.setEnabled(true);
                }*/
            }
        });

        projectList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = projectList.locationToIndex(e.getPoint());
                    doOpenProject(index);
                }
            }
        });
    }

    private void doOpenProject(int index) {
        if (index >= 0) {
            URI uri = (URI) projectURIs.get(index);
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
        int index = projectList.getSelectedIndex();
        doOpenProject(index);
    }

    public void openOtherButton_actionPerformed(ActionEvent ae) {
        boolean opened = ProjectManager.getProjectManager().openProjectRequest(this);
        if (opened) {
            setVisible(false);
        }
    }

    private static void faqButton_actionPerformed(ActionEvent ae) {
        SystemUtilities.showHTML(ApplicationProperties.getFAQURLString());
    }

    private static void topicsButton_actionPerformed(ActionEvent ae) {
        SystemUtilities.showHTML(ApplicationProperties.getAllHelpURLString());
    }

    private static void tutorialButton_actionPerformed(ActionEvent ae) {
        SystemUtilities.showHTML(ApplicationProperties.getGettingStartedURLString());
    }

    private static void usersGuideButton_actionPerformed(ActionEvent ae) {
        SystemUtilities.showHTML(ApplicationProperties.getUsersGuideURLString());
    }

    public void closeButton_actionPerformed(ActionEvent e) {
        setVisible(false);
    }

    private void equalizeHelpButtons() {
		String[] labels = new String[] { faqButton.getText(),
        								 topicsButton.getText(),
                                         tutorialButton.getText(),
										 usersGuideButton.getText() };

		// Get the largest width and height
		Dimension maxSize= new Dimension(0,0);
		Rectangle2D textBounds = null;
        FontMetrics metrics = faqButton.getFontMetrics(faqButton.getFont());
    	Graphics g = getGraphics();
    	for (int i=0; i<labels.length; ++i) {
			textBounds = metrics.getStringBounds(labels[i], g);
        	maxSize.width = Math.max(maxSize.width, (int)textBounds.getWidth());
			maxSize.height = Math.max(maxSize.height, (int)textBounds.getHeight());
    	}

        Insets insets = faqButton.getBorder().getBorderInsets(faqButton);
        maxSize.width += insets.left + insets.right;
        maxSize.height += insets.top + insets.bottom;

        // Reset preferred and maximum sizes since BoxLayout takes both
    	// into account
        faqButton.setPreferredSize((Dimension)maxSize.clone());
        topicsButton.setPreferredSize((Dimension)maxSize.clone());
        tutorialButton.setPreferredSize((Dimension)maxSize.clone());
        usersGuideButton.setPreferredSize((Dimension)maxSize.clone());

        faqButton.setMaximumSize((Dimension)maxSize.clone());
        topicsButton.setMaximumSize((Dimension)maxSize.clone());
        tutorialButton.setMaximumSize((Dimension)maxSize.clone());
        usersGuideButton.setMaximumSize((Dimension)maxSize.clone());
    }
}
