package edu.stanford.smi.protege.widget;

import java.awt.GridLayout;

import javax.swing.JCheckBox;

import edu.stanford.smi.protege.util.AbstractValidatableComponent;
import edu.stanford.smi.protege.util.LabeledComponent;

public class ReadOnlyWidgetConfigurationPanel extends AbstractValidatableComponent{

	private static final long serialVersionUID = 8496065403491429614L;
    private AbstractSlotWidget widget;
	private JCheckBox readOnlyCheckBox;

	public ReadOnlyWidgetConfigurationPanel(AbstractSlotWidget widget) {
		this.widget = widget;
		
		setLayout(new GridLayout(0,1,10,10));
		
		readOnlyCheckBox = new JCheckBox("Read-only (Users will not be able to edit the value of this widget)");
		readOnlyCheckBox.setSelected(widget.isReadOnlyConfiguredWidget());
		
		LabeledComponent labledComp = new LabeledComponent("Options", readOnlyCheckBox, true);
		add(labledComp);
	}
		
	
	public void saveContents() {
		widget.setReadOnlyWidget(readOnlyCheckBox.isSelected());	
	}

	public boolean validateContents() {	
		return true;
	}

}
