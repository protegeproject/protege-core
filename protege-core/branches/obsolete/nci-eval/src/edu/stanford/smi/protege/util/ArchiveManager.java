package edu.stanford.smi.protege.util;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ArchiveManager {

    private static final ArchiveManager _theInstance = new ArchiveManager();
    private static final DateFormat _theFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

    public static ArchiveManager getArchiveManager() {
        return _theInstance;
    }
    public static DateFormat getDateFormat() {
        return _theFormat;
    }
    private ArchiveManager() {
        // do nothing    
    }

    public void archive(Project p, String comment) {
        String name = p.getProjectName();
        File projectDir = getProjectDirectoryFile(p);
        File archiveDir = createArchiveDir(name, projectDir, new Date());
        File tempDir = getEmptyTempDir(name, archiveDir);
        moveProject(name, projectDir, tempDir);
        try {
            Collection errors = new ArrayList();
            p.save(errors);
            moveProject(name, projectDir, archiveDir);
            createComment(archiveDir, comment);
        } finally {
            moveProject(name, tempDir, projectDir);
            tempDir.delete();
        }
    }

    private File getProjectDirectoryFile(Project p) {
        URI uri = p.getProjectURI();
        return (uri == null) ? null : new File(uri).getParentFile();
    }

    private File createArchiveDir(String name, File projectDir, Date date) {
        File archiveDir = getArchiveDir(name, projectDir, date);
        archiveDir.mkdirs();
        return archiveDir;
    }

    private File getArchiveDir(String name, File projectDir, Date date) {
        File mainArchiveDir = getMainArchiveDir(name, projectDir);
        return new File(mainArchiveDir, getTimestamp(date));
    }

    private File getMainArchiveDir(String name, File projectDir) {
        File mainArchiveDir = new File(projectDir, name + ".parc");
        mainArchiveDir.mkdir();
        return mainArchiveDir;
    }

    private void moveProject(String projectName, File sourceDir, File targetDir) {
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File sourceFile = files[i];
                if (sourceFile.isFile()) {
                    String sourceFileName = sourceFile.getName();
                    if (sourceFileName.startsWith(projectName)) {
                        File targetFile = new File(targetDir, sourceFileName);
                        sourceFile.renameTo(targetFile);
                    }
                }
            }
        }
    }

    private File getEmptyTempDir(String projectName, File archiveDir) {
        File tempDir = new File(archiveDir, "temp");
        boolean created = tempDir.mkdir();
        if (!created) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; ++i) {
                    files[i].delete();
                }
            }
        }
        return tempDir;
    }

    public Collection getArchiveRecords(Project p) {
        Collection records = new ArrayList();
        File[] file = getMainArchiveDir(p.getProjectName(), getProjectDirectoryFile(p)).listFiles();
        for (int i = 0; i < file.length; ++i) {
            records.add(new ArchiveRecord(file[i], getComment(file[i])));
        }
        return records;
    }

    public Project revertToVersion(Project p, Date date) {
        Project revertedProject = null;
        String name = p.getProjectName();
        File projectDir = getProjectDirectoryFile(p);
        File archiveDir = getArchiveDir(name, projectDir, date);
        File tempDir = getEmptyTempDir(name, archiveDir);
        moveProject(name, projectDir, tempDir);
        moveProject(name, archiveDir, projectDir);
        try {
            Collection errors = new ArrayList();
            revertedProject = Project.loadProjectFromURI(p.getProjectURI(), errors);
        } finally {
            moveProject(name, projectDir, archiveDir);
            moveProject(name, tempDir, projectDir);
            tempDir.delete();
        }
        return revertedProject;
    }

    private String getTimestamp(Date date) {
        return _theFormat.format(date);
    }

    private String getComment(File directory) {
        String comment = null;
        File file = getCommentFile(directory);
        if (file != null) {
            try {
                BufferedReader reader = FileUtilities.createBufferedReader(file);
                comment = reader.readLine();
                reader.close();
            } catch (Exception e) {
                // do nothing
            }
        }
        return comment;
    }

    private File getCommentFile(File directory) {
        return new File(directory, "comment.txt");
    }

    private void createComment(File directory, String comment) {
        if (comment != null && comment.length() > 0) {
            File file = getCommentFile(directory);
            try {
                PrintWriter pw = FileUtilities.createPrintWriter(file, false);
                pw.println(comment);
                pw.close();
            } catch (Exception e) {
                Log.getLogger().warning(e.toString());
            }
        }
    }
}
