package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * A utility class that maintains a tree. Why isn't something like this in java.util?
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Tree implements Cloneable {
    private Object root;
    private Map nodeToChildrenMap = new LinkedHashMap();

    public Tree(Object root) {
        setRoot(root);
    }

    public Tree() {
    }

    public Object clone() {
        Tree tree = new Tree(root);
        Iterator i = nodeToChildrenMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Object node = entry.getKey();
            Set children = (Set) entry.getValue();
            tree.nodeToChildrenMap.put(node, new LinkedHashSet(children));
        }
        return tree;
    }

    public void setRoot(Object root) {
        // Log.getLogger().info("set root: " + root);
        this.root = root;
    }

    public void swapNode(Object oldNode, Object newNode) {
        if (root == oldNode) {
            root = newNode;
        }
        Set children = (Set) nodeToChildrenMap.remove(oldNode);
        if (children != null) {
            nodeToChildrenMap.put(newNode, children);
        }
        Iterator i = nodeToChildrenMap.values().iterator();
        while (i.hasNext()) {
            Set nodeChildren = (Set) i.next();
            boolean succeeded = nodeChildren.remove(oldNode);
            if (succeeded) {
                nodeChildren.add(newNode);
            }
        }
    }

    public Object getRoot() {
        return root;
    }

    public boolean isReachable(Object node) {
        return root == node || getDescendents(root).contains(node);
    }

    public void addChild(Object parent, Object child) {
        // Log.getLogger().info("add child: " + parent + " " + child);
        Set children = (Set) nodeToChildrenMap.get(parent);
        if (children == null) {
            children = new LinkedHashSet();
            nodeToChildrenMap.put(parent, children);
        }
        children.add(child);
    }

    public void removeChild(Object parent, Object child) {
        Set children = (Set) nodeToChildrenMap.get(parent);
        if (children == null) {
            logNoSuchChild(parent, child);
        } else {
            boolean succeeded = children.remove(child);
            if (!succeeded) {
                logNoSuchChild(parent, child);
            }
        }
    }

    public void removeNode(Object node) {
        if (root == node) {
            root = null;
        }
        nodeToChildrenMap.remove(node);
        Iterator i = nodeToChildrenMap.values().iterator();
        while (i.hasNext()) {
            Set children = (Set) i.next();
            children.remove(node);
        }
    }

    private static void logNoSuchChild(Object parent, Object child) {
        Log.getLogger().warning("No such child: " + parent + " " + child);
    }

    public Set getChildren(Object parent) {
        Set children = (Set) nodeToChildrenMap.get(parent);
        return (children == null) ? Collections.EMPTY_SET : Collections.unmodifiableSet(children);
    }

    public Set getNodeAndDescendents(Object parent) {
        Set descendents = getDescendents(parent);
        if (parent == null) {
            Log.getLogger().severe("Null parent");
        } else {
            descendents.add(parent);
        }
        return descendents;
    }

    public Set getDescendents(Object parent) {
        Set descendents = new LinkedHashSet();
        getDescendents(parent, descendents);
        return descendents;
    }

    private void getDescendents(Object parent, Set descendents) {
        Set children = (Set) nodeToChildrenMap.get(parent);
        if (children != null) {
            Iterator i = children.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                boolean changed = descendents.add(o);
                if (changed) {
                    getDescendents(o, descendents);
                }
            }
        }
    }

    public Set getNodes() {
        return getDescendents(root);
    }
}