package edu.stanford.smi.protege.storage.jdbc;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Class to handling the logging of commands that change the model.  These changes go into a text file.  We currently 
 * don't have anything that knows how to read this file but it would not be too hard to build such a thing.
 * 
 * Note that logging is hear called "journaling" because the Log class already existing for diagnostic messages.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class OldJournal {
    private static String _userName = SystemUtilities.getUserName();
    private static PrintWriter _output;
    private static PrintWriter _defaultOutput = new PrintWriter(System.out);
    private static boolean _isRecording = false;
    private static URI _journalURI;

    private static String collectionToString(Collection c) {
        boolean isFirst = true;
        StringBuffer buffer = new StringBuffer();
        Iterator i = c.iterator();
        while (i.hasNext()) {
            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(objectToString(i.next()));
        }
        return buffer.toString();
    }

    public static void enter(Object source, String command) {
        if (_isRecording) {
            enter(source, command, new String[0], new Object[0]);
        }
    }

    private static void enter(Object source, String command, String[] argnames, Object[] arguments) {
        StringBuffer entry = new StringBuffer();
        entry.append(new Date().toString());
        entry.append(", ");
        if (_userName.trim().length() == 0) {
            _userName = "<unknown user>";
        }
        entry.append(_userName);
        entry.append(", ");
        entry.append(command);
        entry.append(", ");
        entry.append(getSourceText(source));
        for (int i = 0; i < arguments.length; ++i) {
            entry.append(", ");
            entry.append(argnames[i]);
            entry.append('=');
            entry.append(objectToString(arguments[i]));
        }
        PrintWriter writer = (_output == null) ? _defaultOutput : _output;
        writer.println(entry);
        writer.flush();
    }

    public static void enter(Object source, String command, String argname1, Object arg1) {
        if (_isRecording) {
            enter(source, command, new String[] { argname1 }, new Object[] { arg1 });
        }
    }

    public static void enter(Object source, String command, String name1, Object arg1, String name2, Object arg2) {
        if (_isRecording) {
            enter(source, command, new String[] { name1, name2 }, new Object[] { arg1, arg2 });
        }
    }

    public static void enter(
        Object src,
        String com,
        String n1,
        Object arg1,
        String n2,
        Object arg2,
        String n3,
        Object arg3) {
        if (_isRecording) {
            enter(src, com, new String[] { n1, n2, n3 }, new Object[] { arg1, arg2, arg3 });
        }
    }

    public static void enter(
        Object source,
        String command,
        String argname1,
        Object arg1,
        String argname2,
        Object arg2,
        String argname3,
        Object arg3,
        String argname4,
        Object arg4) {
        if (_isRecording) {
            enter(
                source,
                command,
                new String[] { argname1, argname2, argname3, argname4 },
                new Object[] { arg1, arg2, arg3, arg4 });
        }
    }

    public static void enter(
        Object source,
        String command,
        String argname1,
        Object arg1,
        String argname2,
        Object arg2,
        String argname3,
        Object arg3,
        String argname4,
        Object arg4,
        String argname5,
        Object arg5) {
        if (_isRecording) {
            enter(
                source,
                command,
                new String[] { argname1, argname2, argname3, argname4, argname5 },
                new Object[] { arg1, arg2, arg3, arg4, arg5 });
        }
    }

    public static URI getJournalURI() {
        return _journalURI;
    }

    private static String getSourceText(Object source) {
        String sourceText;
        if (source instanceof KnowledgeBase) {
            sourceText = "kb=" + ((KnowledgeBase) source).getName();
        } else if (source instanceof Frame) {
            sourceText = "kb=" + ((Frame) source).getKnowledgeBase().getName();
        } else if (source instanceof String) {
            sourceText = "source=" + source.toString();
        } else {
            sourceText = "source=" + source.getClass().getName();
        }
        return sourceText;
    }

    public static String getUserName() {
        return _userName;
    }

    private static String objectToString(Object o) {
        String string;
        if (o == null) {
            string = "<null>";
        } else if (o instanceof Collection) {
            string = "{" + collectionToString((Collection) o) + "}";
        } else if (o instanceof Frame) {
            string = ((Frame) o).getName();
        } else {
            string = o.toString();
        }
        return string;
    }

    public static void setJournalURI(URI uri) {
        try {
            _output = new PrintWriter(URIUtilities.createBufferedWriter(uri, true));
            _journalURI = uri;
        } catch (RuntimeException e) {
            Log.exception(e, OldJournal.class, "setJournalFile", uri);
            _journalURI = null;
        }
    }

    public static void setUserName(String userName) {
        _userName = userName;
    }

    public static void startRecording() {
        if (!_isRecording) {
            enter("Journal", "start recording");
            _isRecording = true;
        }
    }

    public static void stopRecording() {
        if (_isRecording) {
            enter("Journal", "stop recording");
            _output.flush();
            _output.close();
            _isRecording = false;
        }
    }
}
