package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.util.FrameWithBrowserText;

public class FrameWithBrowserTextRenderer extends FrameRenderer {

	private static final long serialVersionUID = 1262145669466722719L;

	@Override
		public void load(Object value) {
     	if (value instanceof FrameWithBrowserText) {
     		FrameWithBrowserText fbt = (FrameWithBrowserText) value;
     		setMainText(fbt.getBrowserText());
     		if (fbt.getFrame() != null) {
     		    //TODO: fix this - this might go to the server
     			setMainIcon(fbt.getFrame().getIcon());
     		}
     	} else {
     		super.load(value);
     	}
	 }
}
