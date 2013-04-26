package edu.stanford.smi.protege.util;

import java.awt.datatransfer.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * Transferable object for a collection of Frames.  This is used for drag and drop.  Note that frames are not
 * serializable so we have to use "java local object mime type".
 * 
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class TransferableCollection implements Transferable {
    private static final Collection flavors = new ArrayList();
    private static DataFlavor collectionFlavor;
    private ArrayList frames;

    static {
        try {
            String collectionClassname = ArrayList.class.getName();
            String collectionFlavorName = DataFlavor.javaJVMLocalObjectMimeType + "; class=" + collectionClassname;
            collectionFlavor = new DataFlavor(collectionFlavorName);

            flavors.add(collectionFlavor);
            flavors.add(DataFlavor.stringFlavor);
        } catch (Exception e) {
            Log.getLogger().warning(Log.toString(e));
        }
    }

    public TransferableCollection(Collection frames) {
        this.frames = new ArrayList(frames);
    }

    public static DataFlavor getCollectionFlavor() {
        return collectionFlavor;
    }

    public Object getTransferData(DataFlavor flavor) {
        Object o;
        if (flavor.equals(collectionFlavor)) {
            o = getCollectionTransferData();
        } else if (flavor.equals(DataFlavor.stringFlavor)) {
            o = getStringTransferData();
        } else {
            o = null;
        }
        return o;
    }

    public Object getCollectionTransferData() {
        return new ArrayList(frames);
    }

    public Object getStringTransferData() {
        StringBuffer buffer = new StringBuffer();
        Iterator i = frames.iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            String name = frame.getName();
            buffer.append(name);
            if (i.hasNext()) {
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }

    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) flavors.toArray(new DataFlavor[flavors.size()]);
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }
}
