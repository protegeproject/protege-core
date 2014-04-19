package edu.stanford.smi.protege.ui;

import javax.swing.Icon;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.FrameWithBrowserText;

public class FrameWithBrowserTextRenderer extends FrameRenderer {

	private static final long serialVersionUID = 1262145669466722719L;

	@Override
		public void load(Object value) {
     	if (value instanceof FrameWithBrowserText) {
     		FrameWithBrowserText fbt = (FrameWithBrowserText) value;
     		setMainText(fbt.getBrowserText());
     		if (fbt.getFrame() != null) {     		    
     			setMainIcon(getFbtIcon(fbt));
     		}
     	} else {
     		super.load(value);
     	}
	 }

	protected Icon getFbtIcon(FrameWithBrowserText fbt) {
		String iconName = fbt.getIconName();
		Frame frame = fbt.getFrame();
		if (iconName != null) {
			return Icons.getIcon(new ResourceKey(iconName));
		} else {
			return frame.getIcon(); //could go to the server
		}
	}
}
