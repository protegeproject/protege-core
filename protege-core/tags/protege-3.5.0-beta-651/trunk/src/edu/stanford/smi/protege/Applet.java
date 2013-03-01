package edu.stanford.smi.protege;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Class to allow Protege to run as an applet. This is used mostly for demos.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Applet extends JApplet {

    private static final long serialVersionUID = 7286186605066367207L;

    private String getProjectName() {
        return getParameter("project_name");
    }

    private URL getProjectURL() {
        URL url = null;
        try {
            url = new URL(getDocumentBase(), getProjectName());
        } catch (MalformedURLException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return url;
    }

    public void init() {
        try {
            SystemUtilities.initGraphics();
            SystemUtilities.setApplet(true);
            URL projectURL = getProjectURL();
            setup(projectURL);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
            SystemUtilities.pause();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
        Applet applet = new Applet();
        frame.getContentPane().add(applet);
        applet.setup(getURL(args));
        frame.pack();
        frame.setVisible(true);
    }

    private static URL getURL(String[] args) {
        URL url = null;
        try {
            url = new URL(args[0]);
        } catch (MalformedURLException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return url;
    }

    private void setup(final URL projectURL) {
        JButton button = new JButton(Icons.getLogoBannerIcon());
        button.setFocusPainted(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String[] args;
                if (projectURL == null) {
                    args = new String[0];
                } else {
                    args = new String[] { projectURL.toString() };
                }
                Application.main(args);
            }
        });
        getContentPane().add(button);
    }
}