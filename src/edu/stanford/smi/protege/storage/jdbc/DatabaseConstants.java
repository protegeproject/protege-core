package edu.stanford.smi.protege.storage.jdbc;

/**
 * Constants used in simple database interface.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface DatabaseConstants {

    int TYPE_INVALID = -1;
    int TYPE_STRING = 0;
    int TYPE_BOOLEAN = 1;
    int TYPE_FLOAT = 2;
    int TYPE_INTEGER = 3;
    int TYPE_SIMPLE_INSTANCE = 4;
    int TYPE_CLASS = 5;
    int TYPE_SLOT = 6;
    int TYPE_FACET = 7;
}
