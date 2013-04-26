package edu.stanford.smi.protege.util;

import java.io.*;
import java.util.*;

/**
 * A list the provides fast access to its elements.  This code was copied from the JDK LinkedList class and 
 * hacked. JDK 1.4 offers a standard version of this class which we will soon switch to.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 * @deprecated Use ArrayList instead.
 */
public class HashList extends AbstractSequentialList implements Cloneable, Serializable {
    private static final long serialVersionUID = 8024066403796069628L;
    private transient Entry header = new Entry(null, null, null);
    private transient int size = 0;
    private Map _valueEntryMap = new HashMap();
    private List _unmodifiableList;

    private class ListItr implements ListIterator {
        private Entry lastReturned = header;
        private Entry next;
        private int nextIndex;
        private int expectedModCount = getModCount();

        ListItr(int index) {
            if (index < 0 || index > size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            if (index < size / 2) {
                next = header.next;
                for (nextIndex = 0; nextIndex < index; nextIndex++) {
                    next = next.next;
                }
            } else {
                next = header;
                for (nextIndex = size; nextIndex > index; nextIndex--) {
                    next = next.previous;
                }
            }
        }

        public boolean hasNext() {
            return nextIndex != size;
        }

        public Object next() {
            checkForComodification();
            if (nextIndex == size) {
                throw new NoSuchElementException();
            }

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.element;
        }

        public boolean hasPrevious() {
            return nextIndex != 0;
        }

        public Object previous() {
            if (nextIndex == 0) {
                throw new NoSuchElementException();
            }

            lastReturned = next = next.previous;
            nextIndex--;
            checkForComodification();
            return lastReturned.element;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            HashList.this.remove(lastReturned);
            if (next == lastReturned) {
                next = lastReturned.next;
            } else {
                nextIndex--;
            }
            lastReturned = header;
            expectedModCount++;
        }

        public void set(Object o) {
            if (lastReturned == header) {
                throw new IllegalStateException();
            }
            checkForComodification();
            removeHashEntry(lastReturned);
            lastReturned.element = o;
            addHashEntry(lastReturned);
        }

        public void add(Object o) {
            checkForComodification();
            lastReturned = header;
            addBefore(o, next);
            nextIndex++;
            expectedModCount++;
        }

        final void checkForComodification() {
            if (getModCount() != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private static class Entry implements Serializable {
        private static final long serialVersionUID = -2080231082256914843L;
        Object element;
        Entry next;
        Entry previous;

        Entry(Object element, Entry next, Entry previous) {
            this.element = element;
            this.next = next;
            this.previous = previous;
        }
    }

    /**
     *  Constructs an empty list.
     */
    public HashList() {
        header.next = header.previous = header;
    }

    /**
     *  Constructs a list containing the elements of the specified collection,
     *  in the order they are returned by the collection's iterator.
     *
     * @param  c  Description of Parameter
     */
    public HashList(Collection c) {
        this();
        addAll(c);
    }

    /**
     *  Inserts the specified element at the specified position in this list.
     *  Shifts the element currently at that position (if any) and any
     *  subsequent elements to the right (adds one to their indices).
     *
     * @param  index    index at which the specified element is to be inserted.
     * @param  element  element to be inserted.
     * @throws          IndexOutOfBoundsException if the specified index is out
     *      of range ( <tt>index &lt; 0 || index &gt; size()</tt> ).
     */
    public void add(int index, Object element) {
        addBefore(element, (index == size ? header : entry(index)));
    }

    /**
     *  Appends the specified element to the end of this list.
     *
     * @param  o  element to be appended to this list.
     * @return    <tt>true</tt> (as per the general contract of <tt>
     *      Collection.add</tt> ).
     */
    public boolean add(Object o) {
        addBefore(o, header);
        return true;
    }

    /**
     *  Inserts all of the elements in the specified collection into this list,
     *  starting at the specified position. Shifts the element currently at that
     *  position (if any) and any subsequent elements to the right (increases
     *  their indices). The new elements will appear in the list in the order
     *  that they are returned by the specified collection's iterator.
     *
     * @param  index  index at which to insert first element from the specified
     *      collection.
     * @param  c      elements to be inserted into this list.
     * @throws        IndexOutOfBoundsException if the specified index is out of
     *      range ( <tt>index &lt; 0 || index &gt; size()</tt> ).
     * @return        Description of the Returned Value
     */
    public boolean addAll(int index, Collection c) {
        int numNew = c.size();
        if (numNew == 0) {
            return false;
        }
        modCount++;

        Entry successor = (index == size ? header : entry(index));
        Entry predecessor = successor.previous;
        Iterator it = c.iterator();
        for (int i = 0; i < numNew; i++) {
            Entry e = newEntry(it.next(), successor, predecessor);
            predecessor.next = e;
            predecessor = e;
        }
        successor.previous = predecessor;

        size += numNew;
        return true;
    }

    /**
     *  Appends all of the elements in the specified collection to the end of
     *  this list, in the order that they are returned by the specified
     *  collection's iterator. The behavior of this operation is undefined if
     *  the specified collection is modified while the operation is in progress.
     *  (This implies that the behavior of this call is undefined if the
     *  specified Collection is this list, and this list is nonempty.)
     *
     * @param  c      elements to be inserted into this list.
     * @throws        IndexOutOfBoundsException if the specified index is out of
     *      range ( <tt>index &lt; 0 || index &gt; size()</tt> ).
     * @return        Description of the Returned Value
     */
    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    private Entry addBefore(Object o, Entry e) {
        Entry newEntry = newEntry(o, e, e.previous);
        newEntry.previous.next = newEntry;
        newEntry.next.previous = newEntry;
        size++;
        modCount++;
        return newEntry;
    }

    /**
     *  Inserts the given element at the beginning of this list.
     *
     * @param  o  The feature to be added to the First attribute
     */
    public void addFirst(Object o) {
        addBefore(o, header.next);
    }

    private void addHashEntry(Entry e) {
        Object o = e.element;
        Object entryOrList = _valueEntryMap.get(o);
        if (entryOrList == null) {
            _valueEntryMap.put(o, e);
        } else if (entryOrList instanceof List) {
            ((List) entryOrList).add(e);
        } else {
            Collection c = new ArrayList();
            c.add(entryOrList);
            c.add(e);
            _valueEntryMap.put(o, c);
        }
    }

    /**
     *  Appends the given element to the end of this list. (Identical in
     *  function to the <tt>add</tt> method; included only for consistency.)
     *
     * @param  o  The feature to be added to the Last attribute
     */
    public void addLast(Object o) {
        addBefore(o, header);
    }

    /**
     *  Removes all of the elements from this list.
     */
    public void clear() {
        modCount++;
        header.next = header.previous = header;
        _valueEntryMap.clear();
        size = 0;
    }

    /**
     *  Returns a shallow copy of this <tt>LinkedList</tt> . (The elements
     *  themselves are not cloned.)
     *
     * @return    a shallow copy of this <tt>LinkedList</tt> instance.
     */
    public Object clone() {
        return new LinkedList(this);
    }

    /**
     *  Returns <tt>true</tt> if this list contains the specified element. More
     *  formally, returns <tt>true</tt> if and only if this list contains at
     *  least one element <tt>e</tt> such that <tt>(o==null ? e==null :
     *  o.equals(e))</tt> .
     *
     * @param  o  element whose presence in this list is to be tested.
     * @return    <tt>true</tt> if this list contains the specified element.
     */
    public boolean contains(Object o) {
        // return indexOf(o) != -1;
        return containsHashKey(o);
    }

    // <Object, Entry or List<Entry> >
    private boolean containsHashKey(Object o) {
        return _valueEntryMap.containsKey(o);
    }

    /**
     *  Return the indexed entry.
     *
     * @return        Description of the Returned Value
     * @param  index  Description of Parameter
     */
    private Entry entry(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Entry e = header;
        if (index < size / 2) {
            for (int i = 0; i <= index; i++) {
                e = e.next;
            }
        } else {
            for (int i = size; i > index; i--) {
                e = e.previous;
            }
        }
        return e;
    }

    // Positional Access Operations
    /**
     *  Returns the element at the specified position in this list.
     *
     * @param  index  index of element to return.
     * @return        the element at the specified position in this list.
     * @throws        IndexOutOfBoundsException if the specified index is is out
     *      of range ( <tt>index &lt; 0 || index &gt;= size()</tt> ).
     */
    public Object get(int index) {
        return entry(index).element;
    }

    /**
     *  Returns the first element in this list.
     *
     * @return    the first element in this list.
     */
    public Object getFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        return header.next.element;
    }

    private Entry getFirstHashEntry(Object o) {
        Entry entry;
        Object entryOrList = _valueEntryMap.get(o);
        if (entryOrList == null) {
            entry = null;
        } else if (entryOrList instanceof List) {
            List c = (List) entryOrList;
            entry = (Entry) c.get(0);
        } else {
            entry = (Entry) entryOrList;
        }
        return entry;
    }

    /**
     *  Returns the last element in this list.
     *
     * @return    the last element in this list.
     * @throws    NoSuchElementException if this list is empty.
     */
    public Object getLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        return header.previous.element;
    }

    private int getModCount() {
        return modCount;
    }

    public List getUnmodifiableList() {
        if (_unmodifiableList == null) {
            _unmodifiableList = Collections.unmodifiableList(this);
        }
        return _unmodifiableList;
    }

    // Search Operations
    /**
     *  Returns the index in this list of the first occurrence of the specified
     *  element, or -1 if the List does not contain this element. More formally,
     *  returns the lowest index i such that <tt>(o==null ? get(i)==null :
     *  o.equals(get(i)))</tt> , or -1 if there is no such index.
     *
     * @param  o  element to search for.
     * @return    the index in this list of the first occurrence of the
     *      specified element, or -1 if the list does not contain this element.
     */
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Entry e = header.next; e != header; e = e.next) {
                if (e.element == null) {
                    return index;
                }
                index++;
            }
        } else {
            for (Entry e = header.next; e != header; e = e.next) {
                if (o.equals(e.element)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    /**
     *  Returns the index in this list of the last occurrence of the specified
     *  element, or -1 if the list does not contain this element. More formally,
     *  returns the highest index i such that <tt>(o==null ? get(i)==null :
     *  o.equals(get(i)))</tt> , or -1 if there is no such index.
     *
     * @param  o  element to search for.
     * @return    the index in this list of the last occurrence of the specified
     *      element, or -1 if the list does not contain this element.
     */
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Entry e = header.previous; e != header; e = e.previous) {
                index--;
                if (e.element == null) {
                    return index;
                }
            }
        } else {
            for (Entry e = header.previous; e != header; e = e.previous) {
                index--;
                if (o.equals(e.element)) {
                    return index;
                }
            }
        }
        return -1;
    }

    /**
     *  Returns a list-iterator of the elements in this list (in proper
     *  sequence), starting at the specified position in the list. Obeys the
     *  general contract of <tt>List.listIterator(int)</tt> . <p>
     *
     *  The list-iterator is <i>fail-fast</i> : if the list is structurally
     *  modified at any time after the Iterator is created, in any way except
     *  through the list-iterator's own <tt>remove</tt> or <tt>add</tt> methods,
     *  the list-iterator will throw a <tt>ConcurrentModificationException</tt>
     *  . Thus, in the face of concurrent modification, the iterator fails
     *  quickly and cleanly, rather than risking arbitrary, non-deterministic
     *  behavior at an undetermined time in the future.
     *
     * @param  index  index of first element to be returned from the
     *      list-iterator (by a call to <tt>next</tt> ).
     * @return        a ListIterator of the elements in this list (in proper
     *      sequence), starting at the specified position in the list.
     * @throws        IndexOutOfBoundsException if index is out of range ( <tt>
     *      index &lt; 0 || index &gt; size()</tt> ).
     * @see           List#listIterator(int)
     */
    public ListIterator listIterator(int index) {
        return new ListItr(index);
    }

    private Entry newEntry(Object element, Entry next, Entry previous) {
        Entry entry = new Entry(element, next, previous);
        addHashEntry(entry);
        return entry;
    }

    /**
     *  Reconstitute this <tt>LinkedList</tt> instance from a stream (that is
     *  deserialize it).
     *
     * @param  s                           Description of Parameter
     * @exception  java.io.IOException     Description of Exception
     * @exception  ClassNotFoundException  Description of Exception
     */
    private synchronized void readObject(java.io.ObjectInputStream s) throws java.io.IOException,
            ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int objects = s.readInt();

        // Initialize header
        header = newEntry(null, null, null);
        header.next = header.previous = header;

        // Read in all elements in the proper order.
        for (int i = 0; i < objects; i++) {
            add(s.readObject());
        }
    }

    /**
     *  Removes the element at the specified position in this list. Shifts any
     *  subsequent elements to the left (subtracts one from their indices).
     *  Returns the element that was removed from the list.
     *
     * @param  index  the index of the element to removed.
     * @return        the element previously at the specified position.
     * @throws        IndexOutOfBoundsException if the specified index is out of
     *      range ( <tt>index &lt; 0 || index &gt;= size()</tt> ).
     */
    public Object remove(int index) {
        Entry e = entry(index);
        remove(e);
        return e.element;
    }

    private void remove(Entry e) {
        if (e == header) {
            throw new NoSuchElementException();
        }

        e.previous.next = e.next;
        e.next.previous = e.previous;
        size--;
        modCount++;
        removeHashEntry(e);
    }

    /**
     *  Removes the first occurrence of the specified element in this list. If
     *  the list does not contain the element, it is unchanged. More formally,
     *  removes the element with the lowest index <tt>i</tt> such that <tt>
     *  (o==null ? get(i)==null : o.equals(get(i)))</tt> (if such an element
     *  exists).
     *
     * @param  o  element to be removed from this list, if present.
     * @return    <tt>true</tt> if the list contained the specified element.
     */
    /*
     * public boolean remove(Object o) {
     * if (o==null) {
     * for (Entry e = header.next; e != header; e = e.next) {
     * if (e.element==null) {
     * remove(e);
     * return true;
     * }
     * }
     * } else {
     * for (Entry e = header.next; e != header; e = e.next) {
     * if (o.equals(e.element)) {
     * remove(e);
     * return true;
     * }
     * }
     * }
     * return false;
     * }
     */
    public boolean remove(Object o) {
        boolean succeeded;
        Entry e = getFirstHashEntry(o);
        if (e == null) {
            succeeded = false;
            // Log.stack("failed", this, "remove", o);
        } else {
            remove(e);
            succeeded = true;
        }
        return succeeded;
    }

    /**
     *  Removes and returns the first element from this list.
     *
     * @return    the first element from this list.
     * @throws    NoSuchElementException if this list is empty.
     */
    public Object removeFirst() {
        Object first = header.next.element;
        remove(header.next);
        return first;
    }

    private void removeHashEntry(Entry entry) {
        Object o = entry.element;
        Object entryOrList = _valueEntryMap.get(o);
        if (entryOrList == null) {
            Log.getLogger().warning("unable to remove: " + o);
        } else if (entryOrList instanceof List) {
            List c = (List) entryOrList;
            c.remove(entry);
            if (c.size() == 1) {
                _valueEntryMap.put(o, c.get(0));
            }
        } else {
            Entry e = (Entry) entryOrList;
            Assert.assertSame("entry", e.element, o);
            _valueEntryMap.remove(o);
        }
    }

    private Entry removeHashEntry(Object o) {
        Entry entry = getFirstHashEntry(o);
        removeHashEntry(entry);
        return entry;
    }

    /**
     *  Removes and returns the last element from this list.
     *
     * @return    the last element from this list.
     * @throws    NoSuchElementException if this list is empty.
     */
    public Object removeLast() {
        Object last = header.previous.element;
        remove(header.previous);
        return last;
    }

    public void replace(int index, Object to) {
        Entry entry = entry(index);
        removeHashEntry(entry);
        entry.element = to;
        addHashEntry(entry);
    }

    public void replace(Object from, Object to) {
        Entry entry = removeHashEntry(from);
        entry.element = to;
        addHashEntry(entry);
    }

    /**
     *  Replaces the element at the specified position in this list with the
     *  specified element.
     *
     * @param  index    index of element to replace.
     * @param  element  element to be stored at the specified position.
     * @return          the element previously at the specified position.
     * @throws          IndexOutOfBoundsException if the specified index is out
     *      of range ( <tt>index &lt; 0 || index &gt;= size()</tt> ).
     */
    public Object set(int index, Object element) {
        Entry e = entry(index);
        Object oldVal = e.element;
        e.element = element;
        addHashEntry(e);
        return oldVal;
    }

    /**
     *  Returns the number of elements in this list.
     *
     * @return    the number of elements in this list.
     */
    public int size() {
        return size;
    }

    /**
     *  Returns an array containing all of the elements in this list in the
     *  correct order.
     *
     * @return    an array containing all of the elements in this list in the
     *      correct order.
     */
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Entry e = header.next; e != header; e = e.next) {
            result[i++] = e.element;
        }
        return result;
    }

    /**
     *  Returns an array containing all of the elements in this list in the
     *  correct order. The runtime type of the returned array is that of the
     *  specified array. If the list fits in the specified array, it is returned
     *  therein. Otherwise, a new array is allocated with the runtime type of
     *  the specified array and the size of this list. <p>
     *
     *  If the list fits in the specified array with room to spare (i.e., the
     *  array has more elements than the list), the element in the array
     *  immediately following the end of the collection is set to null. This is
     *  useful in determining the length of the list <i>only</i> if the caller
     *  knows that the list does not contain any null elements.
     *
     * @param  a  the array into which the elements of the list are to be
     *      stored, if it is big enough; otherwise, a new array of the same
     *      runtime type is allocated for this purpose.
     * @return    an array containing the elements of the list.
     * @throws    ArrayStoreException if the runtime type of a is not a
     *      supertype of the runtime type of every element in this list.
     */
    public Object[] toArray(Object a[]) {
        if (a.length < size) {
            a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        int i = 0;
        for (Entry e = header.next; e != header; e = e.next) {
            a[i++] = e.element;
        }

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }

    public String toString() {
        return "HashList";
    }

    /**
     *  Save the state of this <tt>LinkedList</tt> instance to a stream (that
     *  is, serialize it).
     *
     * @serialData                      The size of the list (the number of
     *      elements it contains) is emitted (int), followed by all of its
     *      elements (each an Object) in the proper order.
     * @param  s                        Description of Parameter
     * @exception  java.io.IOException  Description of Exception
     */
    private synchronized void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Entry e = header.next; e != header; e = e.next) {
            s.writeObject(e.element);
        }
    }
}
