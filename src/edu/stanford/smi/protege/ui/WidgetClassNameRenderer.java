package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Renderer for widget names.  The algorithm is just to strip the package name off of the widget class name and display
 * it.  This is really not a great scheme.  Better would be to simply ask the widget for a display name.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WidgetClassNameRenderer extends DefaultRenderer {
    private static final long serialVersionUID = 7526658612448514937L;
    public static final String NONE = LocalizedText.getText(ResourceKey.FORM_EDITOR_SELECT_NO_WIDGET);

    public void load(Object o) {
        String text;
        if (NONE.equals(o)) {
            text = NONE;
        } else {
            String longName = (String) o;
            int index = longName.lastIndexOf('.');
            text = longName.substring(index + 1);
        }
        setMainText(text);
    }

    public void loadNull() {
        setMainText(LocalizedText.getText(ResourceKey.FORM_EDITOR_SELECT_A_WIDGET_PROMPT));
    }
}
