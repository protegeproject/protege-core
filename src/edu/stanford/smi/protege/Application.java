package edu.stanford.smi.protege;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.logging.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Main class for the Protege application (as opposed to an applet).
 *
 * @author Ray Fergerson
 * @author Jennifer Vendetti
 */
public class Application {
    private static JFrame _mainFrame;
    private static SplashScreen _splashScreen;
    private static WelcomeDialog _welcome;

    private static void initialize() {
        try {
            SystemUtilities.initialize();
        } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "failed to initialize", e);
        } 
    }

    public static Component getMainWindow() {
        Component result;
        if (_splashScreen != null) {
            result = _splashScreen;
        } else {
            result = _mainFrame;
        }
        return result;
    }

    private static URI getProjectURI(String[] args) {
        URI uri = null;
        if (args.length > 0) {
            String projectString = args[0];
            if (!projectString.endsWith(".pprj")) {
                projectString += ".pprj";
            }
            uri = URIUtilities.createURI(projectString);
         }

        return uri;
    }

    private static void init(String[] args) throws Exception {
        parseOptions(args);

        // Construct the application's main frame.
        _mainFrame = ComponentFactory.createMainFrame();
        _mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                ProjectManager.getProjectManager().exitApplicationRequest();
            }
        });

        restoreMainframeRectangle();
        ProjectManager.getProjectManager().setRootPane(_mainFrame.getRootPane());

        // Find out if a filename was passed in on the command-line.

        URI projectURI = getProjectURI(args);

        if (projectURI != null) {
            // Load the project and show the main frame.
            ProjectManager.getProjectManager().loadProject(projectURI);
            showMainFrame();
        } else {
            showMainFrame();
            // Check to see if the user wants to see the welcome dialog.
            boolean b = ApplicationProperties.getWelcomeDialogShow();
            if (b == true) {
                // Load the main frame and show the welcome dialog.
                _welcome = new WelcomeDialog(_mainFrame, Text.getProgramName(), true);
                _welcome.setLocationRelativeTo(_mainFrame);
                _welcome.setVisible(true);
            }
        }
    }
    
    public static WelcomeDialog getWelcomeDialog() {
        return _welcome;
    }

    private static void restoreMainframeRectangle() {
        ApplicationProperties.restoreMainFrameProperties(_mainFrame);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle r = gc.getBounds();

        // make sure that the size is smaller than the current screen size
        Dimension frameSize = _mainFrame.getSize();
        frameSize.width = Math.min(frameSize.width, r.width);
        frameSize.height = Math.min(frameSize.height, r.height);
        _mainFrame.setSize(frameSize);

        // make sure that the position appears on the screen -- some "virtual" windowing systems mess this up.
        Point location = _mainFrame.getLocation();
        location.x = Math.max(r.x, location.x);
        location.y = Math.max(r.y, location.y);
        _mainFrame.setLocation(location);
    }
    
    public static void main(final String[] args) {
        // This bit of sleight of hand causes uncaught exceptions to get logged.
        // There is a better way to do this in JDK 1.5.
        ThreadGroup group = new ThreadGroup(Thread.currentThread().getThreadGroup(), "Safe Thread Group") {
            public void uncaughtException(Thread thread, Throwable throwable) {
                Log.getLogger().log(Level.SEVERE, "Uncaught Exception", throwable);
            }
        };
        Thread thread = new Thread(group, "Safe Main Thread") {
            public void run() {
                realmain(args);
            }
        };
        thread.setDaemon(true);
        thread.start();
        // We have to give the thread time to get going or the entire application will exit!
        SystemUtilities.sleepMsec(5000);
    }

    public static void realmain(String[] args) {
        initialize();
        _splashScreen = new SplashScreen();
        try {
            init(args);
        } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "failed to initialize", e);
        }
        _splashScreen.dispose();
        _splashScreen = null;
    }

    private static void parseOptions(String[] args) {
        // do nothing
    }

    public static void repaint() {
        if (_mainFrame != null) {
            _mainFrame.repaint();
        }
    }

    private static void showMainFrame() {
        _splashScreen.setVisible(false);
        _mainFrame.setVisible(true);
        ProjectManager.getProjectManager().bringErrorFrameToFront();
    }

    public static void shutdown() {
        _mainFrame.dispatchEvent(new WindowEvent(_mainFrame, WindowEvent.WINDOW_CLOSING));
    }

}
