package edu.stanford.smi.protege.ui;

import java.awt.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;

/**
 * Frame Renderer that makes it clear whether the frame being rendered is valid or not.  Invalid frames are rendered in
 * red.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class OwnSlotValueFrameRenderer extends FrameRenderer {
    private static final long serialVersionUID = 7172693591093061328L;
    private static final Color INVALID_ITEM_COLOR = Color.red;
    private final Frame _frame;
    private final Slot _slot;

    public OwnSlotValueFrameRenderer(Frame frame, Slot slot) {
        _frame = frame;
        _slot = slot;
    }

    public void load(Object o) {
        super.load(o);
        if (!_frame.isValidOwnSlotValue(_slot, o)) {
            setForegroundColorOverride(INVALID_ITEM_COLOR);
        }
    }
}
