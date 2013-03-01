package edu.stanford.smi.protege.model;
//ESCA*JAVA0037

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.util.*;

/**
 * Base class that provides convenience and template methods for backend developers to extend. This "editor" is a panel
 * in which the backend developer can prompt for file names and any other information that the backend needs. See the
 * {@link edu.stanford.smi.protege.storage.clips.FileSourcesPanel Clips}and the
 * {@link edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseSourcesEditor JDBC}editors for examples of the
 * use of this class.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class KnowledgeBaseSourcesEditor extends JComponent implements Validatable {
    private static final long serialVersionUID = 3820797792113838686L;
    private PropertyList _sources;
    private FileField _projectPathField;
    private URIList _includedProjectsList;
    private String _oldProjectPath;
    private boolean _showingProject;

    protected KnowledgeBaseSourcesEditor(String projectPath, PropertyList sources) {
        _sources = sources;
        if (projectPath != null) {
            try {
                _oldProjectPath = new File(URI.create(projectPath)).getAbsolutePath();
            } catch (Exception e) {
                // do nothing
            }
        }
        setLayout(new BorderLayout(10, 10));
        add(createProjectPathField(), BorderLayout.NORTH);
        createIncludedProjectsList();
    }

    public void setShowIncludedProjects(boolean b) {
        if (_includedProjectsList != null) {
            _includedProjectsList.setVisible(b);
        }
    }

    public JComponent createIncludedProjectsList() {
        _includedProjectsList = new URIList("Included Projects", null, ".pprj", "Project Files");
        return _includedProjectsList;
    }

    public JComponent createProjectPathField() {
        _projectPathField = new FileField("Project", _oldProjectPath, ".pprj", "Project Files");
        _projectPathField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                String newProjectPath = getProjectPath();
                onProjectPathChange(_oldProjectPath, newProjectPath);
                _oldProjectPath = newProjectPath;
            }
        });
        _projectPathField.setDialogType(JFileChooser.SAVE_DIALOG);
        return _projectPathField;
    }

    /**
     * 
     * @return Collection of URI's for included projects
     */
    public Collection getIncludedProjects() {
        return (_includedProjectsList == null) ? Collections.EMPTY_LIST : _includedProjectsList.getURIs();
    }

    public String getProjectPath() {
        return getProjectPathFromProjectField();
    }

    private String getProjectPathFromProjectField() {
        String path = null;
        if (_showingProject) {
            path = _projectPathField.getPath();
            if (path != null) {
                path = FileUtilities.ensureExtension(path, ".pprj");
            }
        }
        return path;
    }

    public PropertyList getSources() {
        return _sources;
    }

    public void setShowProject(boolean showProject) {
        _showingProject = showProject;
        remove(_projectPathField);
        if (_includedProjectsList != null) {
            remove(_includedProjectsList);
        }
        if (showProject) {
            add(_projectPathField, BorderLayout.NORTH);
        } else if (_includedProjectsList != null) {
            add(_includedProjectsList, BorderLayout.NORTH);
        }
        revalidate();
    }

    public boolean isShowingProject() {
        return _showingProject;
    }

    //ESCA-JAVA0130 
    protected boolean hasValidValue(URIField field) {
        boolean hasValidValue;
        URI value = field.getRelativeURI();
        if (value == null) {
            hasValidValue = false;
        } else {
            // other tests???
            hasValidValue = true;
        }
        return hasValidValue;
    }

    protected void onProjectPathChange(String oldPath, String newPath) {
    }

    //ESCA-JAVA0130 
    protected void updatePath(FileField field, String newBasePath, String ext) {
        String name = new File(newBasePath).getName();
        String fieldText = FileUtilities.replaceExtension(name, ext);
        field.setPath(fieldText);
    }

    protected String getBaseFile(FileField field) {
        String s = field.getPath();
        if (_showingProject) {
            int index = s.lastIndexOf(File.separatorChar);
            s = s.substring(index + 1);
        }
        return s;
    }
}