package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * This class determines the default layout of a form.  The original idea was that users could plug in other layouts but
 * I never implemented this and no one has ever asked for it.<br> 
 * The heuristic used for laying out forms is first that
 * strings should come first and then other "small things".  "Big things" (such as list boxes) come last.  All items are
 * thus grouped by cardinality and then type.  This algorithm seems to do a decent job at organizing a form in a 
 * reasonable fashion.  The implementation of this algorithm is partially delegated to the {@link SlotWidgetComparer}
 * which "sorts" the widgets. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultWidgetLayoutStrategy implements WidgetLayoutStrategy {
    private final static int COLUMN_WIDTH = 250;
    private final static int MAX_COLUMNS = 3;

    private boolean canStretchVertically(Component c) {
        return c.getPreferredSize().height > ComponentUtilities.getStandardRowHeight();
    }

    private void doOneColumnLayout(List components) {
        Collections.sort(components, new SlotWidgetComparer());
        Point p = new Point();
        Dimension previousComponentSize = new Dimension();
        Iterator i = components.iterator();
        while (i.hasNext()) {
            Component c = (Component) i.next();
            Dimension currentComponentSize = c.getPreferredSize();
            if (currentComponentSize.width + p.x > COLUMN_WIDTH) {
                p.x = 0;
                p.y += previousComponentSize.height;
            }
            c.setBounds(new Rectangle(p, c.getPreferredSize()));
            p.x += currentComponentSize.width;
            previousComponentSize = currentComponentSize;
        }
    }

    private void doThreeColumnLayout(List components) {
        doNColumnLayout(3, components);
    }

    private void doTwoColumnLayout(List components) {
        doNColumnLayout(2, components);
    }

    private void doNColumnLayout(int n, List components) {
        if (!components.isEmpty()) {
            doOneColumnLayout(components);
            JComponent lastComponent = (JComponent) components.get(components.size() - 1);
            Rectangle bounds = lastComponent.getBounds();
            int columnHeight = (bounds.y + bounds.height) / n;
            int currentYShift = 0;
            int currentColumn = 0;
            int currentXShift = 0;
            int widestPoint = 0;
            Component[] bottomComponents = new Component[n];

            Iterator i = components.iterator();
            while (i.hasNext()) {
                Component c = (Component) i.next();
                int column = c.getY() / columnHeight;
                if (column != currentColumn) {
                    currentColumn = column;
                    currentYShift = c.getY();
                    currentXShift = widestPoint;
                }
                Point newLocation = new Point(c.getX() + currentXShift, c.getY() - currentYShift);
                widestPoint = Math.max(widestPoint, newLocation.x + c.getWidth());
                c.setLocation(newLocation);
                bottomComponents[column] = c;
                // Log.trace("location=" + newLocation + " for " + c, this, "doNColumnLayout");
            }
            evenBottomComponents(bottomComponents);
        }
    }

    private void evenBottomComponents(Component[] components) {
        int max = 0;
        for (int i = 0; i < components.length; ++i) {
            Component c = components[i];
            if (c != null) {
                max = Math.max(max, c.getY() + c.getHeight());
            }
        }

        for (int i = 0; i < components.length; ++i) {
            Component c = components[i];
            if (c != null) {
	            if (canStretchVertically(c)) {
	                Dimension size = new Dimension(c.getWidth(), max - c.getY());
	                c.setSize(size);
	            }
            }
        }

    }

    private int getPreferredNumberOfColumns(List components) {
        int maxWidth = 0;
        int totalHeight = 0;
        Iterator i = components.iterator();
        while (i.hasNext()) {
            Component c = (Component) i.next();
            Dimension d = c.getPreferredSize();
            maxWidth = Math.max(maxWidth, d.width);
            totalHeight += d.height;
        }

        // no worse than square
        int nColumns = (totalHeight + COLUMN_WIDTH - 1) / COLUMN_WIDTH;

        // no less than narrowest widget
        nColumns = Math.max(nColumns, ((maxWidth - 1) / COLUMN_WIDTH) + 1);

        // no more than MAX_COLUMNS
        nColumns = Math.min(MAX_COLUMNS, nColumns);

        return nColumns;
    }

    public void layout(Container container, int index) {
        // Log.enter(this, "layout", container, new Integer(index));
        List allComponents = new ArrayList(Arrays.asList(container.getComponents()));
        List moveableComponents = allComponents.subList(index, allComponents.size());
        int nColumns = getPreferredNumberOfColumns(moveableComponents);
        switch (nColumns) {
            case 1 :
                doOneColumnLayout(moveableComponents);
                break;
            case 2 :
                doTwoColumnLayout(moveableComponents);
                break;
            case 3 :
            default :
                doThreeColumnLayout(moveableComponents);
                break;
        }
        shiftMovableComponents(allComponents, index);
    }

    private void shiftMovableComponents(List allComponents, int index) {
        int height = 0;
        int nComponents = allComponents.size();
        if (index < nComponents) {
            for (int i = 0; i < index; ++i) {
                Component c = (Component) allComponents.get(i);
                Rectangle r = c.getBounds();
                height = Math.max(height, r.y + r.height);
            }
        }
        if (height > 0) {
            for (int i = index; i < nComponents; ++i) {
                Component c = (Component) allComponents.get(i);
                Point p = c.getLocation();
                p.y += height;
                c.setLocation(p);
            }
        }
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}
