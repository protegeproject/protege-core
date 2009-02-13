package edu.stanford.smi.protege.model.framestore.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.CommandManager;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.ModificationFrameStore;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class UndoFrameStore extends ModificationFrameStore implements CommandManager {
    Logger log = Log.getLogger(UndoFrameStore.class);
    
    private static final int NO_COMMAND = -1;

    private Collection _listeners = new ArrayList();
    private List _commands = new ArrayList();
    private int _lastExecutedCommand = NO_COMMAND;
    private LinkedList _macroCommandList = new LinkedList();
    
    /*
     * If the delegate does not support transactions then the undo manager will do the
     * best that it can at the task.
     */
    private TransactionMonitor transactionMonitor = new TransactionMonitor() {

        @Override
        public TransactionIsolationLevel getTransationIsolationLevel() throws TransactionException {
            return TransactionIsolationLevel.READ_UNCOMMITTED;
        }

        @Override
        public void setTransactionIsolationLevel(TransactionIsolationLevel level) throws TransactionException {
            // TODO Auto-generated method stub
            throw new TransactionException("Cannot set transaction isolation level using undo");
        }
        
    };

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
            if (log.isLoggable(Level.FINE)) {
                log.fine("undoing " + command.getDescription());
            }
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
            if (log.isLoggable(Level.FINE)) {
                log.fine("redoing " + command.getDescription());
            }
            command.redoIt();
            notifyListeners();
        } else {
            Log.getLogger().warning("Not able to redo");
        }
    }

    public Cls createCls(FrameID id, Collection types, Collection superclasses, boolean loadDefaults) {
        Command cmd = new CreateClsCommand(getDelegate(), id, types, superclasses, loadDefaults);
        return (Cls) execute(cmd);
    }

    public Slot createSlot(FrameID id, Collection types, Collection superslots, boolean loadDefaults) {
        Command cmd = new CreateSlotCommand(getDelegate(), id, types, superslots, loadDefaults);
        return (Slot) execute(cmd);
    }

    public Facet createFacet(FrameID id, Collection types, boolean loadDefaults) {
        Command cmd = new CreateFacetCommand(getDelegate(), id, types, loadDefaults);
        return (Facet) execute(cmd);
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection types, boolean loadDefaults) {
        Command cmd = new CreateSimpleInstanceCommand(getDelegate(), id, types, loadDefaults);
        return (SimpleInstance) execute(cmd);
    }

    public void deleteCls(Cls cls) {
        execute(new DeleteClsCommand(getDelegate(), cls));
    }
    
    /*
     * There is a problem with both delete Slot and delete facet.  I don't know how to save and restore 
     * deleted facet values.  So some information is lost on the undo.
     */

    public void deleteSlot(Slot slot) {
        execute(new DeleteSlotCommand(getDelegate(), slot));
    }

    public void deleteFacet(Facet facet) {
        execute(new DeleteFrameCommand(getDelegate(), facet));
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        execute(new DeleteFrameCommand(getDelegate(), simpleInstance));
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

    public void moveDirectType(Instance instance, Cls type, int index) {
        execute(new MoveDirectTypeCommand(getDelegate(), instance, type, index));
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        execute(new RemoveDirectTemplateFacetOverridesCommand(getDelegate(), cls, slot));
    }
    
    public void replaceFrame(Frame original, Frame replacement) {
        execute(new ReplaceFrameCommand(getDelegate(), original, replacement));
    }

    public boolean beginTransaction(String name) {
        // Log.enter(this, "beginTransaction", name);
        boolean ok = getDelegate().beginTransaction(name);
        transactionMonitor.beginTransaction();
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
        transactionMonitor.commitTransaction();
        notifyListeners();
        return committed;
    }

    public boolean rollbackTransaction() {
        popTransaction();
        boolean succeeded = getDelegate().rollbackTransaction();
        transactionMonitor.rollbackTransaction();
        if (!succeeded) {
            undo();
            _commands.remove(_lastExecutedCommand + 1);
        }
        return succeeded;
    }
    
    public TransactionMonitor getTransactionStatusMonitor() {
        TransactionMonitor delegateMonitor = getDelegate().getTransactionStatusMonitor();
        if (delegateMonitor == null || 
                delegateMonitor.getTransationIsolationLevel() == TransactionIsolationLevel.NONE) {
            return transactionMonitor;
        }
        else {
            return getDelegate().getTransactionStatusMonitor();
        }
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