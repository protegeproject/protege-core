package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A renderer for widget descriptors.  This just strips off the package information on the widget class file and 
 * displays the short class file name.  This is not really guaranteed to be unique and really isn't a great scheme 
 * anyway.  Much better would be to have another static method on the widget that specifies the browser text. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetDescriptorRenderer extends DefaultRenderer {
    private static final long serialVersionUID = 3251699554890095247L;
    private Project project;

    public WidgetDescriptorRenderer(Project project) {
        this.project = project;
    }

    public void load(Object o) {
        StringBuffer text = new StringBuffer();
        WidgetDescriptor d = (WidgetDescriptor) o;
        String longName = d.getWidgetClassName();
        if (longName == null) {
            text.append("<none>");
        } else {
            int index = longName.lastIndexOf('.');
            text.append(longName.substring(index + 1));
            Collection errors = new ArrayList();
            boolean isSuitable = WidgetUtilities.isSuitableTab(longName, project, errors);
            if (!isSuitable) {
                setFontStyle(Font.ITALIC);
                text.append(" -- ");
                Iterator i = errors.iterator();
                while (i.hasNext()) {
                    Object error = i.next();
                    text.append(error.toString());
                }
                // text.append(")");
            }
        }
        setMainText(text.toString());
    }
}
