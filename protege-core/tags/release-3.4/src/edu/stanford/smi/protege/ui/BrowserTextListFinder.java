package edu.stanford.smi.protege.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListModel;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.FrameWithBrowserText;
import edu.stanford.smi.protege.util.SimpleStringMatcher;
import edu.stanford.smi.protege.util.StringMatcher;

public class BrowserTextListFinder extends ListFinder {

	private static final long serialVersionUID = 3317904008378472274L;
	
	public BrowserTextListFinder(JList list, ResourceKey key) {
		super(list, key);	
	}

	 protected List getMatches(String text, int maxMatches) {
	        if (!text.endsWith("*")) {
	            text += "*";
	        }

	        StringMatcher matcher = new SimpleStringMatcher(text);
	        List matchingInstances = new ArrayList();
	        ListModel model = _list.getModel();
	        int size = model.getSize();
	        for (int i = 0; i < size; ++i) {
	            FrameWithBrowserText fbt = (FrameWithBrowserText) model.getElementAt(i);
	            String browserText = fbt.getBrowserText();	            
	            if (browserText != null) {      	
	            	browserText = browserText.toLowerCase();
	            	if (matcher.isMatch(browserText)) {
	            		matchingInstances.add(fbt.getFrame());
	            	}
	            }
	        }
	        return matchingInstances;
	    }
	 
	 
	 protected void select(Object o) {
		 if (o instanceof Frame) {
			 _list.setSelectedValue(new FrameWithBrowserText((Frame)o), true);
		 } else if (o instanceof FrameWithBrowserText) {			 
			 _list.setSelectedValue(o, true);
		 } else {
			 _list.setSelectedIndex(0);
		 }
	 }
}
