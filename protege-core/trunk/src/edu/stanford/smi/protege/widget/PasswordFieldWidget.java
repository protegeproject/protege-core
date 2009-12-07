package edu.stanford.smi.protege.widget;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PasswordFieldWidget extends TextFieldWidget {
   
	private static final long serialVersionUID = -7876765205711389282L;

	@Override
    public JTextField createTextField() {
        return new JPasswordField();
    }
}
