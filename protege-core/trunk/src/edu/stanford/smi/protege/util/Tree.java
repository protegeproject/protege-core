package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * A utility class that maintains a tree. Why isn't something like this in java.util?
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Tree<X> implements Cloneable {
    private X root;
    private Map<X,Set<X>> nodeToChildrenMap = new LinkedHashMap<X,Set<X>>();

    public Tree(X root) {
        setRoot(root);
    }

    public Tree() {
    }

    public Tree<X> clone() {
        Tree<X> tree = new Tree<X>(root);
        Iterator<Map.Entry<X,Set<X>>> i = nodeToChildrenMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<X,Set<X>> entry = i.next();
            X node = entry.getKey();
            Set<X> children = entry.getValue();
            tree.nodeToChildrenMap.put(node, new LinkedHashSet<X>(children));
        }
        return tree;
    }

    public void setRoot(X root) {
        // Log.getLogger().info("set root: " + root);
        this.root = root;
    }

    public void swapNode(X oldNode, X newNode) {
        if (root == oldNode) {
            root = newNode;
        }
        Set<X> children = nodeToChildrenMap.remove(oldNode);
        if (children != null) {
            nodeToChildrenMap.put(newNode, children);
        }
        Iterator<Set<X>> i = nodeToChildrenMap.values().iterator();
        while (i.hasNext()) {
            Set<X> nodeChildren = i.next();
            boolean succeeded = nodeChildren.remove(oldNode);
            if (succeeded) {
                nodeChildren.add(newNode);
            }
        }
    }

    public X getRoot() {
        return root;
    }

    public boolean isReachable(X node) {
        return root == node || getDescendents(root).contains(node);
    }

    public void addChild(X parent, X child) {
        if (parent == null) {
            throw new IllegalArgumentException("Null parent");
        }
        if (child == null) {
            throw new IllegalArgumentException("Null child");
        }
        // Log.getLogger().info("add child: " + parent + " " + child);
        Set<X> children = nodeToChildrenMap.get(parent);
        if (children == null) {
            children = new LinkedHashSet<X>();
            nodeToChildrenMap.put(parent, children);
        }
        children.add(child);
    }

    public void removeChild(X parent, X child) {
        Set<X> children =  nodeToChildrenMap.get(parent);
        if (children == null) {
            logNoSuchChild(parent, child);
        } else {
            boolean succeeded = children.remove(child);
            if (!succeeded) {
                logNoSuchChild(parent, child);
            }
        }
    }

    public void removeNode(X node) {
        if (root == node) {
            root = null;
        }
        nodeToChildrenMap.remove(node);
        Iterator<Set<X>> i = nodeToChildrenMap.values().iterator();
        while (i.hasNext()) {
            Set children = i.next();
            children.remove(node);
        }
    }

    private static void logNoSuchChild(Object parent, Object child) {
        Log.getLogger().warning("No such child: " + parent + " " + child);
    }

    public Set<X> getChildren(X parent) {
        Set<X> children = nodeToChildrenMap.get(parent);
        return (Set<X>)((children == null) ?  Collections.EMPTY_SET : Collections.unmodifiableSet(children));
    }

    public Set<X> getNodeAndDescendents(X parent) {
        Set<X> descendents = getDescendents(parent);
        if (parent == null) {
            Log.getLogger().severe("Null parent");
        } else {
            descendents.add(parent);
        }
        return descendents;
    }

    public Set<X> getDescendents(X parent) {
        Set<X> descendents = new LinkedHashSet<X>();
        getDescendents(parent, descendents);
        return descendents;
    }

    private void getDescendents(X parent, Set<X> descendents) {
        Set<X> children =  nodeToChildrenMap.get(parent);
        if (children != null) {
            Iterator<X> i = children.iterator();
            while (i.hasNext()) {
                X o = i.next();
                boolean changed = descendents.add(o);
                if (changed) {
                    getDescendents(o, descendents);
                }
            }
        }
    }

    public Set<X> getNodes() {
        return getDescendents(root);
    }
}