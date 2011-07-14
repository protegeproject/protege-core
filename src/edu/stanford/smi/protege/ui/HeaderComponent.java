package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 *
 * TODO Class Comment
 */
public class HeaderComponent extends JPanel {
    private static final long serialVersionUID = 6410242403360276543L;
    private JComponent component;
    private JLabel titleLabel;
    private JPanel titlePanel;
    private JLabel componentLabel;
    private JToolBar toolBar = ComponentFactory.createToolBar();
    
    public HeaderComponent(JComponent component) {
        this("", "", component);
    }
    
    public HeaderComponent(String title, String label, JComponent component) {
        this.component = component;
	    setLayout(new BorderLayout());
	    titlePanel = new JPanel(new BorderLayout());
	    titlePanel.setBackground(Colors.getClsColor());
	    titleLabel = ComponentFactory.createTitleFontLabel(title.toUpperCase());
	    titleLabel.setForeground(Color.white);
	    titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
	    titlePanel.add(titleLabel);
	    add(titlePanel, BorderLayout.NORTH);
	
	    componentLabel = ComponentFactory.createSmallFontLabel("");
	    componentLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
	    JPanel subjectPanel = new JPanel(new BorderLayout());
	    subjectPanel.add(componentLabel, BorderLayout.WEST);
	    subjectPanel.add(component, BorderLayout.CENTER);
	    add(subjectPanel, BorderLayout.CENTER);
	    add(toolBar, BorderLayout.EAST);
	    setComponentLabel(label);
	}
    
    public JButton addButton(Action action) {
        return ComponentFactory.addToolBarButton(toolBar, action);
    }
    
    public JToolBar getToolBar() {
        return toolBar;
    }
    
    public JToggleButton addToggleButton(Action action) {
        return ComponentFactory.addToggleToolBarButton(toolBar, action);
    }
    
    public JComponent getComponent() {
        return component;
    }
    
    public void setTitle(String title) {
        setTitle(title, true);
    }
    
    public void setTitle(String title, boolean toUpperCase) {    	
        titleLabel.setText(toUpperCase == true ? title.toUpperCase() : title);
    }
    
    
    public void setComponentLabel(String label) {
        componentLabel.setText(label);
    }
    
    public void setColor(Color color) {
        titlePanel.setBackground(color);
    }
}
