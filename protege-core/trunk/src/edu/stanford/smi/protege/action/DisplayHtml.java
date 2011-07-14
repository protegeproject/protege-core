package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Display a URL in a top level browser.  We do not use the built-in html display capabilities since they can't render
 * all HTML. We just delegate to a browser and let them handle it.
 * 
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DisplayHtml extends StandardAction {
    private static final long serialVersionUID = -2984843815869687450L;
    private String url;

    public DisplayHtml(String text, String url) {
        super(text);
        this.url = url;
    }
    
    public DisplayHtml(ResourceKey key, String url) {
        super(key);
        this.url = url;
    }

    public void actionPerformed(ActionEvent event) {
        SystemUtilities.showHTML(url);
    }
}
