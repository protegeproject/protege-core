package edu.stanford.smi.protege.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.storage.clips.ClipsKnowledgeBaseFactory;
import edu.stanford.smi.protege.util.Log;

public class ServerProjectURIUtil {
    private static final Logger logger = Log.getLogger(ServerProjectURIUtil.class);
    
    private ServerProjectURIUtil() {
    }

    public static URI getSingleProjectURI(Project project) throws RemoteException {
        if (project.getIncludedProjects().isEmpty()) {
            return project.getProjectURI();
        }
        else {
            List errors = new ArrayList();
            Project output = Project.createNewProject(new ClipsKnowledgeBaseFactory(), errors);
            if (!errors.isEmpty()) {
                return failed(project, errors);
            }
            project.loadIncludedProjects(project.getProjectURI(), project.getProjectInstance(), errors);
            if (!errors.isEmpty()) {
                return failed(project, errors);
            }
            copy(project, output);
            try {
                return save(project, output);
            }
            catch (IOException e) {
                RemoteException re = new RemoteException("Failed to save project kb");
                re.initCause(e);
                throw re;
            }
        }
    }
    
    private static void copy(Project project, Project output) {
        MergingNarrowFrameStore sourceMFS = MergingNarrowFrameStore.get(project.getInternalProjectKnowledgeBase());
        MergingNarrowFrameStore targetMFS = MergingNarrowFrameStore.get(output.getInternalProjectKnowledgeBase());
        copy(sourceMFS, targetMFS);
    }
    
    private static void copy(MergingNarrowFrameStore sourceMFS, MergingNarrowFrameStore targetMFS) {
        for (Object o : sourceMFS.getFrames()) {
            Frame frame = (Frame) o;
            copyDirectOwnSlotValues(frame, sourceMFS, targetMFS);
            if (frame instanceof Cls) {
                Cls cls = (Cls) frame;
                copyDirectTemplateSlotInformation(cls, sourceMFS, targetMFS);
            }
        }
    }
    
    
    private static void copyDirectOwnSlotValues(Frame frame, MergingNarrowFrameStore sourceMFS, MergingNarrowFrameStore targetMFS) {
        for (Object o : frame.getOwnSlots()) {
            Slot slot = (Slot) o;
            Collection values = sourceMFS.getValues(frame, slot, null, false);
            targetMFS.setValues(frame, slot, null, false, values);
        }
    }

    private static void copyDirectTemplateSlotInformation(Cls cls, MergingNarrowFrameStore sourceMFS, MergingNarrowFrameStore targetMFS) {
        for (Object o : cls.getTemplateSlots()) {
            Slot slot = (Slot) o;
            Collection values  = sourceMFS.getValues(cls, slot, null, true);
            targetMFS.setValues(cls, slot, null, true, values);
            copyDirectTemplateFacetValues(cls, slot, sourceMFS, targetMFS);
        }
    }

    private static void copyDirectTemplateFacetValues(Cls cls, Slot slot, MergingNarrowFrameStore sourceMFS, MergingNarrowFrameStore targetMFS) {
        for (Object o : cls.getTemplateFacets(slot)) {
            Facet facet = (Facet) o;
            Collection values = sourceMFS.getValues(cls, slot, facet, true);
            targetMFS.setValues(cls, slot, facet, true, values);
        }
    }
    
    private static URI save(Project project, Project output) throws IOException { 
        File tmpFile = File.createTempFile("ServerProject", ".pprj");
        output.setProjectURI(tmpFile.toURI());
        List errors = new ArrayList();
        output.save(errors);
        if (!errors.isEmpty()) {
            return failed(project, errors);
        }
        String newFileName = tmpFile.getAbsolutePath().replace(".pprj", ".pins");
        return new File(newFileName).toURI();
    }
    
    private static URI failed(Project project, List errors) {
        Log.handleErrors(logger, Level.WARNING, errors);
        return project.getProjectURI();
    }

}
