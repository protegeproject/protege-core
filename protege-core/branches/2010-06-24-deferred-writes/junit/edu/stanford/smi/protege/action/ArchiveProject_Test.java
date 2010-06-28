package edu.stanford.smi.protege.action;
//ESCA*JAVA0130

import java.awt.*;
import java.util.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.test.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ArchiveProject_Test extends UITestCase {

    public void testArchive() {
        executeOnNextModalDialog(new Runnable() {
            public void run() {
                String randomText = new Date().toString();
                Component window = getTopWindow();
                setLabeledComponentText(window, "Comment", randomText);
                pressButton(window, Icons.getOkIcon());
            }
        });
        // pressToolBarButton(Icons.getArchiveProjectIcon(true, false));
        pressToolBarButton(Icons.getArchiveProjectIcon(false, false));
    }

}