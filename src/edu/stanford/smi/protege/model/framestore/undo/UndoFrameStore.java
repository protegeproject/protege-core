package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

public class UndoFrameStore extends ModificationFrameStore implements CommandManager {
    private static final int NO_COMMAND = -1;

    private Collection _listeners = new ArrayList();
    private List _commands = new ArrayList();
    private int _lastExecutedCommand = NO_COMMAND;
    private LinkedList _macroCommandList = new LinkedList();

    public void close() {
        super.close();
        _listeners = null;
        _commands = null;
        _macroCommandList = null;
    }

    private void addExecutedCommand(Command command) {
        if (_lastExecutedCommand != _commands.size() - 1) {
            _commands = new ArrayList(_commands.subList(0, _lastExecutedCommand + 1));
        }
        _commands.add(command);
        ++_lastExecutedCommand;
    }

    public void reinitialize() {
        _commands.clear();
        _macroCommandList.clear();
        _lastExecutedCommand = NO_COMMAND;
    }

    public UndoFrameStore() {
    }

    private Object execute(Command c) {
        Object o;
        if (isInTransaction()) {
            o = transactionCommandExecute(c);
        } else {
            o = simpleCommandExecute(c);
        }
        return o;
    }

    private Object simpleCommandExecute(Command c) {
        Object o = c.doIt();
        addExecutedCommand(c);
        notifyListeners();
        return o;
    }

    private Object transactionCommandExecute(Command c) {
        Object o = c.doIt();
        getCurrentMacro().add(c);
        return o;
    }

    private MacroCommand getCurrentMacro() {
        return (MacroCommand) (_macroCommandList.isEmpty() ? null : _macroCommandList.getLast());
    }

    private boolean isInTransaction() {
        return !_macroCommandList.isEmpty();
    }

    public boolean canUndo() {
        return _lastExecutedCommand >= 0;
    }

    public boolean canRedo() {
        return _lastExecutedCommand < _commands.size() - 1;
    }

    public void undo() {
        if (canUndo()) {
            Command command = (Command) _commands.get(_lastExecutedCommand);
            // Log.trace("undoing " + command.getDescription(), this, "undo");
            command.undoIt();
            --_lastExecutedCommand;
            notifyListeners();
        } else {
            Log.getLogger().warning("Not able to undo");
        }
    }

    public Command getUndoCommand() {
        return canUndo() ? (Command) _commands.get(_lastExecutedCommand) : null;
    }

    public Command getRedoCommand() {
        return canRedo() ? (Command) _commands.get(_lastExecutedCommand + 1) : null;
    }

    public void redo() {
        if (canRedo()) {
            ++_lastExecutedCommand;
            Command command = (Command) _commands.get(_lastExecutedCommand);
            // Log.trace("redoing " + command.getDescription(), this, "redo");
            command.redoIt();
            notifyListeners();
        } else {
            Log.getLogger().warning("Not able to redo");
        }
    }

    public Cls createCls(FrameID id, String name, Collection types, Collection superclasses, boolean loadDefaults) {
        Command cmd = new CreateClsCommand(getDelegate(), id, name, types, superclasses, loadDefaults);
        return (Cls) execute(cmd);
    }

    public Slot createSlot(FrameID id, String name, Collection types, Collection superslots, boolean loadDefaults) {
        Command cmd = new CreateSlotCommand(getDelegate(), id, name, types, superslots, loadDefaults);
        return (Slot) execute(cmd);
    }

    public Facet createFacet(FrameID id, String name, Collection types, boolean loadDefaults) {
        Command cmd = new CreateFacetCommand(getDelegate(), id, name, types, loadDefaults);
        return (Facet) execute(cmd);
    }

    public SimpleInstance createSimpleInstance(FrameID id, String name, Collection types, boolean loadDefaults) {
        Command cmd = new CreateSimpleInstanceCommand(getDelegate(), id, name, types, loadDefaults);
        return (SimpleInstance) execute(cmd);
    }

    public void deleteCls(Cls cls) {
        execute(new DeleteClsCommand(getDelegate(), cls));
    }

    public void deleteSlot(Slot slot) {
        execute(new DeleteSlotCommand(getDelegate(), slot));
    }

    public void deleteFacet(Facet facet) {
        execute(new DeleteFacetCommand(getDelegate(), facet));
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        execute(new DeleteSimpleInstanceCommand(getDelegate(), simpleInstance));
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        execute(new AddDirectTemplateSlotCommand(getDelegate(), cls, slot));
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        execute(new RemoveDirectTemplateSlotCommand(getDelegate(), slot, cls));
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        execute(new MoveDirectTemplateSlotCommand(getDelegate(), slot, cls, index));
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        execute(new SetDirectTemplateSlotCommand(getDelegate(), cls, values, slot));
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        execute(new SetDirectTemplateFacetValuesCommand(getDelegate(), slot, cls, facet, values));
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        execute(new SetDirectOwnSlotValuesCommand(getDelegate(), values, slot, frame));
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        execute(new MoveDirectOwnSlotValueCommand(getDelegate(), frame, slot, from, to));
    }

    public void setFrameName(Frame frame, String name) {
        execute(new SetFrameNameCommand(getDelegate(), name, frame));
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        execute(new AddDirectSuperclassCommand(getDelegate(), superclass, cls));
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        execute(new RemoveDirectSuperclassCommand(getDelegate(), superclass, cls));
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        execute(new MoveDirectSubclassCommand(getDelegate(), cls, index, subclass));
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        execute(new AddDirectSuperslotCommand(getDelegate(), superslot, slot));
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        execute(new RemoveDirectSuperslotCommand(getDelegate(), superslot, slot));
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        execute(new MoveDirectSubslotCommand(getDelegate(), slot, index, subslot));
    }

    public void addDirectType(Instance instance, Cls type) {
        execute(new AddDirectTypeCommand(getDelegate(), type, instance));
    }

    public void removeDirectType(Instance instance, Cls type) {
        execute(new RemoveDirectTypeCommand(getDelegate(), type, instance));
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        execute(new RemoveDirectTemplateFacetOverridesCommand(getDelegate(), cls, slot));
    }

    public boolean beginTransaction(String name) {
        // Log.enter(this, "beginTransaction", name);
        boolean ok = getDelegate().beginTransaction(name);
        pushTransaction(new MacroCommand(name, getDelegate()));
        return ok;
    }

    private void pushTransaction(MacroCommand newMacro) {
        MacroCommand currentMacro = getCurrentMacro();
        if (currentMacro == null) {
            addExecutedCommand(newMacro);
        } else {
            currentMacro.add(newMacro);
        }
        _macroCommandList.add(newMacro);
    }

    private MacroCommand popTransaction() {
        return (MacroCommand) _macroCommandList.removeLast();
    }

    public boolean commitTransaction() {
        popTransaction();
        boolean committed = getDelegate().commitTransaction();
        notifyListeners();
        return committed;
    }

    public boolean rollbackTransaction() {
        popTransaction();
        boolean succeeded = getDelegate().rollbackTransaction();
        if (!succeeded) {
            undo();
            _commands.remove(_lastExecutedCommand + 1);
        }
        return succeeded;
    }

    private void notifyListeners() {
        ChangeEvent event = new ChangeEvent(this);
        Iterator i = _listeners.iterator();
        while (i.hasNext()) {
            ChangeListener listener = (ChangeListener) i.next();
            listener.stateChanged(event);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        _listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        _listeners.remove(listener);
    }

    public Collection getDoneCommands() {
        Collection commands;
        if (_lastExecutedCommand >= 0) {
            commands = _commands.subList(0, _lastExecutedCommand + 1);
        } else {
            commands = Collections.EMPTY_LIST;
        }
        return commands;
    }

    public Collection getUndoneCommands() {
        Collection commands;
        if ((_lastExecutedCommand + 1) < _commands.size()) {
            commands = _commands.subList(_lastExecutedCommand + 1, _commands.size());
        } else {
            commands = Collections.EMPTY_LIST;
        }
        return commands;
    }
}