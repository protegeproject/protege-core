package edu.stanford.smi.protege.widget;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.DigestAndSalt;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.ModalDialog.CloseCallback;

public class DigestedPasswordWidget extends TextFieldWidget {

	private static final long serialVersionUID = 7208216346180195394L;

	public static final String SALT_SLOT_NAME = MetaProjectImpl.SlotEnum.salt.name();

	@Override
	public JComponent createCenterComponent(JTextComponent textComponent) {
		JComponent comp = new JPanel();
		comp.setLayout(new BoxLayout(comp, BoxLayout.LINE_AXIS));
		textComponent.setEditable(false);
		textComponent.setEnabled(false);
		comp.add(textComponent);
		comp.add(Box.createHorizontalStrut(10));
		JButton editButton = new JButton(createEditPasswordAction());
		editButton.setText("Change password");
		comp.add(editButton);
		return comp;
	}

	public void initialize() {
		super.initialize(false, 4, 1);
	}

	protected Action createEditPasswordAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 7043445264872043240L;

            public void actionPerformed(ActionEvent e) {
				final PasswordPanel passwordPanel = new PasswordPanel();
				int opt = ModalDialog.showDialog(ProjectManager.getProjectManager().getCurrentProjectView(), passwordPanel, 
						   "Change password", ModalDialog.MODE_OK_CANCEL, 
						      new CloseCallback() {
					             public boolean canClose(int result) {					        	 
					        	    boolean valid = passwordPanel.validatePasswords();
					        	    if (!valid) {
					        		    ModalDialog.showMessageDialog(passwordPanel, "Passwords do not match. Please enter them again.", "Error");
					        		    passwordPanel.clear();
					        	    }
					        	    return valid;
					            }
						     });
				if (opt == ModalDialog.OPTION_OK) {
					DigestAndSalt ds = generateDigestAndSalt(passwordPanel.getPassword());
					setDigestAndSalt(ds);
				}
			}
		};
	}


	protected DigestAndSalt generateDigestAndSalt(String password) {
		return StringUtilities.makeDigest(password);
	}
	
	protected void setDigestAndSalt(DigestAndSalt ds) {
		setValues(CollectionUtilities.createCollection(ds.getDigest()));
		Slot salt = getKnowledgeBase().getSlot(SALT_SLOT_NAME);
		if (salt == null) {
			Log.getLogger().severe("Could not find salt slot associated to class : " + getCls() + " and instance: " + getInstance());
		} else {
			getInstance().setOwnSlotValue(salt, ds.getSalt());
			valueChanged();
		}
	}
	
	
	public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
		boolean isSuitable = TextComponentWidget.isSuitable(cls, slot, facet);
		if (cls == null) { return false; }		
		Slot saltSlot = cls.getKnowledgeBase().getSlot(SALT_SLOT_NAME);		
		isSuitable = isSuitable && saltSlot != null;		
		isSuitable = isSuitable && 	cls.hasTemplateSlot(saltSlot) &&  
						!cls.getTemplateSlotAllowsMultipleValues(saltSlot) &&
						saltSlot.getValueType() == ValueType.STRING;		
		return isSuitable;
	}

	private class PasswordPanel extends JPanel {
		private static final long serialVersionUID = 3329310294772318595L;
        private JTextField pass1; 
		private JTextField pass2;

		public PasswordPanel() {
			setLayout(new BoxLayout(PasswordPanel.this, BoxLayout.PAGE_AXIS));
			pass1 = new JPasswordField(20);
			pass2 = new JPasswordField(20);
			add(new LabeledComponent("New password", pass1));
			add(new LabeledComponent("Retype password", pass2));	
		}
		
		public boolean validatePasswords() {
			return pass1.getText().equals(pass2.getText());
		}
		
		public void clear() {
			pass1.setText("");
			pass2.setText("");
		}
		
		public String getPassword() {
			return pass1.getText();
		}
	}

}
