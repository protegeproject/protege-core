package edu.stanford.smi.protege.widget;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.ExtensionFilter;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SelectableTable;


/**
 * Graphical component for handling property files. It supports the loading, editing and saving of property files.
 * @author ttania
 *
 */
public class ProtegePropertiesComponent extends JPanel {
	private static final long serialVersionUID = -5051686359840804602L;
    private SelectableTable _propertiesTable;
	private PropertiesTableModel _propertiesTableModel;
	
	private LabeledComponent _labeledComponent;
	private JLabel _warningLabel;
	
	private AllowableAction addAction;
	private AllowableAction viewAction;
	private AllowableAction deleteAction;
	private AllowableAction saveAction;
	private AllowableAction loadAction;

	private File lastDirectory = ApplicationProperties.getLastFileDirectory();
	
	private File _propertiesFile = null;
	
	public ProtegePropertiesComponent() {
		buildGUI(null);
	}
	
	public ProtegePropertiesComponent(Properties properties) {
		buildGUI(properties);
	}
	
	public ProtegePropertiesComponent(File propertyFile){
		buildGUI(loadPropertyFile(propertyFile));
	}
	
	
	public void buildGUI(Properties properties) {
		setLayout(new BorderLayout());
				
		_propertiesTable = new SelectableTable() {
			private static final long serialVersionUID = -7335109538274237998L;

            public void editingStopped(javax.swing.event.ChangeEvent e) {				
				int editingColumn = getEditingColumn();
				String cellValue = (String) getCellEditor().getCellEditorValue();
					
				super.editingStopped(e);
				
				if (editingColumn == PropertiesTableModel.getPropertyColumnIndex()) {					
					int newRow = ((PropertiesTableModel)getModel()).getRowOfProperty(cellValue);
					
					if (newRow >= 0) {
						ComponentUtilities.scrollToVisible(_propertiesTable, newRow, PropertiesTableModel.getValueColumnIndex());
						_propertiesTable.setRowSelectionInterval(newRow, newRow);						
						_propertiesTable.editCellAt(newRow, PropertiesTableModel.getValueColumnIndex());
						_propertiesTable.requestFocus();								
					}
				} 				
			};
		};
				
		_propertiesTableModel = new PropertiesTableModel(properties);
		_propertiesTable.setModel(_propertiesTableModel);
		
		_propertiesTable.getColumnModel().getColumn(1).setCellRenderer(new TextAreaRenderer());
		//_propertiesTable.getColumnModel().getColumn(1).setCellEditor(new TextAreaEditor());
		
		_propertiesTable.getTableHeader().setReorderingAllowed(false);
				
		_labeledComponent = new LabeledComponent("Property Table", new JScrollPane(_propertiesTable));
		_warningLabel = new JLabel("", null,JLabel.CENTER);
				
		viewAction = createViewAction();
		addAction = createAddAction();
		deleteAction = createDeleteAction();
		saveAction = createSaveAction();
		loadAction = createLoadAction();
		
		saveAction.setName("Save property file as ...");
		loadAction.setName("Load property file");
		
		_labeledComponent.addHeaderButton(viewAction);
		_labeledComponent.addHeaderButton(addAction);
		_labeledComponent.addHeaderButton(deleteAction);
		_labeledComponent.addHeaderButton(saveAction);
		_labeledComponent.addHeaderButton(loadAction);
					
		add(_warningLabel, BorderLayout.NORTH);
		add(_labeledComponent, BorderLayout.CENTER);
		
		updateLabel();
	}

	private AllowableAction createViewAction(){
		return new AllowableAction(ResourceKey.VALUE_VIEW, _propertiesTable) {

			private static final long serialVersionUID = -782664778236281392L;

            public void actionPerformed(ActionEvent e) {
				int selectedRow = _propertiesTable.getSelectedRow();
				
				String propName = (String)_propertiesTable.getValueAt(selectedRow, PropertiesTableModel.getPropertyColumnIndex());
				String propValue = (String)_propertiesTable.getValueAt(selectedRow, PropertiesTableModel.getValueColumnIndex());
				
				JPanel panel = new JPanel();
				panel.setLayout(new GridLayout());
				panel.setPreferredSize(new Dimension(300,100));				
				
				JTextArea textArea = new JTextArea();
				textArea.setLineWrap(true);
				textArea.setWrapStyleWord(true);
				textArea.setText(propName + " = " + propValue);
				textArea.setEnabled(false);	
				panel.add(new JScrollPane(textArea));
								
				ModalDialog.showDialog(ProtegePropertiesComponent.this, panel , "View property value", ModalDialog.MODE_CLOSE);				
			}			
		};			
	}

	private AllowableAction createAddAction(){
		return new AllowableAction(ResourceKey.VALUE_ADD) {

			private static final long serialVersionUID = 4094647618090854671L;

            public void actionPerformed(ActionEvent arg0) {
				((PropertiesTableModel)_propertiesTable.getModel()).addRow("","");
				
				_propertiesTable.clearSelection();
				ComponentUtilities.scrollToVisible(_propertiesTable, 0, 0);
				_propertiesTable.setRowSelectionInterval(0, 0);
				_propertiesTable.editCellAt(0, 0);
				_propertiesTable.requestFocus();			
			}
		};
	}
	
	private AllowableAction createDeleteAction(){
		return new AllowableAction(ResourceKey.VALUE_DELETE, _propertiesTable) {

			private static final long serialVersionUID = 5139197510485952863L;

            public void actionPerformed(ActionEvent arg0) {
				if (_propertiesTable.getCellEditor() != null)
					_propertiesTable.getCellEditor().stopCellEditing();
				
				int selectedRow = _propertiesTable.getSelectedRow();
				
				_propertiesTableModel.deleteRow(selectedRow);
				
				if (_propertiesTableModel.getRowCount() > selectedRow)
					_propertiesTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		};
	}
	
	private AllowableAction createSaveAction(){		
		return new AllowableAction(ResourceKey.PROJECT_SAVE) {

			private static final long serialVersionUID = 6607784184953248192L;

            public void actionPerformed(ActionEvent arg0) {
				savePropertyFile();
			}					
		};	
	}
	
	private void savePropertyFile() {
		JFileChooser chooser = createFileChooser("Save property file", "Property Files", "", false);
        int openDialogResult = chooser.showSaveDialog(ProtegePropertiesComponent.this);
        
        if  (openDialogResult == JFileChooser.APPROVE_OPTION) {
        	File propFile = chooser.getSelectedFile();       	      	
        	
        	if (savePropertyFile(propFile)) {
        		ModalDialog.showMessageDialog(ProtegePropertiesComponent.this, "Property file written out successfully to:\n" + propFile.getAbsolutePath(), "Property file saved");
        	}    	
        	else        	
        		ModalDialog.showMessageDialog(ProtegePropertiesComponent.this, "Error writing property file", "Error");
        }
	}
	
	public boolean savePropertyFile(File propFile) {
		return savePropertyFile(propFile, false);
	}
	
	public boolean savePropertyFile(File propFile, boolean escapeColonChar) {
    	try {
    		OutputStream out = new FileOutputStream(propFile);
    		
    		//TT: The Java Properties escapes the ":" charachter in the saved file, and lax cannot in
    		//interpret the new Lax file correctly. That is why another save method is used for that.    		
    		if (escapeColonChar)
    			FileUtilities.savePropertiesFile(propFile, _propertiesTableModel.getProperties());
    		else
    			_propertiesTableModel.getProperties().store(out, "Generated by ProtegePropertyTab");
        	
        	_propertiesTableModel.setChanged(false);
        	return true;
		} catch (Exception e) {			
			Log.getLogger().warning("Could not write property file: " + propFile.getAbsolutePath());
		}		
		return false;
	}
	
	private AllowableAction createLoadAction(){
		return new AllowableAction(ResourceKey.PROJECT_OPEN) {

			private static final long serialVersionUID = 4999564289768724565L;

            public void actionPerformed(ActionEvent arg0) {
				
				if (isChangedContent()) {
					int rval = ModalDialog.showMessageDialog(ProtegePropertiesComponent.this, "Save current properties?", "Save", ModalDialog.MODE_YES_NO);
					if (rval == ModalDialog.OPTION_YES) {
						savePropertyFile();
					}
				}
				
				JFileChooser chooser = createFileChooser("Load property file","Property files", "", true);				
                int openDialogResult = chooser.showOpenDialog(ProtegePropertiesComponent.this);
                
                if  (openDialogResult == JFileChooser.APPROVE_OPTION) {                	
                	File propFile = chooser.getSelectedFile();
            		Properties properties = loadPropertyFile(propFile);
            		_propertiesTableModel.setProperties(properties);
            		updateLabel();
                 }
			}
		};			
	}
	
	private Properties loadPropertyFile(File propFile) {
       	Properties properties = new Properties();
    	
		try {
	        InputStream is = new FileInputStream(propFile);
	        properties.load(is);
	        is.close();
		} catch (Exception e) {
			ModalDialog.showMessageDialog(ProtegePropertiesComponent.this, "Error loading property file", "Error");
			Log.getLogger().warning("Could not open property file: " + propFile.getAbsolutePath());
		}
		
		_propertiesFile = propFile;
			
		return properties;
	}

	public AllowableAction getAddAction() {
		return addAction;
	}

	public AllowableAction getDeleteAction() {
		return deleteAction;
	}

	public AllowableAction getLoadAction() {
		return loadAction;
	}

	public AllowableAction getSaveAction() {
		return saveAction;
	}

	public AllowableAction getViewAction() {
		return viewAction;
	}

	
    private JFileChooser createFileChooser(String title, String fileDescription, String fileExtension, final boolean overwriteWithoutConfirm) {
    	    	
        JFileChooser chooser = new JFileChooser(lastDirectory) {
            private static final long serialVersionUID = -3429961883209448355L;

            public int showDialog(Component c, String s) {
                int rval = super.showDialog(c, s);
                if (rval == APPROVE_OPTION) {
                    lastDirectory = getCurrentDirectory();
                }
                return rval;
            }
            
            public void approveSelection() {
            	if (overwriteWithoutConfirm) {
            		super.approveSelection();
            		return;
            	}
            	
            	File f = getSelectedFile();
            	
            	if ( f.exists() ) {
            		String msg = "The file '"+ f.getName() + "' already exists!\nDo you want to replace it?";            		
            		String title = getDialogTitle();
            		int option = JOptionPane.showConfirmDialog( this, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
            		if ( option == JOptionPane.NO_OPTION ) {
            			return;
            		}
            	}            	
            	super.approveSelection();            	
            }
            
        };
        
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle(title);
        if (fileExtension != null && fileExtension.length() > 0) {        
            String text = fileDescription;
            chooser.setFileFilter(new ExtensionFilter(fileExtension, text));
        }
        return chooser;
    }
	
    private void updateLabel() {
    	String headerLabel = "Property Table    (" + (_propertiesFile == null ? ApplicationProperties.FILE_NAME: _propertiesFile.getName()) + ")";
    	
    	_labeledComponent.setHeaderLabel(headerLabel);
    	    	
    	if (_propertiesFile != null)
    		_warningLabel.setToolTipText(_propertiesFile.getAbsolutePath());
    	 
    	_warningLabel.setFont(new Font(null, Font.BOLD, 12));
    	_warningLabel.setText(_propertiesFile == null ? "":"  Changes take effect after restarting " + Text.getProgramName());
    	_warningLabel.setIcon(_propertiesFile == null ? null: Icons.getIcon(new ResourceKey("warning")));    	
    }
    
    public void setVisibleHeaderButton(AllowableAction buttonAction, boolean visible) {
    	for (Iterator iter = _labeledComponent.getHeaderButtons().iterator(); iter.hasNext();) {
			JButton button = (JButton) iter.next();
			
			if (button.getAction() == buttonAction)
				button.setVisible(visible);			
		}
    }
    	
    public boolean isChangedContent() {
    	return _propertiesTableModel.isChanged();
    }
    
    public void stopCellEditing(){
    	if (_propertiesTable.getCellEditor() != null)
    		_propertiesTable.getCellEditor().stopCellEditing();
    }
}
