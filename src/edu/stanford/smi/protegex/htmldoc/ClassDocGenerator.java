package edu.stanford.smi.protegex.htmldoc;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Generate the individual html pages for classes and instances.
 *
 * @author Samson Tu
 * @author Henk-Jan Lebbink
 * @author Robert Voorn
 */
public class ClassDocGenerator {

    private File _directory = null;
    private Project _project = null;
    private boolean _saveHidden = false;
    private String _indexpage = null;
    private Collection _clsesToPrint = null;
    private boolean _printInstances = false;
    private static final String ENCODING = FileUtilities.getWriteEncoding();

    public ClassDocGenerator() {
    }

    public void setPrintInstances(boolean inst) {
        _printInstances = inst;
    }

    private void genDoc(Instance inst) {
        if (_directory != null) {
            File instanceFile = new File(_directory, getHtmlFilename(inst, false));
            PrintWriter itsWriter = null; //Note that the writer changes for each class
            try {
                itsWriter = FileUtilities.createPrintWriter(instanceFile, true);
                itsWriter.println();
                printHeaderInstanceInformation(itsWriter, inst);
                itsWriter.println("<body>");
                printInfo(inst, itsWriter);
                printOwnSlots(inst, itsWriter);
                printClosingText(itsWriter);
            } catch (Throwable e) {
                Log.getLogger().warning("Error writing to " + instanceFile);
            }
        } else {
            Log.getLogger().warning("\"directory\" variable is not set!");
        }
    }

    private void genDoc(Cls cls) {
        if (cls.isVisible() || (!cls.isVisible() && _saveHidden)) {
            if (_directory != null) {
                File classFile = new File(_directory, getHtmlFilename(cls, false));
                PrintWriter itsWriter = null; //Note that the writer changes for each class
                try {
                    itsWriter = FileUtilities.createPrintWriter(classFile, true);
                    itsWriter.println();
                    printHeaderClassInformation(itsWriter, cls);
                    itsWriter.println("<body>");
                    printInfo(cls, itsWriter);
                    printTemplateSlots(cls, itsWriter);
                    printOwnSlots(cls, itsWriter);
                    printClosingText(itsWriter);
                } catch (Throwable e) {
                    Log.getLogger().warning("Error writing to " + classFile + ": " + e);
                    FileUtilities.close(itsWriter);
                }
            } else {
                Log.getLogger().warning("\"directory\" variable is not set!");
            }
        }
    }

    public void genClsesDoc(KnowledgeBase kb, Collection topClses) {
        // get all non-included classes in kb
        Collection clses = new ArrayList();
        Collection instes = new ArrayList();

        Cls topCls = null;
        if ((topClses == null) || topClses.isEmpty())
            clses = kb.getClses();
        else {
            for (Iterator topIterator = topClses.iterator(); topIterator.hasNext();) {
                topCls = (Cls) topIterator.next();
                clses.add(topCls);
                clses.addAll(topCls.getSubclasses());
            }
        }
        _clsesToPrint = clses;
        _project = kb.getProject();
        for (Iterator clsIterator = clses.iterator(); clsIterator.hasNext();) {
            Cls cls = (Cls) clsIterator.next();
            if (hasDocPage(cls)) {
                genDoc(cls);
                if (_printInstances) {
                    instes = cls.getInstances();
                    for (Iterator instesIterator = instes.iterator(); instesIterator.hasNext();) {
                        Instance inst = (Instance) instesIterator.next();
                        genDoc(inst);
                    }
                }
            }
        }
    }

    public void genClsesDoc(
        KnowledgeBase kb,
        Collection topClses,
        boolean hidden,
        String indexPage,
        String outputDir,
        boolean printInstances)
        throws FileNotFoundException {

        setIndexPage(indexPage);
        setOutputDir(outputDir);
        setHidden(hidden);
        setPrintInstances(printInstances);
        genClsesDoc(kb, topClses);
    }

    private String getAllowed(Cls cls, Slot slot) {
        String allowed = "";
        ValueType type = cls.getTemplateSlotValueType(slot);
        if (type.equals(ValueType.SYMBOL)) {
            Collection allowedValues = cls.getTemplateSlotAllowedValues(slot);
            for (Iterator allowedIterator = allowedValues.iterator(); allowedIterator.hasNext();) {
                String nextAllowed = (String) allowedIterator.next();
                allowed = allowed + nextAllowed;
                if (allowedIterator.hasNext())
                    allowed = allowed + ", ";
            }
        } else if (type.equals(ValueType.INSTANCE) || type.equals(ValueType.CLS)) {
            Collection allowedValues;
            if (type.equals(ValueType.INSTANCE)) {
                allowedValues = cls.getTemplateSlotAllowedClses(slot);
            } else {
                allowedValues = cls.getTemplateSlotAllowedParents(slot);
            }
            for (Iterator allowedIterator = allowedValues.iterator(); allowedIterator.hasNext();) {
                Cls nextAllowed = (Cls) allowedIterator.next();
                if (nextAllowed.isVisible() || (!nextAllowed.isVisible() && _saveHidden)) {
                    if (hasDocPage(nextAllowed))
                        allowed = allowed + hrefToFrame(nextAllowed);
                    else
                        allowed = nextAllowed.getName();
                    if (allowedIterator.hasNext())
                        allowed = allowed + ", ";
                }
            }
        }
        return allowed;
    }

    private String getCardinality(Cls cls, Slot slot) {
        String maxCardinality =
            (slot.getMaximumCardinality() == KnowledgeBase.MAXIMUM_CARDINALITY_UNBOUNDED)
                ? "*"
                : Integer.toString(slot.getMaximumCardinality());
        String cardinality = slot.getMinimumCardinality() + ":" + maxCardinality;
        return cardinality;
    }

    private String getDefaults(Cls cls, Slot slot) {
        String defaults = "";
        Collection defaultCollection = slot.getDefaultValues();
        if (defaultCollection != null) {
            for (Iterator i = defaultCollection.iterator(); i.hasNext();) {
                defaults = defaults + " " + i.next().toString();
            }
        }
        return defaults;
    }

    private static String getHtmlFilename(Frame frame, boolean escape) {
        String name;
        try {
            name = URLEncoder.encode(frame.getName(), ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.exception(e, ClassDocGenerator.class, "getHtmlFilename", frame);
            name = frame.getName();

        }
        StringBuffer result = new StringBuffer();
        if (escape) {
            for (int i = 0; i < name.length(); ++i) {
                char c = name.charAt(i);
                result.append(c);
                if (c == '%') {
                    String HEX_CODE_FOR_PERCENT = "25";
                    result.append(HEX_CODE_FOR_PERCENT);
                }
            }

        } else {
            result.append(name);
        }
        result.append(".html");
        return result.toString();
    }

    private String getSlotDoc(Cls cls, Slot slot) {

        String doc = "";

        Collection classdocCollection = cls.getTemplateSlotDocumentation(slot);
        if (!classdocCollection.isEmpty()) {
            //    doc = "Class slot documentation: ";

            for (Iterator i = classdocCollection.iterator(); i.hasNext();) {
                doc += toHtmlString((String) i.next()) + "<P>";

            }
        }
        return doc;
    }

    private boolean hasDocPage(Cls cls) {
        return (!cls.isSystem() && _clsesToPrint.contains(cls));
    }

    public static String hrefToFrame(Frame frame) {
        return "<A HREF=\"" + getHtmlFilename(frame, true) + "\">" + frame.getBrowserText() + "</A>";
    }

    public static String frameBrowserText(Frame frame) {
        return "[" + frame.getBrowserText() + "]";
    }

    private void printInfo(Cls cls, PrintWriter itsWriter) {
        itsWriter.println("<!-- ======== START OF CLASS DATA ======== -->  ");
        itsWriter.println(
            "<H2><font size=\"-1\"> Project: "
                + ((cls.isIncluded()) ? "included" : _project.getName())
                + "</font><BR>");
        itsWriter.println("Class " + toHtmlString(cls.getName()) + "</H2>  ");
        itsWriter.println("<dl>");
        itsWriter.println("<dt><b>" + ((cls.isAbstract()) ? "Abstract" : "Concrete") + " Class Extends&nbsp;</b></dt>");

        itsWriter.println("<dt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ");
        Collection supers = cls.getDirectSuperclasses();
        for (Iterator supersIterator = supers.iterator(); supersIterator.hasNext();) {
            Cls superCls = (Cls) supersIterator.next();
            if (superCls.isVisible() || (!superCls.isVisible() && _saveHidden)) {
                if (hasDocPage(superCls))
                    itsWriter.print(hrefToFrame(superCls));
                else
                    itsWriter.print(toHtmlString(superCls.getName()));
                if (supersIterator.hasNext())
                    itsWriter.print(", ");
                else
                    itsWriter.print("</dt>");
            }
        }

        itsWriter.println("<dd>&nbsp;</dd>");
        itsWriter.println("<DT><B>Direct Instances:</B> <DD>");
        Collection instes = cls.getDirectInstances();
        if ((instes == null) || instes.isEmpty()) {
            itsWriter.println("None");
        } else if (instes.size() == 1) {
            Object[] objects = instes.toArray();
            if (_printInstances)
                itsWriter.print(hrefToFrame((Instance) objects[0]));
            else
                itsWriter.print(frameBrowserText((Instance) objects[0]));
        } else {
            itsWriter.print("<OL>");
            for (Iterator instesIterator = instes.iterator(); instesIterator.hasNext();) {
                Instance inst = (Instance) instesIterator.next();
                itsWriter.print("<LI>");
                if (_printInstances) {
                    itsWriter.print(hrefToFrame(inst));
                } else
                    itsWriter.print(frameBrowserText(inst));
                itsWriter.print("</LI>");

            }
            itsWriter.print("</OL>");

        }
        itsWriter.print("</dt>");
        itsWriter.println("<dd>&nbsp;</dd>");
        itsWriter.println("<DT><B>Direct Subclasses:</B> <DD>");
        Collection subs = cls.getDirectSubclasses();
        if ((subs == null) || subs.isEmpty()) {
            itsWriter.println("None");
        } else if (subs.size() == 1) {
            Object[] objects = subs.toArray();
            itsWriter.print(hrefToFrame((Cls) objects[0]));
        } else {
            itsWriter.print("<OL>");
            for (Iterator subsIterator = subs.iterator(); subsIterator.hasNext();) {
                Cls subCls = (Cls) subsIterator.next();
                if (subCls.isVisible() || (!subCls.isVisible() && _saveHidden)) {
                    itsWriter.print("<LI>");
                    if (hasDocPage(cls))
                        itsWriter.print(hrefToFrame(subCls));
                    else
                        itsWriter.print(toHtmlString(subCls.getName()));
                    itsWriter.print("</LI>");
                }
            }
            itsWriter.print("</OL>");
        }
        itsWriter.print("</dt>");
        itsWriter.println("<dd>&nbsp;</dd>");
        writeClassDoc(cls, itsWriter);
        itsWriter.println("</dl>");
        itsWriter.println("<HR>");

        itsWriter.println("<P>");
    }

    private void printInfo(Instance inst, PrintWriter itsWriter) {
        itsWriter.println("<!-- ======== START OF INSTANCE DATA ======== -->  ");
        itsWriter.println(
            "<H2><font size=\"-1\"> Project: "
                + ((inst.isIncluded()) ? "included" : _project.getName())
                + "</font><BR>");
        itsWriter.println("Instance " + toHtmlString(inst.getBrowserText()) + "</H2>  ");
        itsWriter.println("<dl>");
        Cls directType = inst.getDirectType();
        String directTypeString = toHtmlString(directType.getName());
        if (hasDocPage(directType))
            directTypeString = hrefToFrame(directType);
        itsWriter.println("<dt><b>" + " Instance of " + directTypeString + " Class&nbsp;</b></dt>");

        itsWriter.println("<dt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ");
        itsWriter.println("</dl>");
    }

    private void printClosingText(PrintWriter itsWriter) {
        if ((_indexpage != null) && !_indexpage.equals("")) {
            itsWriter.println("<P><A HREF=\"" + _indexpage + "\"> Return to class hierarchy </A>");
        } else
            Log.getLogger().warning("No index page defined!");
        itsWriter.println("<hr>Generated on " + new Date().toString() + "</body></html>");
    }

    private void printHeaderClassInformation(PrintWriter itsWriter, Cls cls) {
        itsWriter.println("<head>");
        itsWriter.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + ENCODING + "\">");
        itsWriter.println("<title>Protege-2000 Class " + toHtmlString(cls.getName()) + " Documentation</title>");
        itsWriter.println("</head>");

    }

    private void printHeaderInstanceInformation(PrintWriter itsWriter, Instance inst) {
        itsWriter.println("<head>");
        itsWriter.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + ENCODING + "\">");
        itsWriter.println("<title>Protege-2000 Instance " + toHtmlString(inst.getName()) + " Documentation</title>");
        itsWriter.println("</head>");

    }

    private void printOwnSlots(Cls cls, PrintWriter itsWriter) {
        Collection nonSystemSlots = new ArrayList();
        Collection slots = cls.getOwnSlots();
        for (Iterator i = slots.iterator(); i.hasNext();) {
            Slot slot = (Slot) i.next();
            if (!slot.isSystem())
                nonSystemSlots.add(slot);
        }
        if (!nonSystemSlots.isEmpty()) {
            itsWriter.println("<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\"> ");
            itsWriter.println("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">");
            itsWriter.println("<TD COLSPAN=5 width=\"100%\"><font size=\"+2\"><b>Own Slots</b></font></TD>");
            itsWriter.println("</TR> ");
            itsWriter.println("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\"> ");
            itsWriter.println("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"20%\"><b>Slot name</b></TD>");
            itsWriter.println("<TD ALIGN=\"left\" width=\"50%\"><b>Documentation</b></TD>");
            itsWriter.println("<TD ALIGN=\"left\" width=\"10%\"><b>Type</b></TD>   ");
            itsWriter.println("<TD ALIGN=\"left\" width=\"10%\"><b>Value&nbsp;</b></TD> ");
            itsWriter.println("<TD width=\"5%\"><b>Cardinality</b></TD> ");
            itsWriter.println("</TR>  ");

            for (Iterator slotsIterator = nonSystemSlots.iterator(); slotsIterator.hasNext();) {
                Slot slot = (Slot) slotsIterator.next();
                itsWriter.println(
                    "<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"20%\"><i>"
                        + toHtmlString(slot.getName())
                        + "</i></TD>");
                itsWriter.println("<TD ALIGN=\"left\" width=\"50%\">" + getSlotDoc(cls, slot) + "&nbsp;</TD>");
                StringBuffer valuesBuffer = new StringBuffer();
                Iterator values = cls.getOwnSlotValues(slot).iterator();
                while (values.hasNext()) {
                    valuesBuffer.append(values.next().toString() + "\n");
                }
                itsWriter.println("<TD ALIGN=\"left\" width=\"10%\">" + slot.getValueType() + "&nbsp;</TD>   ");
                itsWriter.println(
                    "<TD ALIGN=\"left\" width=\"10%\"> <PRE>" + valuesBuffer.toString() + "</PRE>&nbsp;</b></TD> ");
                itsWriter.println("<TD width=\"5%\">" + "&nbsp;" + "</b></TD></TR>");
            }
            itsWriter.println("</table><P>");

        }
    }

    private void printOwnSlots(Instance inst, PrintWriter itsWriter) {
        Collection nonSystemSlots = new ArrayList();
        Collection slots = inst.getOwnSlots();

        for (Iterator i = slots.iterator(); i.hasNext();) {
            Slot slot = (Slot) i.next();
            if (!slot.isSystem())
                nonSystemSlots.add(slot);
        }
        if (!nonSystemSlots.isEmpty()) {
            itsWriter.println("<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\"> ");
            itsWriter.println("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">");
            itsWriter.println("<TD COLSPAN=3 width=\"100%\"><font size=\"+2\"><b>Own Slots</b></font></TD>");
            itsWriter.println("</TR> ");
            itsWriter.println("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\"> ");
            itsWriter.println("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"20%\"><b>Slot name</b></TD>");
            itsWriter.println("<TD ALIGN=\"left\" width=\"10%\"><b>Value&nbsp;</b></TD> ");
            itsWriter.println("<TD ALIGN=\"left\" width=\"10%\"><b>Type</b></TD>   ");
            itsWriter.println("</TR>  ");

            for (Iterator slotsIterator = nonSystemSlots.iterator(); slotsIterator.hasNext();) {
                Slot slot = (Slot) slotsIterator.next();
                itsWriter.println(
                    "<TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"15%\"><i>"
                        + toHtmlString(slot.getName())
                        + "&nbsp;</i></TD>");
                itsWriter.println(
                    "<TD ALIGN=\"left\" width=\"70%\">"
                        + toHtmlString(inst.getOwnSlotValues(slot))
                        + "&nbsp;</b></TD> ");
                itsWriter.println("<TD ALIGN=\"left\" width=\"15%\">" + slot.getValueType() + "&nbsp;</b></TD></TR> ");
            }
            itsWriter.println("</table><P>");
        }
    }

    private void printTemplateSlots(Cls cls, PrintWriter itsWriter) {
        Collection nonSystemSlots = new ArrayList();
        Collection slots = cls.getTemplateSlots();
        for (Iterator i = slots.iterator(); i.hasNext();) {
            Slot slot = (Slot) i.next();
            if (!slot.isSystem())
                nonSystemSlots.add(slot);
        }
        if (!nonSystemSlots.isEmpty()) {
            itsWriter.println("<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\"> ");
            itsWriter.println("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">");
            itsWriter.println("<TD COLSPAN=6 width=\"100%\"><font size=\"+2\"><b>Template Slots</b></font></TD>");
            itsWriter.println("</TR> ");
            itsWriter.println("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\"> ");
            itsWriter.println("<TD ALIGN=\"right\" WIDTH=\"10%\"><b>Slot name</b></TD>");
            itsWriter.println("<TD width=\"60%\"><b>Documentation</b></TD>");
            itsWriter.println("<TD width=\"10%\"><b>Type</b></TD>   ");
            itsWriter.println("<TD width=\"10%\"><b>Allowed Values/Classes</b></TD> ");
            itsWriter.println("<TD width=\"5%\"><b>Cardinality</b></TD> ");
            itsWriter.println("<TD width=\"5%\"><b>Default</b></TD>  ");
            itsWriter.println("</TR>  ");

            for (Iterator slotsIterator = nonSystemSlots.iterator(); slotsIterator.hasNext();) {
                Slot slot = (Slot) slotsIterator.next();
                itsWriter.println(
                    "<TD ALIGN=\"right\"  WIDTH=\"10%\"><i>" + toHtmlString(slot.getName()) + "</i></TD>");
                itsWriter.println("<TD ALIGN=\"left\" width=\"60%\">" + getSlotDoc(cls, slot) + "</TD>");
                itsWriter.println("<TD ALIGN=\"left\" width=\"10%\">" + slot.getValueType() + "&nbsp;" + "</TD>   ");
                itsWriter.println(
                    "<TD ALIGN=\"left\" width=\"10%\">" + getAllowed(cls, slot) + "&nbsp;" + "</b></TD> ");
                itsWriter.println("<TD width=\"5%\">" + getCardinality(cls, slot) + "&nbsp;" + "</b></TD> ");
                itsWriter.println(
                    "<TD ALIGN=\"left\" width=\"5%\">"
                        + toHtmlString(getDefaults(cls, slot))
                        + "&nbsp;"
                        + "</TD></TR>  ");
            }
        }
        itsWriter.println("</table><P>");
    }

    public void setHidden(boolean saveHidden) {
        this._saveHidden = saveHidden;
    }

    public void setIndexPage(String indexpage) {
        this._indexpage = indexpage;
    }

    public void setOutputDir(String path) throws FileNotFoundException {
        File pathFile = new File(path);
        if (pathFile.exists()) {
            this._directory = pathFile;
        } else
            throw new FileNotFoundException("Output directory " + path + " not found");
    }

    private static String toHtmlString(String s) {
        StringBuffer htmlString = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                htmlString.append(c);
            } else {
                // use escape sequence
                htmlString.append("&#");
                htmlString.append((int) c);
                htmlString.append(';');
            }
        }
        return htmlString.toString();
    }

    private static String generateString(Object inst) {
        if (inst instanceof DefaultSimpleInstance) {
            StringBuffer buffer = new StringBuffer();

            buffer.append(hrefToFrame((Frame) inst));
            buffer.append(" (of class ");
            DefaultSimpleInstance tmpInst = (DefaultSimpleInstance) inst;
            buffer.append(hrefToFrame(tmpInst.getDirectType()));
            buffer.append(")");
            return buffer.toString();
        }
        if (inst instanceof DefaultCls) {
            DefaultCls tmpCls = (DefaultCls) inst;
            return hrefToFrame(tmpCls.getDirectType());
        }
        return inst.toString(); // casting failed -> simple java.lang.object class
    }

    private static String toHtmlString(Collection collection) {
        StringBuffer htmlString = new StringBuffer();
        if (collection.size() == 1) {
            Object[] slots = collection.toArray();
            htmlString.append(generateString(slots[0]));

        } else {

            htmlString.append("<OL>");
            for (Iterator collectionIterator = collection.iterator(); collectionIterator.hasNext();) {
                htmlString.append("<LI>");
                Object slot = collectionIterator.next();
                String tmpStr = generateString(slot);
                htmlString.append(tmpStr);
                htmlString.append("</LI>");

            }
            htmlString.append("</OL>");

        }
        return htmlString.toString();
    }

    private void writeClassDoc(Cls cls, PrintWriter itsWriter) {
        Collection comments = cls.getDocumentation();
        if ((comments != null) && !(comments.isEmpty())) {
            itsWriter.println("<DT><B>Class Documentation:</B> <DD>");
            Iterator i = comments.iterator();
            while (i.hasNext()) {
                String doc = (String) i.next();
                int lineBreakLoc;
                while ((lineBreakLoc = doc.indexOf(Character.getNumericValue('\n'))) != -1) {
                    doc =
                        toHtmlString(doc.substring(0, lineBreakLoc + 1))
                            + "<P>"
                            + toHtmlString(doc.substring(lineBreakLoc + 1, doc.length()));
                }
                itsWriter.println(doc);
                itsWriter.println("<P>");
            }
            itsWriter.print("</dt>");

        }
    }
}
