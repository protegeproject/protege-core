package edu.stanford.smi.protege.server.util;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ProtegeJob;

public class PingProtegeServerJob extends ProtegeJob {
    private static final long serialVersionUID = -4241126901237002543L;

    protected PingProtegeServerJob(KnowledgeBase kb) {
        super(kb);
    }
    
    @Override
    public Object run() throws ProtegeException {
        return true;
    }

    public static boolean ping(KnowledgeBase kb) {
        try {
            new PingProtegeServerJob(kb).execute();
            return true;
        }
        catch (Throwable t) {
            /* It is the callers job to log a message */
            return false;
        }
    }
    
}
