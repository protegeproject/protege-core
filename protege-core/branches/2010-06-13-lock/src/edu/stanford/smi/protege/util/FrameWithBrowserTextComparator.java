package edu.stanford.smi.protege.util;

import java.util.Comparator;

import edu.stanford.smi.protege.model.Frame;

public class FrameWithBrowserTextComparator implements
		Comparator<FrameWithBrowserText> {

	public int compare(FrameWithBrowserText fbt1, FrameWithBrowserText fbt2) {
		String bt1 = fbt1.getBrowserText();
		String bt2 = fbt2.getBrowserText();
		if (bt1 != null && bt2 != null) {
			if (bt1.charAt(0) == '\'') {
				bt1 = bt1.substring(1);
			}
			if (bt2.charAt(0) == '\'') {
				bt2 = bt2.substring(1);
			}
			return bt1.compareTo(bt2);
		}
		Frame f1 = fbt1.getFrame();
		Frame f2 = fbt2.getFrame();
		if (f1 != null) {
			return f2 == null ? 1 : f1.compareTo(f2);
		}
		if (f2 != null) {
			return f1 == null ? -1 : f1.compareTo(f2);
		}
		return 0;
	}

}
