package edu.stanford.smi.protege.util;

import java.util.Comparator;

public class FrameWithBrowserTextComparator implements
		Comparator<FrameWithBrowserText> {

	public int compare(FrameWithBrowserText fbt1, FrameWithBrowserText fbt2) {
		String bt1 = fbt1.getBrowserText();
		String bt2 = fbt2.getBrowserText();
		if (bt1 != null && bt2 != null) {
			return bt1.compareTo(bt2);
		}
		return fbt1.getFrame().compareTo(fbt2.getFrame());
	}

}
