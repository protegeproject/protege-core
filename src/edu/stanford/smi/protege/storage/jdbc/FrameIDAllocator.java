package edu.stanford.smi.protege.storage.jdbc;

import edu.stanford.smi.protege.model.*;

/**
 * The interface for an object that allocates frame ids.  This is necessary because the database backend must allocate
 * unique ids in its own way, while the file-based backend handles this job quite differently.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FrameIDAllocator {

    FrameID allocateFrameID();
}
