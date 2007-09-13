package edu.stanford.smi.protege.plugin;
//ESCA*JAVA0130

import java.util.*;

import junit.framework.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PluginUtilities_Test extends TestCase {

    static {
        SystemUtilities.initialize();
    }

    public void testGetClassesWithAttribute() {
        Collection values = PluginUtilities.getClassesWithAttribute("Tab-Widget", "TRUE");
        assertTrue(values.contains(ClsesTab.class));
        assertTrue(values.size() > 5);
    }
}
