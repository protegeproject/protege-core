package edu.stanford.smi.protege.storage.clips;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Base class for classes which generate clips files.  This class contains lots of convenience functions.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ClipsFileWriter {
    private PrintWriter _writer;

    protected ClipsFileWriter(Writer writer) {
        _writer = new PrintWriter(writer);
        printVersion();
    }

    public void flush() {
        _writer.flush();
    }

    public void print(int i) {
        _writer.print(i);
    }

    public void print(String s) {
        _writer.print(s);
    }

    public void printFrame(Frame frame) {
        _writer.print(toExternalFrameName(frame));
    }

    public void printFrameName(String name) {
        _writer.print(toExternalFrameName(name));
    }

    public void println() {
        _writer.println();
    }

    public void println(String s) {
        _writer.println(s);
    }

    public boolean printSucceeded() {
        return !_writer.checkError();
    }

    private void printVersion() {
        println("; " + new Date().toString());
        println("; ");
        println(";+ (version \"" + Text.getVersion() + "\")");
        println(";+ (build \"" + Text.getBuildInfo() + "\")");
    }

    public static String toExternalFrameName(Frame frame) {
        return toExternalFrameName(frame.getName());
    }

    public static String toExternalFrameName(String internalFrameName) {
        return ClipsUtil.toExternalSymbol(internalFrameName);
    }

    protected static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }
}
