package edu.stanford.smi.protege.resource;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.metal.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class ConfigureButtonIcon implements Icon {

    public void paintIcon(Component c, Graphics g, int x, int y) {
        JComponent component = (JComponent) c;
        int iconWidth = getIconWidth();

        g.translate(x, y);

        Color color = g.getColor();
        g.setColor(component.isEnabled() ? MetalLookAndFeel.getControlInfo() : MetalLookAndFeel.getControlShadow());
        g.drawLine(0, 0, iconWidth - 1, 0);
        g.drawLine(1, 1, 1 + (iconWidth - 3), 1);
        g.drawLine(2, 2, 2 + (iconWidth - 5), 2);
        g.drawLine(3, 3, 3 + (iconWidth - 7), 3);

        g.setColor(color);
        g.translate(-x, -y);
    }

    public int getIconWidth() {
        return 8;
    }

    public int getIconHeight() {
        return 4;
    }
}
