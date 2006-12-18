package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.logging.*;

import edu.stanford.smi.protege.server.*;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class JournalFormater extends AbstractFormatter {
    private final String appUsername;

    public JournalFormater() {
        this.appUsername = ApplicationProperties.getUserName();
    }

    private String getUsername() {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        return session == null ? appUsername : session.getUserName();
    }

    public String format(LogRecord record) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getDateString());
        buffer.append(" ");
        buffer.append(getUsername());
        buffer.append(" - ");
        Object[] params = record.getParameters();
        Method method = (Method) params[0];
        buffer.append(method.getName());
        buffer.append("(");
        for (int i = 1; i < params.length - 1; ++i) {
            Object param = params[i];
            if (i != 1) {
                buffer.append(", ");
            }
            buffer.append(toString(param));
        }
        buffer.append(") returns ");
        Object result = params[params.length - 1];
        buffer.append(toString(result));
        buffer.append(getLineSeparator());
        return buffer.toString();
    }

}