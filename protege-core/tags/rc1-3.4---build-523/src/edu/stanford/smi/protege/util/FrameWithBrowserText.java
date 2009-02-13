package edu.stanford.smi.protege.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;

public class FrameWithBrowserText implements Serializable, Localizable {
	private static final long serialVersionUID = -8597023478383674378L;

	private Frame frame;
	private String browserText;
	private Collection<Cls> types;


	public FrameWithBrowserText(Frame frame) {
		this(frame, null, null);
	}

	public FrameWithBrowserText(Frame frame, String browserText,
			Collection<Cls> types) {
		this.frame = frame;
		this.browserText = browserText == null ? frame.getName() : browserText;
		this.types = types == null ? new ArrayList<Cls>() : types;
	}

	public Frame getFrame() {
		return frame;
	}
	public void setFrame(Frame frame) {
		this.frame = frame;
	}
	public String getBrowserText() {
		return browserText;
	}
	public void setBrowserText(String browserText) {
		this.browserText = browserText;
	}
	public Collection<Cls> getTypes() {
		return types;
	}
	public void setTypes(Collection<Cls> types) {
		this.types = types;
	}

	public void localize(KnowledgeBase kb) {
		LocalizeUtils.localize(frame, kb);
		LocalizeUtils.localize(types, kb);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FrameWithBrowserText)) { return false;}
		FrameWithBrowserText fbt = (FrameWithBrowserText)obj;		
		return fbt.getFrame() == null ? false : fbt.getFrame().equals(frame);
	}

	@Override
	public int hashCode() {
		return
			frame == null ? 42 :
				frame.getName().length() * 43 + 7 * browserText.length() + 5 * types.size() + 3;
	}

}
