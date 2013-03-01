package edu.stanford.smi.protege.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseSourcesEditor;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ArchiveManager {

    private static final ArchiveManager THE_INSTANCE = new ArchiveManager();
    private static final DateFormat THE_FORMAT = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
    private static final String TMP_TABLE_NAME = "prtgeTmp";

    private int tableNameIndex = 0;
    
    public static ArchiveManager getArchiveManager() {
        return THE_INSTANCE;
    }

    public static DateFormat getDateFormat() {
        return THE_FORMAT;
    }

    private ArchiveManager() {
        // do nothing    
    }

    //ESCA-JAVA0130 
    public void archive(Project p, String comment) {
        String name = p.getProjectName();
        File projectDir = getProjectDirectoryFile(p);
        File archiveDir = createArchiveDir(name, projectDir, new Date());
        File tempDir = getEmptyTempDir(name, archiveDir);
        moveProject(name, projectDir, tempDir);
        try {
            Collection errors = new ArrayList();
            saveProject(p, errors);
            logErrors(errors);
            moveProject(name, projectDir, archiveDir);
            createComment(archiveDir, comment);
        } finally {
            moveProject(name, tempDir, projectDir);
            tempDir.delete();
        }
    }

    
    private void saveProject(Project prj, Collection errors) {
    	if (prj.getKnowledgeBaseFactory() instanceof DatabaseKnowledgeBaseFactory) {
        	String origTableName = DatabaseKnowledgeBaseFactory.getTableName(prj.getSources());
			String newTableName = DatabaseKnowledgeBaseSourcesEditor.DEFAULT_TABLE_NAME;
        	
    		Connection connection = null;
    		
    		try {
    			connection = getDBConnection(prj);
        		newTableName = getNextTableNameIndex(connection, origTableName);	    		
        		connection.close();
        		
    		} catch (SQLException e) {
    			errors.add(e);
    			throw new RuntimeException("Archive error", e);
    		}
    		
    		copyDBProjectInNewTable(prj, errors, newTableName);   		
    		
    	} else { // any other types of project
    		prj.save(errors);
    	}
    }
    
   private void copyDBProjectInNewTable(Project prj, Collection errors, String newTableName) {	
    	String origTableName = DatabaseKnowledgeBaseFactory.getTableName(prj.getSources());
		DatabaseKnowledgeBaseFactory.setTablename(prj.getSources(), newTableName);
		
		try {
			prj.save(errors);	
		} catch (Exception e) {
			errors.add(e);
			throw new RuntimeException(e);
		} finally {
			DatabaseKnowledgeBaseFactory.setTablename(prj.getSources(), origTableName);
		}
    }
    
    
    private static File getProjectDirectoryFile(Project p) {
        URI uri = p.getProjectURI();
        return (uri == null) ? null : new File(uri).getParentFile();
    }

    private static File createArchiveDir(String name, File projectDir, Date date) {
        File archiveDir = getArchiveDir(name, projectDir, date);
        archiveDir.mkdirs();
        return archiveDir;
    }

    private static File getArchiveDir(String name, File projectDir, Date date) {
        File mainArchiveDir = getMainArchiveDir(name, projectDir);
        return new File(mainArchiveDir, getTimestamp(date));
    }

    private static File getMainArchiveDir(String name, File projectDir) {
        File mainArchiveDir = new File(projectDir, name + ".parc");
        mainArchiveDir.mkdir();
        return mainArchiveDir;
    }

    private static void moveProject(String projectName, File sourceDir, File targetDir) {
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

    
    private static void copyProject(String projectName, File sourceDir, File targetDir) {
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File sourceFile = files[i];
                if (sourceFile.isFile()) {
                    String sourceFileName = sourceFile.getName();
                    if (sourceFileName.startsWith(projectName)) {
                        File targetFile = new File(targetDir, sourceFileName);
                        copyFile(sourceFile, targetFile);
                    }
                }
            }
        }
    }
    
    private static File getEmptyTempDir(String projectName, File archiveDir) {
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

    //ESCA-JAVA0130 
    public Collection getArchiveRecords(Project p) {
        Collection records = new ArrayList();
        File[] file = getMainArchiveDir(p.getProjectName(), getProjectDirectoryFile(p)).listFiles();
        for (int i = 0; i < file.length; ++i) {
            records.add(new ArchiveRecord(file[i], getComment(file[i])));
        }
        return records;
    }

    //ESCA-JAVA0130 
    public Project revertToVersion(Project p, Date date) {
        Project revertedProject = null;
        String name = p.getProjectName();
        File projectDir = getProjectDirectoryFile(p);
        File archiveDir = getArchiveDir(name, projectDir, date);
        File tempDir = getEmptyTempDir(name, archiveDir);
        Collection errors = new ArrayList();
        
        if (p.getKnowledgeBaseFactory() instanceof DatabaseKnowledgeBaseFactory) {
        	//special handling of DB projects
        	revertedProject = revertToDBVersion(p, projectDir, archiveDir, tempDir, errors);
        } else {
        	//any other types of projects        
        	moveProject(name, projectDir, tempDir);
        	moveProject(name, archiveDir, projectDir);
        	try {        		
        		revertedProject = Project.loadProjectFromURI(p.getProjectURI(), errors);        		
        	} finally {
        		moveProject(name, projectDir, archiveDir);
        		moveProject(name, tempDir, projectDir);
        		tempDir.delete();        		
        	}
        }
        
        logErrors(errors);
        
        return revertedProject;
    }


	private Project revertToDBVersion(Project prj, File projectDir, File archiveDir, File tempDir, Collection errors) {
		Project revertedProject = null;
						
		String name = prj.getProjectName();
		String originalTableName = DatabaseKnowledgeBaseFactory.getTableName(prj.getSources());
		 
		//move project_dir pprj and table -> tmp_dir pprj and tmp table
		moveProject(name, projectDir, tempDir); //move only pprj file
		try {		
			renameTable(prj, originalTableName, TMP_TABLE_NAME);		
		} catch (SQLException e) {
			errors.add(e);
			moveProject(name, tempDir, projectDir);
			return null;
		};
		
		//copy archive_dir -> project_dir
		copyProject(name + ".pprj", archiveDir, projectDir);
		try {
			revertedProject = loadAndMoveDBProject(prj, errors);
		} catch (Exception e) {
			errors.add(e);			
			try {
				renameTable(prj, TMP_TABLE_NAME, originalTableName);
				moveProject(name, tempDir, projectDir);
			} catch (SQLException e1) {
				errors.add(e);
				return null;
			}
		} finally {
			moveProject(name, tempDir, projectDir);
			tempDir.delete();
		}		
		
		return revertedProject;		
	}
	
	
	

	private Project loadAndMoveDBProject(Project prj, Collection errors) {    	
    		String originalTableName = DatabaseKnowledgeBaseFactory.getTableName(prj.getSources());
    		
    		Project revertedProject = null;
    		
    		//load project from archived folder
    		try {
				revertedProject = Project.loadProjectFromURI(prj.getProjectURI(), errors);
			} catch (Exception e) {
				errors.add(e);
				throw new RuntimeException(e);
			}
    		
			//set the table name to be the original table name
			DatabaseKnowledgeBaseFactory.setTablename(revertedProject.getSources(), originalTableName);
			
			//save - this will override the content of the original table with the content of the archived table
			revertedProject.save(errors);
			
			//reload project with original table name
			revertedProject = Project.loadProjectFromURI(prj.getProjectURI(), errors);
			
			return revertedProject;    	
    }
    
    
    private static String getTimestamp(Date date) {
        return THE_FORMAT.format(date);
    }

    private static String getComment(File directory) {
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

    private static File getCommentFile(File directory) {
        return new File(directory, "comment.txt");
    }

    private static void createComment(File directory, String comment) {
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
    
    
    /*
     * Utility methods
     */
    
    private void logErrors(Collection errors) {
    	if (errors == null || errors.size() == 0) {
    		return;
    	}
    	
    	Log.getLogger().severe("There were errors at archiving/reverting project");
    	for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
			Object error = (Object) iterator.next();
			if (error instanceof Throwable) {
				Log.getLogger().log(Level.SEVERE, ((Throwable) error).getMessage(), error);
			} else if (error instanceof MessageError) {
				MessageError msgErr = (MessageError) error;
				Log.getLogger().log(Level.SEVERE, msgErr.getMessage(), msgErr.getException());
			} else {
				Log.getLogger().severe(error.toString());
			}
		}
    }
    
    
    private static void copyFile(File inputFile, File outputFile) {	   
    	try {
    		FileReader in = new FileReader(inputFile);
    		FileWriter out = new FileWriter(outputFile);
    		int c;

    		while ((c = in.read()) != -1)
    			out.write(c);

    		in.close();
    		out.close();			
    	} catch (FileNotFoundException e) {
    		Log.getLogger().log(Level.WARNING, "Error at copying file " + inputFile + " to " + outputFile, e);			
    	} catch (IOException e) {
    		Log.getLogger().log(Level.WARNING, "Error at copying file " + inputFile + " to " + outputFile, e);
    	}	 
    }
  
    
    /*
     * DB access methods
     */    
    
    private static Connection getDBConnection(Project prj) throws SQLException {        	 
    	String url = DatabaseKnowledgeBaseFactory.getURL(prj.getSources());
    	String userName = DatabaseKnowledgeBaseFactory.getUsername(prj.getSources());
    	String password = DatabaseKnowledgeBaseFactory.getPassword(prj.getSources());

    	Connection connection = null;
    	
    	connection = DriverManager.getConnection(url, userName, password);   	

    	return connection;
    }

	private String getNextTableNameIndex(Connection connection, String origName) {
    	String tableName = origName + Integer.toString(tableNameIndex);
    	
    	while (tableExists(connection, tableName)) {
    		tableNameIndex++;
    		tableName = origName + Integer.toString(tableNameIndex);
    	}
    	
    	return tableName;
    }
        
    
    private static boolean tableExists(Connection connection, String tableName) {
        boolean exists = false;
                 
        try{
             Statement statement = connection.createStatement();
             String query = "SELECT COUNT(*) FROM " + tableName;
             ResultSet rs = statement.executeQuery(query);
             rs.close();
             statement.close();
             exists = true;       
             
         } catch (SQLException e) {
             //do nothing
         }

        return exists;
    }
    
    private static void renameTable(Project prj, String tableName, String newTableName) throws SQLException {    	
		Connection connection = getDBConnection(prj);
		renameTable(connection, tableName, newTableName);
		connection.close();		
    }
    
    private static void renameTable(Connection connection, String tableName, String newTableName) throws SQLException {
    	try {
    		Statement statement = connection.createStatement();
    		String query = "DROP TABLE " + newTableName;
    		boolean s = statement.execute(query);    		
    		statement.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		//it's OK to fail if table does not exist
    	}    	

    	Statement statement = connection.createStatement();
    	String query = "RENAME TABLE " + tableName + " TO " + newTableName;
    	boolean s = statement.execute(query);    	
    	statement.close();             

    }
    
    private static void deleteTable(Project prj, String tableName) {
    	try {
    		Connection connection = getDBConnection(prj);
    		Statement statement = connection.createStatement();
    		String query = "DROP " + tableName;
    		ResultSet rs = statement.executeQuery(query);
    		rs.close();
    		statement.close();
    		connection.close();
    	} catch (SQLException e) {
    		//it's OK to fail if table does not exist
    	}  
    }
    
}
