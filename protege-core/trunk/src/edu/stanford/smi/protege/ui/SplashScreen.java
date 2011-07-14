package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * A window for the application startup splash screen. This just displays the nerd icon and the version number.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SplashScreen extends JWindow {
    private static final long serialVersionUID = 4752946291493511390L;
    private WaitCursor _waitCursor;

    public SplashScreen() {
        _waitCursor = new WaitCursor(this);
        String labelText = "Loading " + Text.getProgramNameAndVersion();
        // setTitle("Loading " + Text.getProgramName());
        // setIconImage(Icons.getNerd16x16Image());

        JLabel label = ComponentFactory.createLabel(labelText, SwingConstants.CENTER);

        JLabel icon = ComponentFactory.createLabel(Icons.getLogoSplashIcon());

        getContentPane().setLayout(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(icon, BorderLayout.CENTER);
        panel.add(label, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        getContentPane().add(panel);
        pack();
        ComponentUtilities.center(this);
        setVisible(true);
        Toolkit.getDefaultToolkit().sync();
    }

    public void dispose() {
        setVisible(false);

        super.dispose();
        _waitCursor.hide();

        getContentPane().removeAll();
        getContentPane().setLayout(null);
    }

}