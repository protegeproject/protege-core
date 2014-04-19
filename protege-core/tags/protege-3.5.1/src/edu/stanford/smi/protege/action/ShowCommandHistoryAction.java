package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.ui.*;

/**
 *  Show a panel displaying the previously executed commands.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ShowCommandHistoryAction extends ProjectAction {

    private static final long serialVersionUID = 807631758311720753L;

    public ShowCommandHistoryAction() {
        super(ResourceKey.COMMAND_HISTORY_ACTION);
    }

    public void actionPerformed(ActionEvent event) {
        CommandManager manager = getCommandManager();
        if (manager != null) {
            JPanel panel = new CommandHistoryPanel(manager);
            String title = LocalizedText.getText(ResourceKey.COMMAND_HISTORY_DIALOG_TITLE);
            ModalDialog.showDialog(getMainPanel(), panel, title, ModalDialog.MODE_CLOSE);
        }
    }
}
