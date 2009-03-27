package edu.stanford.smi.protege.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Tim's rule of comments: big comments mean trouble.
 * 
 * Be careful of deadlock.  Actually I am not sure this is a problem
 * but it made me nervous so I was careful.  The approach that I took
 * is to synchronize the inner list and the read methods. If there is
 * only one writer then I don't have to worry about things changing
 * underneath the writer.  The fact that the list is synchronized
 * means that readers are safe accessing the list.
 * 
 * The world could change underneath the reader while he is trying to
 * find something. But synchronizing the reader methods protect the
 * reader from change.  These methods are safe to synchronize because
 * they do not make calls such as fire... which might take who knows
 * what locks.
 * 
 * I don't need to synchronize all the reader calls but it seems safer
 * since there is little or no performance penalty.
 */

/**
 * This class is a trivial extension of the SimpleListModel.  This class protects the list in the 
 * SimpleListModel from concurrent access.  It assumes that there is one writer and possibly many readers.
 */

public class ConcurrentListModel extends SimpleListModel {
    private static final long serialVersionUID = -1544733464161201247L;

    @SuppressWarnings("unchecked")
    protected List makeList() {
        return Collections.synchronizedList(new ArrayList());
    }
    
    @Override
    public boolean contains(Object o) {
        synchronized (getList()) {
            return super.contains(o);
        }
    }
    
    @Override
    public Object getElementAt(int i) {
        synchronized (getList()) {
            return super.getElementAt(i);
        }
    }
    
    @Override
    public int getSize() {
        synchronized (getList()) {
            return super.getSize();
        }
    }
    
    @Override
    public List getValues() {
        synchronized (getList()) {
            return super.getValues();
        }
    }
    
    @Override
    public int indexOf(Object o) {
        synchronized (getList()) {
            return super.indexOf(o);
        }
    }
}
