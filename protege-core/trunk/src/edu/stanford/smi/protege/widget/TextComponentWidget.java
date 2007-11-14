package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for acquiring a string of arbitrary length.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class TextComponentWidget extends AbstractSlotWidget {
	
    private static final Color INVALID_COLOR = Color.red;
    private JTextComponent _textComponent;
    private Color _defaultColor;
    private boolean _isDirty;

    private DocumentChangedListener _documentListener = new DocumentChangedListener() {
        public void stateChanged(ChangeEvent event) {
            onTextChange();
        }
    };
    private FocusListener _focusListener = new FocusAdapter() {
        public void focusLost(FocusEvent event) {
            commitChanges();
        }
    };

    private KeyListener _keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                commitChanges();
            }
        }
    };

    public void commitChanges() {
        onCommit();
        if (_isDirty) {
            valueChanged();
        }
    }

    protected void onCommit() {
    }

    protected Collection createActions() {
        return Collections.EMPTY_LIST;
    }

    private void onTextChange() {
        validateText(getText());
        _isDirty = true;
    }

    protected abstract JTextComponent createTextComponent();

    protected abstract JComponent createCenterComponent(JTextComponent textComponent);

    public void dispose() {
        commitChanges();
        super.dispose();
    }

    protected String getInvalidTextDescription(String text) {
        return null;
    }

    public String getText() {
        String s = _textComponent.getText().trim();
        return s.length() == 0 ? null : s;
    }

    public JTextComponent getTextComponent() {
        return _textComponent;
    }

    public void markDirty(boolean b) {
        _isDirty = b;
    }

    public void setInstanceValues() {
        super.setInstanceValues();
        _isDirty = false;
    }

    public Collection getValues() {
        String s = getText();
        return CollectionUtilities.createList(s);
    }

    public void initialize(boolean isStretchable, int nColumns, int nRows) {
    	initialize(isStretchable, false, nColumns, nRows);
    }
    
    public void initialize(boolean isStretchable, boolean isSwappedHeader, int nColumns, int nRows) {
        _textComponent = createTextComponent();
        _textComponent.getDocument().addDocumentListener(_documentListener);
        _textComponent.addFocusListener(_focusListener);
        _textComponent.addKeyListener(_keyListener);
        this.setEditable(true);
        _defaultColor = _textComponent.getForeground();
        JComponent centerComponent = createCenterComponent(_textComponent);
        LabeledComponent labeledComponent = new LabeledComponent(getLabel(), centerComponent, isStretchable, isSwappedHeader);
        Iterator i = createActions().iterator();
        while (i.hasNext()) {
            Action action = (Action) i.next();
            labeledComponent.addHeaderButton(action);
        }
        add(labeledComponent);
        setPreferredColumns(nColumns);
        setPreferredRows(nRows);
    }

    public boolean isEditable() {
        return _textComponent.isEditable();
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable;
        if (cls == null || slot == null) {
            isSuitable = false;
        } else {
            ValueType type = cls.getTemplateSlotValueType(slot);
            boolean isString = type == ValueType.STRING;
            boolean isSymbol = type == ValueType.SYMBOL;
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = (isString || isSymbol) && !isMultiple;
        }
        return isSuitable;
    }

    public void selectAll() {
        /*
         * This has to be done in invokeLater because TextComponent.selectAll() doesn't work unless the text is already
         * on the screen.
         */
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _textComponent.selectAll();
                _textComponent.requestFocus();
            }
        });
    }

    public void setEditable(boolean b) {
    	b = b && !isReadOnlyConfiguredWidget();
    	
        _textComponent.setEditable(b);
    }
  

    public void setText(String text) {
        // _isDirty = false;
        _documentListener.disable();
        _textComponent.setText(text == null ? "" : text);
        onSetText(text);
        _documentListener.enable();
        validateText(text);
    }

    protected void onSetText(String text) {
        // do nothing
    }

    public void setInstance(Instance instance) {
        if (_isDirty) {
            valueChanged();
        }
        super.setInstance(instance);
    }

    public void setValues(Collection values) {
        Object o = CollectionUtilities.getFirstItem(values);
        String text = o == null ? (String) null : o.toString();
        setText(text);
    }

    protected boolean validateText(String text) {
        String errorDescription = text == null ? null : getInvalidTextDescription(text);
        if (errorDescription == null) {
            _textComponent.setForeground(_defaultColor);
            _textComponent.setToolTipText(null);
        } else {
            _textComponent.setForeground(INVALID_COLOR);
            _textComponent.setToolTipText(errorDescription);
        }
        return errorDescription == null;
    }
    
    
    @Override
    public WidgetConfigurationPanel createWidgetConfigurationPanel() {
    	WidgetConfigurationPanel confPanel = super.createWidgetConfigurationPanel();
    	
    	confPanel.addTab("Options", new ReadOnlyWidgetConfigurationPanel(this));
    	
    	return confPanel;
    }
 
    
}