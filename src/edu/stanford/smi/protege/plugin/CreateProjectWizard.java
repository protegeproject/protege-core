package edu.stanford.smi.protege.plugin;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CreateProjectWizard extends Wizard {
    private static final long serialVersionUID = -645454167907225242L;
    private CreateProjectPlugin plugin;
    private Project project;
    
    public CreateProjectWizard(JComponent owner) {
        super(owner, "Create New Project");
        addPage(new SelectProjectTypeWizardPage(this));
        setSize(new Dimension(400, 500));
    }
    
    public void setPlugin(CreateProjectPlugin plugin) {
        this.plugin = plugin;
    }
    
    public CreateProjectPlugin getPlugin() {
        return plugin;
    }
    public Project getProject() {
        return project;
    }
    
    public void onFinish() {
        super.onFinish();
        project = getPlugin().createProject();
    }
}

