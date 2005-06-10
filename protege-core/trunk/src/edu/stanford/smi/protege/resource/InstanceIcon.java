package edu.stanford.smi.protege.resource;

import java.awt.*;

import javax.swing.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class InstanceIcon implements Icon {
    private static final int ICON_SIZE = 16;
    private static final int DIAMOND_HEIGHT = 12;
    private static final int DIAMOND_WIDTH = 10;

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);
        Color color = g.getColor();
        g.setColor(Colors.getInstanceColor());
        int xoffset = (ICON_SIZE - DIAMOND_WIDTH) / 2;
        int yoffset = (ICON_SIZE - DIAMOND_HEIGHT) / 2;
        Polygon polygon = new Polygon();
        polygon.addPoint(xoffset, ICON_SIZE / 2);
        polygon.addPoint(ICON_SIZE / 2, yoffset);
        polygon.addPoint(xoffset + DIAMOND_WIDTH, ICON_SIZE / 2);
        polygon.addPoint(ICON_SIZE / 2, yoffset + DIAMOND_HEIGHT);
        ((Graphics2D) g).fill(polygon);
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
