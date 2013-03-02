package edu.stanford.smi.protege.code.generator.wrapping;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;


public class JavaCodeGeneratorAction extends AbstractAction {

	private static final long serialVersionUID = -916379802587626908L;

    public static final String JAVA_CODE_GEN_ACTION_NAME = "Generate Java Code...";

	private KnowledgeBase kb;

	public JavaCodeGeneratorAction(KnowledgeBase kb) {
		this.kb = kb;
	}

    public void actionPerformed(ActionEvent arg0) {
        EditableJavaCodeGeneratorOptions options = new ProjectBasedJavaCodeGeneratorOptions();
        JavaCodeGeneratorPanel panel = new JavaCodeGeneratorPanel(kb, options);

        ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();

        if (ModalDialog.showDialog(view , panel,
                getName(), ModalDialog.MODE_OK_CANCEL) == ModalDialog.OPTION_OK) {
            panel.ok();
            JavaCodeGenerator creator = new JavaCodeGenerator(kb, options);

            try {
                creator.createAll();
                ModalDialog.showMessageDialog(view, "Java code generated successfully in directory:\n" + options.getOutputFolder());
            }
            catch (Exception ex) {
                Log.getLogger().log(Level.SEVERE, "Exception caught", ex);
                ModalDialog.showMessageDialog(view, "Could not create Java code:\n" + ex.getMessage(), ModalDialog.MODE_CLOSE);
            }
        }
    }

    public String getName() {
        return JAVA_CODE_GEN_ACTION_NAME;
    }

}
