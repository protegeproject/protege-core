package edu.stanford.smi.protege.ui;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.ListCellRenderer;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.Validator;

/**
 * A bunch of generic utilities for popping up dialogs that allow the user to do something.  Most of these methods
 * all the user to pick "something" (a class, slot, instance). 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DisplayUtilities {

    public static String editString(final Component component, String label, Object startValue,
            final Validator validator) {
        final EditStringPanel panel = new EditStringPanel(startValue, label);

        ModalDialog.CloseCallback callback = new ModalDialog.CloseCallback() {
            public boolean canClose(int result) {
                boolean canClose;
                if (result == ModalDialog.OPTION_OK && validator != null) {
                    String text = panel.getText();
                    canClose = validator.isValid(text);
                    if (!canClose) {
                        ModalDialog.showMessageDialog(component, validator.getErrorMessage(text));
                    }
                } else {
                    canClose = true;
                }
                return canClose;
            }
        };
        String endValue = (startValue == null) ? null : startValue.toString();
        int result = ModalDialog.showDialog(component, panel, label, ModalDialog.MODE_OK_CANCEL, callback);
        if (result == ModalDialog.OPTION_OK) {
            endValue = panel.getText();
        }
        return endValue;
    }

    private static Collection getFirstTwoConcreteClses(KnowledgeBase kb, Collection allowedClses) {
        Collection concreteClses = new HashSet();
        if (allowedClses.isEmpty()) {
            allowedClses = kb.getRootClses();
        }
        getFirstTwoConcreteClses(allowedClses, concreteClses);
        return concreteClses;
    }

    private static void getFirstTwoConcreteClses(Collection allowedClses, Collection concreteClses) {
        Iterator i = allowedClses.iterator();
        while (i.hasNext() && concreteClses.size() != 2) {
            Cls cls = (Cls) i.next();
            if (cls.isConcrete()) {
                concreteClses.add(cls);
                if (concreteClses.size() == 2) {
                    break;
                }
            }
            getFirstTwoConcreteClses(cls.getDirectSubclasses(), concreteClses);
        }
    }

    public static boolean hasMultipleConcreteClses(KnowledgeBase kb, Collection allowedClses) {
        return allowedClses.isEmpty() || getFirstTwoConcreteClses(kb, allowedClses).size() == 2;
    }

    private static boolean hasOneClass(Collection rootClses) {
        boolean hasOneClass;
        if (rootClses.size() == 1) {
            Cls cls = (Cls) CollectionUtilities.getFirstItem(rootClses);
            hasOneClass = cls.getDirectSubclassCount() == 0;
        } else {
            hasOneClass = false;
        }
        return hasOneClass;
    }

    /**
     * @deprecated Use #pickCls(Component, KnowledgeBase, Collection)
     */
    public static Cls pickCls(Component component, Collection rootClses) {
        return pickCls(component, getKnowledgeBase(rootClses), rootClses, "Select Class");
    }

    public static Cls pickCls(Component component, KnowledgeBase kb, Collection rootClses) {
        return pickCls(component, kb, rootClses, "Select Class");
    }

    /**
     * @deprecated Use #pickCls(Component, KnowledgeBase, Collection, String)
     */
    public static Cls pickCls(Component component, Collection rootClses, String label) {
        return pickCls(component, getKnowledgeBase(rootClses), rootClses, label);
    }

    public static Cls pickCls(Component component, KnowledgeBase kb, Collection rootClses, String label) {
        return (Cls) CollectionUtilities.getFirstItem(pickClses(component, kb, rootClses, label, false));
    }

    public static Collection pickClses(Component component, KnowledgeBase kb) {
        return pickClses(component, kb, "Select Classes");
    }

    public static Collection pickClses(Component component, KnowledgeBase kb, String label) {
        Collection rootClses = CollectionUtilities.createCollection(kb.getRootCls());
        return pickClses(component, kb, rootClses, label);
    }

    /**
     * @deprecated Use #pickClses(Component, KnowledgeBase, Collection)
     */
    public static Collection pickClses(Component component, Collection rootClses) {
        return pickClses(component, getKnowledgeBase(rootClses), rootClses);
    }

    public static Collection pickClses(Component component, KnowledgeBase kb, Collection rootClses) {
        return pickClses(component, kb, rootClses, "Select Classes");
    }

    public static Collection pickClses(Component component, KnowledgeBase kb, Collection rootClses, String label) {
        return pickClses(component, kb, rootClses, label, true);
    }

    private static Collection pickClses(Component component, KnowledgeBase kb, Collection rootClses, String label,
            boolean multiple) {
        Collection clses;
        if (rootClses.isEmpty()) {
            clses = Collections.EMPTY_LIST;
        } else if (hasOneClass(rootClses)) {
            clses = rootClses;
        } else {
            SelectClsesPanel p = new SelectClsesPanel(kb, rootClses, multiple);
            int result = ModalDialog.showDialog(component, p, label, ModalDialog.MODE_OK_CANCEL);
            if (result == ModalDialog.OPTION_OK) {
                clses = p.getSelection();
            } else {
                clses = Collections.EMPTY_LIST;
            }
        }
        return clses;
    }

    public static Cls pickConcreteCls(Component component, KnowledgeBase kb, String label) {
        Collection rootClses = CollectionUtilities.createCollection(kb.getRootCls());
        return pickConcreteCls(component, kb, rootClses, label);
    }

    /**
     * @deprecated Use #pickConcreteCls(Component, KnowledgeBase kb, Collection)
     */
    public static Cls pickConcreteCls(Component component, Collection allowedClses) {
        return pickConcreteCls(component, allowedClses, "Select Concrete Cls");
    }

    public static Cls pickConcreteCls(Component component, KnowledgeBase kb, Collection allowedClses) {
        return pickConcreteCls(component, kb, allowedClses, "Select Concrete Cls");
    }

    /**
     * @deprecated Use #pickConcreteCls(Component, KnowledgeBase kb, Collection, String)
     */
    public static Cls pickConcreteCls(Component component, Collection allowedClses, String label) {
        return pickConcreteCls(component, getKnowledgeBase(allowedClses), allowedClses, label);
    }

    public static Cls pickConcreteCls(Component component, KnowledgeBase kb, Collection allowedClses, String label) {
        Cls cls;
        Collection concreteClses = getFirstTwoConcreteClses(kb, allowedClses);
        switch (concreteClses.size()) {
            case 0:
                ModalDialog.showMessageDialog(component, "There are no concrete allowed classes");
                cls = null;
                break;
            case 1:
                cls = (Cls) CollectionUtilities.getFirstItem(concreteClses);
                break;
            case 2:
                cls = promptForConcreteCls(component, kb, allowedClses, label);
                break;
            default:
                Assert.fail("bad size: " + concreteClses.size());
                cls = null;
                break;
        }
        return cls;
    }

    public static Cls pickForm(Component component, Project project) {
        return pickForm(component, project, "Select Prototype Form");
    }

    public static Cls pickForm(Component component, Project project, String label) {
        Cls cls;
        SelectClsesPanel p = new SelectClsesPanel(project.getKnowledgeBase(), new FormRenderer(project));
        int result = ModalDialog.showDialog(component, p, label, ModalDialog.MODE_OK_CANCEL);
        if (result == ModalDialog.OPTION_OK) {
            cls = (Cls) CollectionUtilities.getFirstItem(p.getSelection());
        } else {
            cls = null;
        }
        return cls;
    }

    public static Instance pickInstance(Component component, KnowledgeBase kb) {
        Collection allowedClses = Collections.singleton(kb.getRootCls());
        return pickInstance(component, allowedClses, "Select Instance");
    }

    public static Instance pickInstance(Component component, Collection allowedClses) {
        return pickInstance(component, allowedClses, "Select Instance");
    }

    public static Instance pickInstance(Component component, Collection allowedClses, String label) {
        return (Instance) CollectionUtilities.getFirstItem(pickInstances(component, allowedClses, label));
    }

    public static Instance pickInstanceFromCollection(Component component, 
                                                      Collection collection, 
                                                      int initialSelection,
                                                      String label) {
        return pickInstanceFromCollection(component, collection, initialSelection, label, null);
    }
    
    public static Instance pickInstanceFromCollection(Component component, 
                                                      Collection collection, 
                                                      int initialSelection,
                                                      String label,
                                                      ListCellRenderer cellRenderer) {
        Instance instance;
        SelectInstanceFromCollectionPanel panel = new SelectInstanceFromCollectionPanel(collection, initialSelection);
        if (cellRenderer != null) {
            panel.setCellRenderer(cellRenderer);
        }
        int result = ModalDialog.showDialog(component, panel, label, ModalDialog.MODE_OK_CANCEL);
        if (result == ModalDialog.OPTION_OK) {
            instance = panel.getSelection();
        } else {
            instance = null;
        }
        return instance;
    }

    /**
     * @deprecated Use #pickInstance(Component, KnowledgeBase, Collection)
     */
    public static Collection pickInstances(Component component, Collection allowedClses) {
        return pickInstances(component, allowedClses, "Select Instances");
    }

    public static Collection pickInstances(Component component, KnowledgeBase kb, Collection allowedClses) {
        return pickInstances(component, kb, allowedClses, "Select Instances");
    }

    /**
     * deprecated Use #pickInstances(Component, KnowldgeBase, Collection, String)
     */
    public static Collection pickInstances(Component component, Collection allowedClses, String label) {
        KnowledgeBase kb = getKnowledgeBase(allowedClses);
        return pickInstances(component, kb, allowedClses, label);
    }

    private static KnowledgeBase getKnowledgeBase(Collection allowedClses) {
        KnowledgeBase kb;
        Cls cls = (Cls) CollectionUtilities.getFirstItem(allowedClses);
        if (cls == null) {
            // we have to hack it
            kb = ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase();
        } else {
            kb = cls.getKnowledgeBase();
        }
        return kb;
    }

    public static Collection pickInstances(Component component, KnowledgeBase kb, Collection allowedClses, String label) {
        Collection instances = Collections.EMPTY_LIST;
        SelectInstancesPanel panel = new SelectInstancesPanel(kb, allowedClses);
        int result = ModalDialog.showDialog(component, panel, label, ModalDialog.MODE_OK_CANCEL);
        if (result == ModalDialog.OPTION_OK) {
            instances = panel.getSelection();
        }
        return instances;
    }

    public static Collection pickInstancesFromCollection(Component component, Collection instances, String label) {
        Collection selectedSlots = Collections.EMPTY_LIST;
        SelectInstancesFromCollectionPanel panel = new SelectInstancesFromCollectionPanel(instances);
        int result = ModalDialog.showDialog(component, panel, label, ModalDialog.MODE_OK_CANCEL);
        switch (result) {
            case ModalDialog.OPTION_OK:
                selectedSlots = panel.getSelection();
                break;
            case ModalDialog.OPTION_CANCEL:
                break;
            default:
                Assert.fail("bad result: " + result);
                break;
        }
        return selectedSlots;
    }

    public static Slot pickSlot(Component component, Collection slots) {
        return pickSlot(component, slots, "Select Slot");
    }

    public static Slot pickSlot(Component component, Collection slots, String label) {
        return (Slot) CollectionUtilities.getFirstItem(pickInstancesFromCollection(component, slots, label));
    }

    public static Collection pickSlots(Component component, Collection slots) {
        return pickInstancesFromCollection(component, slots, "Select Slots");
    }

    public static Collection pickSlots(Component component, Collection slots, String label) {
        return pickInstancesFromCollection(component, slots, label);
    }

    public static Object pickSymbol(Component component, String label, Object initialValue, Collection allowedValues) {
        Object value = initialValue;
        PickSymbolPanel panel = new PickSymbolPanel(label, initialValue, allowedValues);
        int result = ModalDialog.showDialog(component, panel, label, ModalDialog.MODE_OK_CANCEL);
        if (result == ModalDialog.OPTION_OK) {
            value = panel.getSelectedValue();
        }
        return value;
    }

    private static Cls promptForConcreteCls(final Component component, KnowledgeBase kb, Collection clses,
            final String label) {
        final SelectClsesPanel p = new SelectClsesPanel(kb, clses);

        ModalDialog.CloseCallback callback = new ModalDialog.CloseCallback() {
            public boolean canClose(int result) {
                boolean canClose;
                if (result == ModalDialog.OPTION_OK) {
                    Cls cls = (Cls) CollectionUtilities.getFirstItem(p.getSelection());
                    canClose = cls != null && cls.isConcrete();
                    if (!canClose) {
                        ModalDialog.showMessageDialog(component, label);
                    }
                } else {
                    canClose = true;
                }
                return canClose;
            }
        };

        Cls cls;
        int result = ModalDialog.showDialog(component, p, label, ModalDialog.MODE_OK_CANCEL, callback);
        if (result == ModalDialog.OPTION_OK) {
            cls = (Cls) CollectionUtilities.getFirstItem(p.getSelection());
        } else {
            cls = null;
        }
        return cls;
    }
}
