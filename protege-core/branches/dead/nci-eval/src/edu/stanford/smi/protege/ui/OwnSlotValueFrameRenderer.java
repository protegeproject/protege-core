package edu.stanford.smi.protege.ui;

import java.awt.Color;
import edu.stanford.smi.protege.model.*;

/**
 * Frame Renderer that makes it clear whether the frame being rendered is valid or not.  Invalid frames are rendered in
 * red.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class OwnSlotValueFrameRenderer extends FrameRenderer {
    private static final Color _invalidItemColor = Color.red;
    private final Frame _frame;
    private final Slot _slot;

    public OwnSlotValueFrameRenderer(Frame frame, Slot slot) {
        _frame = frame;
        _slot = slot;
    }

    public void load(Object o) {
        super.load(o);
        if (!_frame.isValidOwnSlotValue(_slot, o)) {
            setForegroundColorOverride(_invalidItemColor);
        }
    }
}
