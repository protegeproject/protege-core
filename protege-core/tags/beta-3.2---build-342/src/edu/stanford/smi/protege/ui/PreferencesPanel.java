package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PreferencesPanel extends Box implements Validatable {
    private JTextField userNameField;
    private JRadioButton useLoginUserNameButton;
    private JRadioButton useSpecifiedUserNameButton;

    private JRadioButton protegeDefaultLocaleButton;
    private JRadioButton systemDefaultLocaleButton;
    private JRadioButton otherLocaleButton;
    private JComboBox localeComboBox;

    private JCheckBox showWelcomeDialogCheckBox;
    private JCheckBox prettyPrintLabelsCheckBox;

    public PreferencesPanel() {
        super(BoxLayout.Y_AXIS);
        add(createShowWelcomeDialogCheckBox());
        add(createPrettyPrintLabelsCheckBox());
        add(createUsernamePanel());
        add(createLocalePanel());
    }

    private JComponent createUsernamePanel() {
        String applicationUserName = ApplicationProperties.getUserName();
        String systemUserName = SystemUtilities.getUserName();

        userNameField = ComponentFactory.createTextField();
        userNameField.setColumns(15);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder("User Name"));
        useLoginUserNameButton = new JRadioButton("Use Login Name: " + systemUserName);
        Box useSpecifiedPanel = Box.createHorizontalBox();
        useSpecifiedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Action action = new StandardAction("Use Specified Name: ") {
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
        useSpecifiedPanel.add(useSpecifiedUserNameButton, BorderLayout.WEST);
        useSpecifiedPanel.add(userNameField, BorderLayout.CENTER);
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

    public void saveContents() {
        ApplicationProperties.setUserName(userNameField.getText());
        ApplicationProperties.setWelcomeDialogShow(showWelcomeDialogCheckBox.isSelected());
        ApplicationProperties.setPrettyPrintSlotWidgetLabels(prettyPrintLabelsCheckBox.isSelected());
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
