package edu.stanford.smi.protegex.htmldoc;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Generate the class hierarchy html page.
 * 
 * @author Samson Tu
 */
public class ClassHierarchyGenerator {
    private boolean _saveHidden = false;
    private boolean _printInstances;
    private static final String ENCODING = FileUtilities.getWriteEncoding();

    public ClassHierarchyGenerator() {
    }

    public void genHierarchy(KnowledgeBase kb, Collection topClses, PrintWriter itsWriter) {
        printHeader(kb, itsWriter);
        if ((topClses == null) || topClses.isEmpty()) {
            Cls root = kb.getRootCls();
            printClses(kb, itsWriter, root.getDirectSubclasses());
        } else {
            printClses(kb, itsWriter, topClses);
        }
        printClosingText(itsWriter);
    }

    public void genHierarchy(
        KnowledgeBase kb,
        Collection topClses,
        PrintWriter itsWriter,
        boolean saveHidden,
        boolean printInstances) {
        _printInstances = printInstances;
        setHidden(saveHidden);
        genHierarchy(kb, topClses, itsWriter);
    }

    private void printClosingText(PrintWriter itsWriter) {
        itsWriter.println("<hr>Generated on " + new Date().toString() + "</body></html>");
    }

    private void printCls(KnowledgeBase kb, PrintWriter itsWriter, Cls cls) {
        String clsRef = cls.getName();
        if (cls.isVisible() || (!cls.isVisible() && _saveHidden)) {
            if (!cls.isSystem())
                clsRef = ClassDocGenerator.hrefToFrame(cls);
            itsWriter.println("<li>");
            itsWriter.println(clsRef);
            itsWriter.println("</li>");
            if (_printInstances)
                printInstances(kb, itsWriter, cls.getDirectInstances());
            // display a list of all instances of this class
            Collection classes = cls.getDirectSubclasses();
            if ((classes != null) && (!classes.isEmpty())) {
                printClses(kb, itsWriter, classes); // display all direct subclasses
            }
        }
    }

    private void printClses(KnowledgeBase kb, PrintWriter itsWriter, Collection classes) {
        itsWriter.println("<ul>");
        List classList = new ArrayList(classes);
        Collections.sort(classList, new FrameComparator());
        for (Iterator i = classList.iterator(); i.hasNext();) {
            Cls cls = (Cls) i.next();
            printCls(kb, itsWriter, cls);
        }
        itsWriter.println("</ul>");
    }

    private void printInstances(KnowledgeBase kb, PrintWriter itsWriter, Collection instances) {
        if ((instances == null) || instances.isEmpty())
            return; // exit if no direct instances of this class
        itsWriter.println("<ul><i>");
        itsWriter.println("Instances : ");
        StringBuffer htmlText = new StringBuffer();
        for (Iterator i = instances.iterator(); i.hasNext();) {
            Instance inst = (Instance) i.next();
            htmlText.append(", " + ClassDocGenerator.hrefToFrame(inst));
        }
        itsWriter.println(htmlText.substring(2).toString() + "</i></ul>");
    }

    private void printHeader(KnowledgeBase kb, PrintWriter itsWriter) {
        itsWriter.println("<head>");
        itsWriter.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + ENCODING + "\">");
        itsWriter.println("<title>Protege-2000 Class Hierarchy </title>");
        itsWriter.println("</head>");
        itsWriter.println("<body>");
        itsWriter.println(
            "<center><h1> Class Hierarchy for <i>" + kb.getProject().getName() + "</i> Project </h1></center><hr>");

    }

    public void setHidden(boolean saveHidden) {
        this._saveHidden = saveHidden;
    }
}
