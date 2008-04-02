package edu.stanford.smi.protege.resource;

import java.awt.*;

import javax.swing.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class SlotIcon implements Icon {
    private static final int ICON_SIZE = 16;
    private static final int HEIGHT = 7;
    private static final int WIDTH = 12;

    public void paintIcon(Component c, Graphics g, int x, int y) {
		g.translate(x, y);
		Color color = g.getColor();
		g.setColor(Colors.getSlotColor());
		int xoffset = (ICON_SIZE - WIDTH)/2;
		int yoffset = (ICON_SIZE - HEIGHT)/ 2;
		g.fillRect(xoffset, yoffset, WIDTH, HEIGHT);
		g.setColor(color);
		g.translate(-x, -y);
    }

    public int getIconWidth()  { return ICON_SIZE; }
    public int getIconHeight() { return ICON_SIZE; }
}

