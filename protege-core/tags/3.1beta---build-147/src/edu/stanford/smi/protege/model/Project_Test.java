package edu.stanford.smi.protege.model;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.test.*;

/**
 * Units tests for Project class.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Project_Test extends APITestCase {
    private static final String SUBDIR = "project_subdir";

    private static final String MAP_NAME = "Project_Test.test_map";
    private static final String HTTP_BASE = "http://protege.stanford.edu/";
    private static final String DIR = "applet_demo/Newspaper/";
    private static final String FILE_BASE = "file:/u:/protege_web/";
    private static final String JAR_PROJECT = "newspaper.jar!/samples/examples/newspaper/newspaper.pprj";

    private static final String HTTP_PROJECT_STRING = HTTP_BASE + DIR + "newspaper.pprj";
    private static final String HTTP_JAR_PROJECT_STRING = "jar:" + HTTP_BASE + DIR + JAR_PROJECT;
    private static final String FILE_JAR_PROJECT_STRING = "jar:" + FILE_BASE + DIR + JAR_PROJECT;

    private static final String INCLUDED_PROJECT_NAME = "included";
    private static final String MAIN_PROJECT_NAME = "main";

    public void testPropertyMapLoading() {
        Frame frame = createFrame();
        String frameName = frame.getName();
        Map testMap = new HashMap();
        setClientInformation(MAP_NAME, testMap);
        Rectangle savedRect = new Rectangle(1, 2, 3, 4);
        testMap.put(frame, savedRect);
        testMap.put("foo", "bar");

        saveAndReload();

        frame = getFrame(frameName);
        testMap = (Map) getClientInformation(MAP_NAME);
        assertNotNull("map exists", testMap);
        assertEquals("string", "bar", testMap.get("foo"));
        assertEquals("rectangle", savedRect, testMap.get(frame));
    }

    public void testProjectURISaveLoad() {
        Project p = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        assertNotNull(p);
    }

    private File getTempSubdirectory() {
        return getTempSubdirectory(SUBDIR);
    }

    private void deleteTempSubdirectory() {
        deleteTempSubdirectory(SUBDIR);
    }

    private Project createProjectOnDisk(File directory, String name) {
        Collection errors = new ArrayList();
        Project project = Project.createNewProject(null, errors);
        checkErrors(errors);
        URI projectURI = new File(directory, name + ".pprj").toURI();
        project.setProjectURI(projectURI);
        KnowledgeBase kb = project.getKnowledgeBase();
        Cls cls = kb.createCls(null, kb.getRootClses());
        Slot slot = kb.createSlot(null);
        cls.addDirectTemplateSlot(slot);
        kb.createInstance(null, cls);
        int frameCount = kb.getFrameCount();
        project.save(errors);
        checkErrors(errors);
        project.dispose();
        project = Project.loadProjectFromURI(projectURI, errors);
        checkErrors(errors);
        kb = project.getKnowledgeBase();
        assertEquals(frameCount, kb.getFrameCount());
        return project;
    }

    public void testProjectLoadFromFile() {
        Project p = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        int count = p.getKnowledgeBase().getFrameCount();
        URI uri = p.getProjectURI();
        Collection errors = new ArrayList();
        p = Project.loadProjectFromFile(new File(uri).getPath(), errors);
        checkErrors(errors);
        int count2 = p.getKnowledgeBase().getFrameCount();
        assertEquals(count, count2);
    }

    public void testProjectLoadFromHttpJar() {
        loadProjectFromURI(HTTP_JAR_PROJECT_STRING);
    }

    private Project loadProjectFromURI(String uriString) {
        URI uri = null;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            fail();
        }
        Project p = loadProjectFromURI(uri);
        Cls cls = p.getKnowledgeBase().getCls("Editor");
        assertNotNull(cls);
        return p;
    }

    private Project loadProjectFromURI(URI uri) {
        Collection errors = new ArrayList();
        Project p = Project.loadProjectFromURI(uri, errors);
        checkErrors(errors);
        return p;
    }

    public void testProjectLoadFromHttp() {
        loadProjectFromURI(HTTP_PROJECT_STRING);
    }

    public void testProjectLoadFromFileJar() {
        loadProjectFromURI(FILE_JAR_PROJECT_STRING);
    }

    public void testIncludeFromSameDirectory() {
        Project p1 = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        Project p2 = createProjectOnDisk(getTempDirectory(), INCLUDED_PROJECT_NAME);
        checkInclusion(p2, p1);
    }

    private void checkInclusion(Project includingProject, Project includedProject) {
        URI projectURI = includingProject.getProjectURI();
        Collection errors = new ArrayList();
        includingProject.includeProject(includedProject.getProjectURI(), errors);
        checkErrors(errors);
        int startFrameCount = includingProject.getKnowledgeBase().getFrameCount();
        includingProject.save(errors);
        checkErrors(errors);
        Project p = loadProjectFromURI(projectURI);
        int endFrameCount = p.getKnowledgeBase().getFrameCount();
        assertEquals(startFrameCount, endFrameCount);
    }

    public void testIncludeFromSubdirectory() {
        Project p1 = createProjectOnDisk(getTempSubdirectory(), INCLUDED_PROJECT_NAME);
        Project p2 = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        checkInclusion(p2, p1);
    }

    public void testIncludeFromSuperdirectory() {
        Project p1 = createProjectOnDisk(getTempDirectory(), INCLUDED_PROJECT_NAME);
        Project p2 = createProjectOnDisk(getTempSubdirectory(), MAIN_PROJECT_NAME);
        checkInclusion(p2, p1);
    }

    public void testIncludeWithSaveToSubdirectory() {
        Project p = createProjectOnDisk(getTempSubdirectory(), MAIN_PROJECT_NAME);
        URI targetURI = p.getProjectURI();
        deleteTempSubdirectory();
        Project p1 = createProjectOnDisk(getTempDirectory(), INCLUDED_PROJECT_NAME);
        Project p2 = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        checkInclusion(p2, p1);
        getTempSubdirectory();
        p2.setProjectURI(targetURI);
        saveAndReload(p);
    }

    private Project saveAndReload(Project p) {
        Collection errors = new ArrayList();
        int frameCount = p.getKnowledgeBase().getFrameCount();
        URI uri = p.getProjectURI();
        p.save(errors);
        checkErrors(errors);
        p = Project.loadProjectFromURI(uri, errors);
        checkErrors(errors);
        int frameCount2 = p.getKnowledgeBase().getFrameCount();
        assertEquals(frameCount, frameCount2);
        return p;
    }

    public void testIncludeWithSaveToSuperdirectory() {
        Project p = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        URI targetURI = p.getProjectURI();
        deleteTempSubdirectory();
        Project p1 = createProjectOnDisk(getTempSubdirectory(), INCLUDED_PROJECT_NAME);
        Project p2 = createProjectOnDisk(getTempSubdirectory(), MAIN_PROJECT_NAME);
        checkInclusion(p2, p1);
        p2.setProjectURI(targetURI);
        saveAndReload(p);
    }

    public void testIncludeFromHttp() {
        Project p1 = loadProjectFromURI(HTTP_PROJECT_STRING);
        Project p2 = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        checkInclusion(p2, p1);
    }

    public void testIncludeFromFileJar() {
        Project p1 = loadProjectFromURI(FILE_JAR_PROJECT_STRING);
        Project p2 = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        checkInclusion(p2, p1);
    }

    public void testIncludeFromHttpJar() {
        Project p1 = loadProjectFromURI(HTTP_JAR_PROJECT_STRING);
        Project p2 = createProjectOnDisk(getTempDirectory(), MAIN_PROJECT_NAME);
        checkInclusion(p2, p1);
    }

    public void testIsDirtyOnCreateInstance() {
        Project p = getProject();
        p.clearIsDirty();
        KnowledgeBase kb = p.getKnowledgeBase();
        p.getKnowledgeBase().createInstance(null, kb.getRootCls());
        assertTrue(p.isDirty());
    }
}