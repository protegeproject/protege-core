package edu.stanford.smi.protegex.htmldoc;

import java.io.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Main class for generating html documentation from a Knowledge Base.
 * 
 * @author Samson Tu
 */
public class ProtegeGenClassHierarchy {

    public ProtegeGenClassHierarchy() {
    }

    public static void generateDocs(
        KnowledgeBase kb,
        Collection topClses,
        boolean saveHidden,
        String indexPage,
        String outputDir,
        boolean printInstances) {

        //***********First generate documentation page for classes
        ClassDocGenerator classDocGen = new ClassDocGenerator();
        try {
            classDocGen.genClsesDoc(kb, topClses, saveHidden, indexPage, outputDir, printInstances);
        } catch (Exception e) {
            Log.getLogger().warning(e.toString());
        }

        //********** Generate hierarchy of classes  *************
        File treeFile = new File(outputDir, indexPage);
        PrintWriter itsWriter = null;
        try {
            itsWriter = FileUtilities.createPrintWriter(treeFile, true);
        } catch (Exception e) {
            Log.getLogger().warning(e.toString());
        }

        ClassHierarchyGenerator classTreeGen = new ClassHierarchyGenerator();

        classTreeGen.genHierarchy(kb, topClses, itsWriter, saveHidden, printInstances);
    }

}
