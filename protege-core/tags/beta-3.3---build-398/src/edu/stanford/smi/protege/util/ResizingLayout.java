package edu.stanford.smi.protege.util;

import java.awt.*;

import javax.swing.*;

/**
 * Layout manager that simply scales the existing components by default.  A component
 * can also be designated to take up all of either the extra horizontal or vertical space.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ResizingLayout implements LayoutManager2 {
    public static final String HORIZONTAL_STRETCHER = "horizonal_stretcher";
    public static final String VERTICAL_STRETCHER = "vertical_strecher";
    public static final String FILLING_VERTICALLY = "filling_vertically";
    public static final String FILLING_HORIZONTALLY = "filling_horizontally";

    public static final boolean VERTICAL_FILL_DEFAULT = false;
    public static final boolean HORIZONTAL_FILL_DEFAULT = true;

    private Dimension _previousSize;
    private boolean _resizeVerticallyOverride;

    public void addLayoutComponent(Component c, Object constraint) {
        // do nothing
    }

    public void addLayoutComponent(String s, Component c) {
        // do nothing
    }

    private static Component getHorizontalStretcher(Container container) {
        return (Component) ((JComponent) container).getClientProperty(HORIZONTAL_STRETCHER);
    }

    public void setResizeVertically(boolean b) {
        _resizeVerticallyOverride = b;
    }

    private boolean getIsFillingVertically(Container container) {
        return _resizeVerticallyOverride
                || getBooleanClientProperty(container, FILLING_VERTICALLY, VERTICAL_FILL_DEFAULT);
    }

    private static boolean getIsFillingHorizontally(Container container) {
        return getBooleanClientProperty(container, FILLING_HORIZONTALLY, HORIZONTAL_FILL_DEFAULT);
    }

    private static boolean getBooleanClientProperty(Container container, String propertyName, boolean defaultValue) {
        Boolean b = ((Boolean) (((JComponent) container).getClientProperty(propertyName)));
        return (b == null) ? defaultValue : b.booleanValue();
    }

    public float getLayoutAlignmentX(Container c) {
        return 0.5f;
    }

    public float getLayoutAlignmentY(Container c) {
        return 0.5f;
    }

    public static Dimension getSize(Container container) {
        Dimension size = new Dimension();
        for (int i = 0; i < container.getComponentCount(); ++i) {
            Rectangle r = container.getComponent(i).getBounds();
            size.width = Math.max(size.width, r.x + r.width);
            size.height = Math.max(size.height, r.y + r.height);
        }
        return size;
    }

    private static Component getVerticalStretcher(Container container) {
        return (Component) ((JComponent) container).getClientProperty(VERTICAL_STRETCHER);
    }

    public void invalidateLayout(Container c) {
        // do nothing
    }

    public void layoutContainer(Container container) {
        boolean fillingHorizontally = getIsFillingHorizontally(container);
        boolean fillingVertically = getIsFillingVertically(container);
        Component verticalStretcher = getVerticalStretcher(container);
        Component horizontalStretcher = getHorizontalStretcher(container);
        Dimension newSize = container.getSize();
        Dimension oldSize = (_previousSize == null) ? preferredLayoutSize(container) : _previousSize;
        if (!newSize.equals(oldSize)) {
            Point slidePoint = new Point();
            if (horizontalStretcher != null) {
                slidePoint.x = horizontalStretcher.getX() + horizontalStretcher.getWidth();
            }
            if (verticalStretcher != null) {
                slidePoint.y = verticalStretcher.getY() + verticalStretcher.getHeight();
            }
            for (int i = 0; i < container.getComponentCount(); ++i) {
                Component c = container.getComponent(i);
                resize(c, oldSize, newSize, slidePoint, horizontalStretcher, fillingHorizontally, verticalStretcher,
                        fillingVertically);
            }
        }
        _previousSize = newSize;
    }

    public Dimension maximumLayoutSize(Container c) {
        return new Dimension(10000, 10000);
    }

    public Dimension minimumLayoutSize(Container c) {
        return new Dimension();
    }

    public Dimension preferredLayoutSize(Container container) {
        return getSize(container);
    }

    public void removeLayoutComponent(Component c) {
        // do nothing
    }

    private static void resize(Component c, Dimension oldContainerSize, Dimension newContainerSize, Point slidePoint,
            Component horizontalStretcher, boolean fillingHorizontally, Component verticalStretcher,
            boolean fillingVertically) {
        Rectangle r = c.getBounds();
        if (fillingHorizontally) {
            if (horizontalStretcher == null) {
                r.x = rescale(r.x, newContainerSize.width, oldContainerSize.width);
                r.width = rescale(r.width, newContainerSize.width, oldContainerSize.width);
            } else if (c == horizontalStretcher) {
                r.width += newContainerSize.width - oldContainerSize.width;
            } else if (r.x >= slidePoint.x) {
                r.x += newContainerSize.width - oldContainerSize.width;
            } else {
                // do nothing
            }
        }
        if (fillingVertically) {
            if (verticalStretcher == null) {
                r.y = rescale(r.y, newContainerSize.height, oldContainerSize.height);
                r.height = rescale(r.height, newContainerSize.height, oldContainerSize.height);
            } else if (c == verticalStretcher) {
                r.height += newContainerSize.height - oldContainerSize.height;
            } else if (r.y >= slidePoint.y) {
                r.y += newContainerSize.height - oldContainerSize.height;
            } else {
                // do nothing
            }
        }
        c.setBounds(r);
    }

    private static int rescale(int x, int mult, int div) {
        return (int) Math.round(((double) x * mult) / div);
    }
}
