package edu.stanford.smi.protege.util;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

/**
 * The component that handles the standard layout of slot widgets. This layout
 * consists of a label at the top left and a series of buttons at the top right
 * and then a "center component" that takes up the rest of the space. There can
 * optionally be a footer component at the bottom (usually used for a "find"
 * box).
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 * @author Holger Knublauch <holger@smi.stanford.edu>(minor extensions)
 */
public class LabeledComponent extends JComponent {
    private List actions = new ArrayList();
    private JComponent _header;
    private JToolBar _toolBar;
    private JLabel _label;
    private JComponent _headerComponentHolder;
    private JComponent _centerComponentHolder;
    private JComponent _footerComponentHolder;
    private boolean _isVerticallyStretchable;

    public LabeledComponent(String label, Component c) {
        this(label, c, c instanceof JScrollPane);
    }

    public LabeledComponent(String label, Component c, boolean verticallyStretchable) {
        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);
        add(createCenterComponentHolder(), BorderLayout.CENTER);
        add(createFooterComponentHolder(), BorderLayout.SOUTH);

        _isVerticallyStretchable = verticallyStretchable || (c instanceof JScrollPane);
        setHeaderLabel(label);
        setCenterComponent(c);
    }

    public LabeledComponent(String label, JScrollPane c) {
        this(label, c, true);
    }

    public JButton addHeaderButton(Action action) {
        JButton button = null;
        if (action != null) {
            actions.add(action);
            button = ComponentFactory.addToolBarButton(_toolBar, action);
        }
        return button;
    }

    public void addHeaderSeparator() {
        _toolBar.addSeparator();
    }

    private JComponent createCenterComponentHolder() {
        _centerComponentHolder = new JPanel();
        _centerComponentHolder.setLayout(new BorderLayout());
        return _centerComponentHolder;
    }

    private JComponent createFooterComponentHolder() {
        _footerComponentHolder = new JPanel();
        _footerComponentHolder.setLayout(new BorderLayout());
        return _footerComponentHolder;
    }

    private JComponent createHeader() {
        _header = new JPanel() {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = Math.max(d.height, ComponentFactory.STANDARD_BUTTON_HEIGHT);
                return d;
            }
        };
        _header.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderLabel(), BorderLayout.WEST);
        panel.add(createHeaderComponentHolder(), BorderLayout.CENTER);
        _header.add(panel, BorderLayout.CENTER);
        _header.add(createHeaderToolbar(), BorderLayout.EAST);
        return _header;
    }

    private JComponent createHeaderComponentHolder() {
        _headerComponentHolder = new JPanel();
        _headerComponentHolder.setLayout(new BorderLayout());
        return _headerComponentHolder;
    }

    private JComponent createHeaderLabel() {
        _label = ComponentFactory.createLabel();
        ComponentUtilities.setSmallLabelFont(_label);
        _label.setBorder(BorderFactory.createEmptyBorder(0, 4, 2, 0));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(_label, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent createHeaderToolbar() {
        _toolBar = ComponentFactory.createToolBar();
        return _toolBar;
    }

    public Component getCenterComponent() {
        Component component;
        int count = _centerComponentHolder.getComponentCount();
        if (count == 0) {
            component = null;
        } else {
            component = _centerComponentHolder.getComponent(0);
        }
        return component;
    }

    public Component getFooterComponent() {
        int count = _footerComponentHolder.getComponentCount();
        return (count == 0) ? (Component) null : _footerComponentHolder.getComponent(0);
    }

    public Collection getHeaderButtonActions() {
        return Collections.unmodifiableCollection(actions);
    }

    public Collection getHeaderButtons() {
        Collection buttons = new ArrayList();
        for (int i = 0; i < _toolBar.getComponentCount(); ++i) {
            Component c = _toolBar.getComponent(i);
            if (c instanceof AbstractButton) {
                buttons.add(c);
            }
        }
        return buttons;
    }

    public boolean hasHeaderButton(Icon icon) {
        boolean hasHeaderButton = false;
        Iterator i = getHeaderButtons().iterator();
        while (i.hasNext()) {
            AbstractButton button = (AbstractButton) i.next();
            if (button.getIcon().equals(icon)) {
                hasHeaderButton = true;
                break;
            }
        }
        return hasHeaderButton;
    }

    public Component getHeaderComponent() {
        int count = _headerComponentHolder.getComponentCount();
        return (count == 0) ? (Component) null : _headerComponentHolder.getComponent(0);
    }

    public String getHeaderLabel() {
        return _label.getText();
    }

    public void setComponentsEnabled(boolean b) {
        _label.setEnabled(b);
        getCenterComponent().setEnabled(b);
        _toolBar.setEnabled(b);
    }

    public boolean isVerticallyStretchable() {
        return _isVerticallyStretchable;
    }

    public void removeHeaderButton(int index) {
        int buttonIndex = -1;
        for (int i = 0; i < _toolBar.getComponentCount(); ++i) {
            Component c = _toolBar.getComponent(i);
            if (c instanceof AbstractButton) {
                ++buttonIndex;
                if (buttonIndex == index) {
                    _toolBar.remove(c);
                    break;
                }
            }
        }
    }

    public void removeAllHeaderButtons() {
        _toolBar.removeAll();
    }

    public void setCenterComponent(Component c) {
        _centerComponentHolder.removeAll();
        if (c != null) {
            String location = (_isVerticallyStretchable) ? BorderLayout.CENTER : BorderLayout.NORTH;
            _centerComponentHolder.add(c, location);
        }
        revalidate();
        repaint();
    }

    public void setFooterComponent(JComponent c) {
        _footerComponentHolder.removeAll();
        if (c != null) {
            _footerComponentHolder.add(c);
        }
    }

    public void setHeaderComponent(JComponent component) {
        setHeaderComponent(component, BorderLayout.CENTER);
    }

    public void setHeaderComponent(JComponent component, String alignment) {
        _headerComponentHolder.removeAll();
        if (component == null) {
            _headerComponentHolder.setBorder(null);
        } else {
            _headerComponentHolder.add(component, alignment);
            _headerComponentHolder.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        }
    }

    /**
     * Sets an Icon that will appear beside the label text.
     * 
     * @param icon
     *            the Icon to add
     */
    public void setHeaderIcon(Icon icon) {
        _label.setIcon(icon);
    }

    public void doLayout() {
        super.doLayout();
        adjustToolTipText();
    }

    /*
     * If the header is smaller than it wants to be then the extra comes out
     * of the label, so we provide the entire label text as a tooltip
     */
    private void adjustToolTipText() {
        if (_header.getPreferredSize().width > _header.getSize().width) {
            if (_label.getToolTipText() == null) {
                _label.setToolTipText(_label.getText());
            }
        } else {
            _label.setToolTipText(null);
        }
    }

    public void setHeaderLabel(String label) {
        _label.setText(label);
        _label.setVisible(label != null);
    }

    public void setVerticallyStretchable(boolean b) {
        if (b != _isVerticallyStretchable) {
            _isVerticallyStretchable = b;
            setCenterComponent(getCenterComponent());
        }
    }
}