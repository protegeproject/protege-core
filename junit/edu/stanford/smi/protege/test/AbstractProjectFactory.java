package edu.stanford.smi.protege.test;

import java.io.*;
import java.net.*;
import java.util.*;

import junit.framework.Assert;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractProjectFactory implements ProjectFactory {

    protected static void checkErrors(Collection c) {
        if (!c.isEmpty()) {
            Iterator i = c.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                Log.getLogger().severe("Error: " + o.toString());
            }
            Assert.fail();
        }
    }

    private static File getTempDirectory() {
        return AbstractTestCase.getTempDirectory();
    }

    protected static URI getProjectURI() {
        URI projectURI = null;
        try {
            File tmpFile = File.createTempFile("test", ".pprj", getTempDirectory());
            projectURI = tmpFile.toURI();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return projectURI;
    }

    public Project saveAndReloadProject(Project project) {
        URI uri = project.getProjectURI();
        if (uri == null) {
            uri = getProjectURI();
            project.setProjectURI(uri);
        }
        Collection errors = new ArrayList();
        int frameCount = project.getKnowledgeBase().getFrameCount();
        project.save(errors);
        checkErrors(errors);
        project.dispose();
        project = Project.loadProjectFromURI(uri, errors);
        checkErrors(errors);
        Assert.assertEquals(frameCount, project.getKnowledgeBase().getFrameCount());
        return project;
    }
}
