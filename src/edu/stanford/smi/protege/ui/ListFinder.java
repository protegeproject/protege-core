package edu.stanford.smi.protege.ui;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Implementation of Finder that will work on a generic JList.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ListFinder extends Finder {
    private static final long serialVersionUID = 220635605280533214L;
    protected JList _list;

    public ListFinder(JList list, ResourceKey key) {
        super(key);
        _list = list;
    }
    public ListFinder(JList list, String description, Icon icon) {
        super(description, icon);
        _list = list;
    }

    public ListFinder(JList list, String description) {
        this(list, description, Icons.getFindIcon());
    }

    protected int getBestMatch(List matches, String text) {
        return 0;
    }

    protected List getMatches(String text, int maxMatches) {
        if (!text.endsWith("*")) {
            text += "*";
        }

        StringMatcher matcher = new SimpleStringMatcher(text);
        List matchingInstances = new ArrayList();
        ListModel model = _list.getModel();
        int size = model.getSize();
        for (int i = 0; i < size; ++i) {
            Instance instance = (Instance) model.getElementAt(i);
            String browserText = instance.getBrowserText().toLowerCase();
            if (matcher.isMatch(browserText)) {
                matchingInstances.add(instance);
            }
        }
        return matchingInstances;
    }

    protected void select(Object o) {
        _list.setSelectedValue(o, true);
    }
}
