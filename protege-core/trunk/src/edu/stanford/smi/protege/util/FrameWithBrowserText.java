package edu.stanford.smi.protege.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;

public class FrameWithBrowserText implements Serializable, Localizable {
	private static final long serialVersionUID = -8597023478383674378L;

	private Frame frame;
	private String browserText;
	private Collection<Cls> types;
	private String iconName;

	public FrameWithBrowserText(Frame frame) {
		this(frame, null);
	}

	public FrameWithBrowserText(Frame frame, String browserText) {
		this(frame, browserText, null, null);
	}
	
	public FrameWithBrowserText(Frame frame, String browserText,
			Collection<Cls> types) {
		this(frame, browserText, types, null);
	}
	
	public FrameWithBrowserText(Frame frame, String browserText,
			Collection<Cls> types, String iconName) {
		this.frame = frame;
		this.browserText = browserText == null ? frame.getName() : browserText;
		this.types = types == null ? new ArrayList<Cls>() : types;
		this.iconName = iconName;
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
	
	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
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
				frame.getName().length() * 43 + frame.getName().hashCode();
	}
	
	/*
	 * Util methods
	 */
	
	public static Collection<FrameWithBrowserText> getFramesWithBrowserText(Collection<? extends Frame> frames) {
		if (frames == null) { return null; }	
		ArrayList<FrameWithBrowserText> fbts = new ArrayList<FrameWithBrowserText>();
		for (Frame frame : frames) {
			FrameWithBrowserText fbt = getFrameWithBrowserText(frame);
			if (fbt != null) {
				fbts.add(fbt);
			}
		}
		return fbts;
	}
	
	public static FrameWithBrowserText getFrameWithBrowserText(Frame frame) {
		if (frame == null) { return null; }
		return new FrameWithBrowserText(frame, frame.getBrowserText(), 
				(frame instanceof Instance) ? ((Instance)frame).getDirectTypes() : null);
	}
	
	public static Collection<Frame> getFrames(Collection<FrameWithBrowserText> framesWithBrowserText) {
		if (framesWithBrowserText == null) { return null; }	
		ArrayList<Frame> frames = new ArrayList<Frame>();
		for (FrameWithBrowserText fbt : framesWithBrowserText) {			
			Frame frame = fbt.getFrame();
			if (frame != null) {
				frames.add(frame);
			}
		}
		return frames;
	}

}
