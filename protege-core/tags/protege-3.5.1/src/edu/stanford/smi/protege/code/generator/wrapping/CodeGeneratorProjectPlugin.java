package edu.stanford.smi.protege.code.generator.wrapping;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;

public class CodeGeneratorProjectPlugin extends ProjectPluginAdapter {

	public static final String CODE_MENU = "Code";

	@Override
	public void afterShow(ProjectView view, ProjectToolBar toolBar,
			ProjectMenuBar menuBar) {

		KnowledgeBase kb = view.getProject().getKnowledgeBase();
		if (kb.getKnowledgeBaseFactory().getClass().getName().contains(".owl.")) {
			//try to filter out owl backends
			return;
		}

		insertCodeMenu(kb, menuBar);
	}

	private void insertCodeMenu(KnowledgeBase kb, ProjectMenuBar menuBar) {
		JMenu codeMenu = ComponentUtilities.getMenu(menuBar, CODE_MENU, true, menuBar.getComponentCount() - 2);
		JMenuItem codeGen = ComponentFactory.createMenuItem(JavaCodeGeneratorAction.JAVA_CODE_GEN_ACTION_NAME);
		codeGen.addActionListener(new JavaCodeGeneratorAction(kb));
		codeMenu.add(codeGen);
	}

	@Override
	public void beforeHide(ProjectView view, ProjectToolBar toolBar,
			ProjectMenuBar menuBar) {

		JMenu codeMenu = ComponentUtilities.getMenu(menuBar, CODE_MENU);
		if (codeMenu == null) {
			return;
		}
		ComponentUtilities.removeMenuItem(codeMenu, JavaCodeGeneratorAction.JAVA_CODE_GEN_ACTION_NAME);
	}

}
