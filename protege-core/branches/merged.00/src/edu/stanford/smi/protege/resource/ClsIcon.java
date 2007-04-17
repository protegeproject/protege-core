package edu.stanford.smi.protege.resource;

import java.awt.*;

import javax.swing.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class ClsIcon implements Icon {
    private static final int ICON_SIZE = 16;
    private static final int CIRCLE_SIZE = 10;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);
        Color color = g.getColor();
        g.setColor(Colors.getClsColor());
        int offset = (ICON_SIZE - CIRCLE_SIZE) / 2;
        g.fillOval(offset, offset, CIRCLE_SIZE, CIRCLE_SIZE);
        g.setColor(color);
        g.translate(-x, -y);
    }

    public int getIconWidth() {
        return ICON_SIZE;
    }

    public int getIconHeight() {
        return ICON_SIZE;
    }
}
