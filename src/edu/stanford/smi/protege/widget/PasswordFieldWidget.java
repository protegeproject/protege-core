package edu.stanford.smi.protege.widget;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PasswordFieldWidget extends TextFieldWidget {

    @Override
    public JTextField createTextField() {
        return new JPasswordField();
    }
}
