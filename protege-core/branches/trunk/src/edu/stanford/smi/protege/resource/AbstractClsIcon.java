package edu.stanford.smi.protege.resource;

import java.awt.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class AbstractClsIcon extends ClsIcon {
    private static final int HOLE_SIZE = 2;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        super.paintIcon(c, g, x, y);
        g.translate(x, y);
        Color color = g.getColor();
		g.setColor(c.getBackground());
		int offset = (getIconWidth() - HOLE_SIZE)/2;
		g.fillOval(offset, offset, HOLE_SIZE, HOLE_SIZE);
		g.setColor(color);
		g.translate(-x, -y);
    }

    public int getIconWidth()  { return 16; }
    public int getIconHeight() { return 16; }
}

