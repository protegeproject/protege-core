package edu.stanford.smi.protege.ui;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class FrameTreeFinder extends Finder {
    private JTree tree;
    private KnowledgeBase knowledgeBase;

    protected FrameTreeFinder(KnowledgeBase knowledgeBase, JTree tree, ResourceKey key) {
        super(key);
        this.tree = tree;
        this.knowledgeBase = knowledgeBase;
    }

    protected FrameTreeFinder(KnowledgeBase knowledgeBase, JTree tree, String description) {
        super(description);
        this.tree = tree;
        this.knowledgeBase = knowledgeBase;
    }

    protected FrameTreeFinder(KnowledgeBase knowledgeBase, JTree tree, String description, Icon icon) {
        super(description, icon);
        this.tree = tree;
        this.knowledgeBase = knowledgeBase;
    }

    protected int getBestMatch(List matches, String text) {
        int result = Collections.binarySearch(matches, text, new BrowserComparator());
        if (result < 0) {
            int index = -(result + 1);
            if (index < matches.size()) {
                Instance instance = (Instance) matches.get(index);
                String browserText = instance.getBrowserText().toLowerCase();
                if (browserText.startsWith(text.toLowerCase())) {
                    result = index;
                }
            }
        }
        return result;
    }

    protected void select(Object o) {
        WaitCursor cursor = new WaitCursor(this);
        Frame frame = (Frame) o;
        ArrayList frames = new ArrayList();
        getVisiblePathToRoot(frame, frames);
        Collections.reverse(frames);
        ComponentUtilities.setSelectedObjectPath(tree, frames);
        cursor.hide();
    }

    protected List getMatches(String text, int maxMatches) {
        Cls kbRoot = knowledgeBase.getRootCls();
        List matches = getMatchingFrames(text, maxMatches);
        LazyTreeRoot root = (LazyTreeRoot) tree.getModel().getRoot();
        Set rootNodes = new HashSet((Collection) root.getUserObject());
        if (rootNodes.size() != 1 || !equals(CollectionUtilities.getFirstItem(rootNodes), kbRoot)) {
            // Log.trace("removing bad matches", this, "getMatches");
            Iterator i = matches.iterator();
            while (i.hasNext()) {
                Frame frame = (Frame) i.next();
                boolean isValid = rootNodes.contains(frame);
                if (!isValid) {
                    Collection parents = new HashSet(getAncestors(frame));
                    isValid = parents.removeAll(rootNodes);
                }
                if (!isValid) {
                    i.remove();
                }
            }
        }
        Collections.sort(matches, new FrameComparator());
        return matches;
    }

    protected List getMatchingFrames(String text, int maxMatches) {
        if (!text.endsWith("*")) {
            text += '*';
        }
        Slot slot = getBrowserSlot(knowledgeBase);
        List matches = new ArrayList(knowledgeBase.getMatchingFrames(slot, null, false, text,
                maxMatches));
        Iterator i = matches.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            if (!isCorrectType(frame)) {
                i.remove();
            }
        }
        return matches;
    }

    protected void getVisiblePathToRoot(Frame frame, Collection path) {
        Collection roots = new ArrayList((Collection) ((LazyTreeNode) tree.getModel().getRoot())
                .getUserObject());
        Iterator i = roots.iterator();
        while (i.hasNext()) {
            Frame root = (Frame) i.next();
            if (!root.isVisible()) {
                i.remove();
            }
        }
        path.add(frame);
        if (!roots.contains(frame)) {
            boolean succeeded = getVisiblePathToRoot(frame, roots, path);
            if (!succeeded) {
                Log.getLogger().warning("No visible path found for " + frame);
            }
        }
    }

    protected boolean getVisiblePathToRoot(Frame frame, Collection roots, Collection path) {
        boolean found = false;
        Iterator i = getParents(frame).iterator();
        while (i.hasNext() && !found) {
            Frame parent = (Frame) i.next();
            if (parent.isVisible()) {
                path.add(parent);
                if (roots.contains(parent)) {
                    found = true;
                } else {
                    found = getVisiblePathToRoot(parent, roots, path);
                }
                if (!found) {
                    path.remove(parent);
                }
            }
        }
        return found;
    }

    protected abstract Collection getParents(Frame frame);

    protected abstract Collection getAncestors(Frame frame);

    protected abstract boolean isCorrectType(Frame frame);

    protected abstract Slot getBrowserSlot(KnowledgeBase kb);

}