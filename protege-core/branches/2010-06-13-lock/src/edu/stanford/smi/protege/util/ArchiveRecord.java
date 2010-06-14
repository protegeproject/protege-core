package edu.stanford.smi.protege.util;

import java.io.*;
import java.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ArchiveRecord {
    private File _archiveDir;
    private String _comment;

    public ArchiveRecord(File dir, String comment) {
        _archiveDir = dir;
        _comment = comment;
    }

    public Date getTimestamp() {
        Date date = null;
        try {
            date = ArchiveManager.getDateFormat().parse(_archiveDir.getName());
        } catch (Exception e) {
            // do nothing
        }
        return date;
    }

    public String getComment() {
        return _comment;
    }
}
