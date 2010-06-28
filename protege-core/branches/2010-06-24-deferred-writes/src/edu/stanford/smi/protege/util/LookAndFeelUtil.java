package edu.stanford.smi.protege.util;

import javax.swing.JTree;
import javax.swing.PopupFactory;

import com.jgoodies.looks.FontPolicies;
import com.jgoodies.looks.FontPolicy;
import com.jgoodies.looks.FontSet;
import com.jgoodies.looks.FontSets;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;

public class LookAndFeelUtil {

	public static void setUpPlasticLF() {
		try {
			 PopupFactory.setSharedInstance(new PopupFactory());
	         PlasticLookAndFeel.setCurrentTheme(PlasticHack.createTheme());
	         PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);

	         FontSet fontSet = FontSets.createDefaultFontSet(ProtegePlasticTheme.DEFAULT_FONT);
	         FontPolicy fixedPolicy = FontPolicies.createFixedPolicy(fontSet);
	         PlasticLookAndFeel.setFontPolicy(fixedPolicy);

		} catch (Throwable t) {
			Log.getLogger().warning("Could not load Plastic Look and Feel. " +
					"Probably looks.jar is not on the classpath. Error message: " + t.getMessage());
		}
	}


	public static void setTreeLineStyle(JTree comp) {
		try {
			comp.putClientProperty(Options.TREE_LINE_STYLE_KEY, Options.TREE_LINE_STYLE_NONE_VALUE);
		} catch (Throwable t) {
			Log.getLogger().warning("Could not set tree line style. Probably looks.jar is not on the classpath.");
		}
	}


	// This hack gets around a classloader problem if the looks jar is not on the classpath
	static class PlasticHack {
	    public static PlasticTheme createTheme() {
	        return new ProtegePlasticTheme();
	    }
	}

}
