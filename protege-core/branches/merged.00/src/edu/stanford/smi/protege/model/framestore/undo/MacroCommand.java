package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MacroCommand implements Command {
    private List _commands = new ArrayList();
    private String _description;
    private FrameStore _delegate;

    public MacroCommand(String s, FrameStore fs) {
        _description = s;
        _delegate = fs;
    }

    public void add(Command command) {
        _commands.add(command);
    }

    public Object doIt() {
        throw new IllegalStateException();
    }

    public void undoIt() {
        ListIterator i = _commands.listIterator(_commands.size());
        //ESCA-JAVA0281 
        while (i.hasPrevious()) {
            Command command = (Command) i.previous();
            // Log.trace("macro undoing: " + command.getDescription(), this, "undoIt");
            command.undoIt();
        }
    }

    public void redoIt() {
        _delegate.beginTransaction(_description);
        ListIterator i = _commands.listIterator();
        while (i.hasNext()) {
            Command command = (Command) i.next();
            // Log.trace("macro redoing: " + command.getDescription(), this, "redoIt");
            command.redoIt();
        }
        _delegate.commitTransaction();
    }

    public String getDescription() {
        return _description;
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }

}
