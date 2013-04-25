package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * A tab the mimics the old "KA Tool" application from Protege/Win.  The user selects a "top level" instance and 
 * the tab will display it and allow you to navigate to other instances.  There is no class or instance browser and
 * no search capability.
 * 
 * @author  Ray Fergerson   <fergerson@smi.stanford.edu>
 */
public class KAToolTab extends AbstractTabWidget {
    private static final long serialVersionUID = -9019526061083858552L;
    private static final String TOP_LEVEL_INSTANCE_PROPERTY = "KATool.TOP_LEVEL_INSTANCE";

    public boolean configure() {
        KAToolTabConfigurationPanel panel = new KAToolTabConfigurationPanel(this);
        ModalDialog.showDialog(this, panel, "Configure KA Tool", ModalDialog.MODE_OK_CANCEL);
        return true;
    }

    private JComponent createClsPanel() {
        Instance topLevelInstance = getTopLevelInstance();
        Assert.assertNotNull("top level instance", topLevelInstance);
        InstanceDisplay d = new InstanceDisplay(getProject());
        d.setInstance(topLevelInstance);
        return d;
    }

    private Action createSelectClsAction() {
        return new AbstractAction("Press to select top-level instance") {
            private static final long serialVersionUID = -6735287155779212076L;

            public void actionPerformed(ActionEvent event) {
                Instance instance = DisplayUtilities.pickInstance(KAToolTab.this, getKnowledgeBase());
                if (instance != null) {
                    setTopLevelInstance(instance);
                }
            }
        };
    }

    /**
     * Insert the method's description here.
     * Creation date: (8/17/2000 5:43:40 PM)
     */
    private JComponent createSetupPanel() {
        JPanel outerPanel = new JPanel(new FlowLayout());
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(ComponentFactory.createButton(createSelectClsAction()));
        outerPanel.add(innerPanel);
        return outerPanel;

    }

    public Instance getTopLevelInstance() {
        Instance result = null;
        String name = getPropertyList().getString(TOP_LEVEL_INSTANCE_PROPERTY);
        if (name != null) {
            result = getKnowledgeBase().getInstance(name);
            if (result == null) {
                Log.getLogger().warning("Unable to find top level instance " + name);
            }
        }
        return result;
    }

    /**
     * initialize method comment.
     */
    public void initialize() {
        if (getLabel() == null) {
            setLabel("Knowledge Acquisition");
        }
        setupUI();
    }

    public void setTopLevelInstance(Instance instance) {
        getPropertyList().setString(TOP_LEVEL_INSTANCE_PROPERTY, instance.getName());
        setupUI();
    }

    private void setupUI() {
        removeAll();
        setLayout(new BorderLayout());
        Instance topLevelInstance = getTopLevelInstance();
        JComponent mainForm;
        if (topLevelInstance == null) {
            mainForm = createSetupPanel();
        } else {
            mainForm = createClsPanel();
        }
        add(mainForm);
        revalidate();
        repaint();
    }
}
