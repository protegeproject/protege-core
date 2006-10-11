package edu.stanford.smi.protege.resource;

import java.awt.*;

import javax.swing.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class UglyIcon implements Icon {
    private static final int ICON_SIZE = 16;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);
        g.setColor(Color.GREEN);
        g.drawRect(0, 0, ICON_SIZE - 1, ICON_SIZE - 1);
        g.drawRect(1, 1, ICON_SIZE - 3, ICON_SIZE - 3);
        g.setColor(Color.RED);
        g.fillRect(2, 2, ICON_SIZE - 4, ICON_SIZE - 4);
        g.translate(-x, -y);
    }

    public int getIconWidth() {
        return ICON_SIZE;
    }

    public int getIconHeight() {
        return ICON_SIZE;
    }
}
