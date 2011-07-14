package edu.stanford.smi.protege.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.StandardAction;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.Validatable;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class GeneralPreferencesPanel extends Box implements Validatable{

    private static final long serialVersionUID = 5026044507780193841L;
    private JTextField userNameField;
    private JRadioButton useLoginUserNameButton;
    private JRadioButton useSpecifiedUserNameButton;
    
    private JRadioButton protegeDefaultLocaleButton;
    private JRadioButton systemDefaultLocaleButton;
    private JRadioButton otherLocaleButton;
    private JComboBox localeComboBox;

    private JCheckBox showWelcomeDialogCheckBox;
    private JCheckBox prettyPrintLabelsCheckBox;
    private JCheckBox sortClassTreeCheckBox;
    private JCheckBox sortSlotTreeCheckBox;

    public GeneralPreferencesPanel() {
        super(BoxLayout.Y_AXIS);
        add(createShowWelcomeDialogCheckBox());
        add(createPrettyPrintLabelsCheckBox());
        add(createSortClassTreeCheckBox());
        add(createSortSlotTreeCheckBox());
        add(createUsernamePanel());
        add(createLocalePanel());
    }

	private JComponent createUsernamePanel() {
        String applicationUserName = ApplicationProperties.getUserName();
        String systemUserName = SystemUtilities.getUserName();   
        
        Box box = Box.createVerticalBox();
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(BorderFactory.createTitledBorder("User Name"));
                        
        Box useSpecifiedPanel = Box.createHorizontalBox();
        useSpecifiedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        useSpecifiedPanel.setBackground(Color.RED);
        useSpecifiedPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
                        
        userNameField = ComponentFactory.createTextField();
        userNameField.setColumns(15);
                
        Action action = new StandardAction("Use Specified Name: ") {
            private static final long serialVersionUID = -3953329470167238831L;

            public void actionPerformed(ActionEvent event) {
                if (useSpecifiedUserNameButton.isSelected()) {
                    userNameField.setEnabled(true);
                } else {
                    userNameField.setText(null);
                    userNameField.setEnabled(false);
                }
            }
        };
        
        useSpecifiedUserNameButton = new JRadioButton(action);
        
        useSpecifiedPanel.add(useSpecifiedUserNameButton);
        useSpecifiedPanel.add(userNameField);
        
        useLoginUserNameButton = new JRadioButton("Use Login Name: " + systemUserName);
        
        ButtonGroup group = new ButtonGroup();
        group.add(useSpecifiedUserNameButton);
        group.add(useLoginUserNameButton);
        
        if (applicationUserName == null || applicationUserName.length() == 0
                || systemUserName.equals(applicationUserName)) {
            useLoginUserNameButton.setSelected(true);
        } else {
            useSpecifiedUserNameButton.setSelected(true);
            userNameField.setText(applicationUserName);
        }
        
        box.add(useLoginUserNameButton);
        box.add(useSpecifiedPanel);        
        
        return box;
    }

    private JComponent createLocalePanel() {
        Box box = Box.createVerticalBox();
        Locale defaultLocale = ApplicationProperties.getLocale();
        Locale systemLocale = SystemUtilities.getSystemLocale();
        Locale protegeDefaultLocale = SystemUtilities.getProtegeSystemDefaultLocale();
        box.setBorder(BorderFactory.createTitledBorder("Locale"));
        protegeDefaultLocaleButton = new JRadioButton(Text.getProgramName() + " Default:   "
                + protegeDefaultLocale.getDisplayName());
        systemDefaultLocaleButton = new JRadioButton("System Default:   " + systemLocale.getDisplayName());
        otherLocaleButton = new JRadioButton("Other:   ");
        localeComboBox = ComponentFactory.createComboBox();
        localeComboBox.setModel(new DefaultComboBoxModel(getSortedLocales()));
        localeComboBox.setRenderer(new LocaleRenderer());
        localeComboBox.setEnabled(false);
        if (defaultLocale.equals(protegeDefaultLocale)) {
            protegeDefaultLocaleButton.setSelected(true);
        } else if (defaultLocale.equals(systemLocale)) {
            systemDefaultLocaleButton.setSelected(true);
        } else {
            otherLocaleButton.setSelected(true);
            localeComboBox.setSelectedItem(defaultLocale);
            localeComboBox.setEnabled(true);
        }
        if (systemLocale.equals(protegeDefaultLocale)) {
            systemDefaultLocaleButton.setEnabled(false);
        }

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                localeComboBox.setEnabled(otherLocaleButton.isSelected());
            }
        };
        protegeDefaultLocaleButton.addActionListener(actionListener);
        systemDefaultLocaleButton.addActionListener(actionListener);
        otherLocaleButton.addActionListener(actionListener);

        ButtonGroup group = new ButtonGroup();
        group.add(protegeDefaultLocaleButton);
        group.add(systemDefaultLocaleButton);
        group.add(otherLocaleButton);

        Box otherLocalePanel = Box.createHorizontalBox();
        otherLocalePanel.add(otherLocaleButton);
        otherLocalePanel.add(localeComboBox);
        otherLocalePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(protegeDefaultLocaleButton);
        box.add(systemDefaultLocaleButton);
        box.add(otherLocalePanel);
        box.setVisible(SystemUtilities.showAlphaFeatures());
        return box;
    }

    private static Locale[] getSortedLocales() {
        Locale[] locales = Locale.getAvailableLocales();
        Arrays.sort(locales, new LocaleComparator());
        return locales;
    }

    private JComponent createShowWelcomeDialogCheckBox() {
        showWelcomeDialogCheckBox = ComponentFactory.createCheckBox("Show Welcome Dialog on Start-up");
        showWelcomeDialogCheckBox.setSelected(ApplicationProperties.getWelcomeDialogShow());
        return showWelcomeDialogCheckBox;
    }

    private JComponent createPrettyPrintLabelsCheckBox() {
        prettyPrintLabelsCheckBox = ComponentFactory.createCheckBox("Capitalize Slot Widget Labels");
        prettyPrintLabelsCheckBox.setSelected(ApplicationProperties.getPrettyPrintSlotWidgetLabels());
        return prettyPrintLabelsCheckBox;
    }
   

    private Component createSortSlotTreeCheckBox() {
		sortSlotTreeCheckBox = ComponentFactory.createCheckBox("Sort slot tree (Slots Tab)");
		sortSlotTreeCheckBox.setSelected(ApplicationProperties.getSortSlotTreeOption());
		return sortSlotTreeCheckBox;    	
	}

	private Component createSortClassTreeCheckBox() {
		sortClassTreeCheckBox = ComponentFactory.createCheckBox("Sort class tree (Classes Tab)");
    	sortClassTreeCheckBox.setSelected(ApplicationProperties.getSortClassTreeOption());
    	return sortClassTreeCheckBox;	
	}    
    
    public void saveContents() {
        ApplicationProperties.setUserName(userNameField.getText());
        ApplicationProperties.setWelcomeDialogShow(showWelcomeDialogCheckBox.isSelected());
        ApplicationProperties.setPrettyPrintSlotWidgetLabels(prettyPrintLabelsCheckBox.isSelected());
        ApplicationProperties.setSortClassTreeOption(sortClassTreeCheckBox.isSelected());
        ApplicationProperties.setSortSlotTreeOption(sortSlotTreeCheckBox.isSelected());
        
        Locale locale;
        if (protegeDefaultLocaleButton.isSelected()) {
            locale = SystemUtilities.getProtegeSystemDefaultLocale();
        } else if (systemDefaultLocaleButton.isSelected()) {
            locale = SystemUtilities.getSystemLocale();
        } else {
            locale = (Locale) localeComboBox.getSelectedItem();
        }
        ApplicationProperties.setLocale(locale);
    }

    public boolean validateContents() {
        return true;
    }
	
}
