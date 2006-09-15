package edu.stanford.smi.protege.storage.database;

import java.net.URI;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory2;
import edu.stanford.smi.protege.model.KnowledgeBaseSourcesEditor;
import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.PropertyList;

/**
 * Description of the class
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DatabaseKnowledgeBaseFactory implements KnowledgeBaseFactory2 {
    public static final String DESCRIPTION = Text.getProgramName() + " Database";
    static final String USERNAME_PROPERTY = "username";
    static final String PASSWORD_PROPERTY = "password";
    static final String URL_PROPERTY = "url";
    static final String DRIVER_PROPERTY = "driver";
    static final String TABLENAME_PROPERTY = "table";

    /*
     * This variable indicates tells the routines that handle the 
     * inclusion of projects to ignore the isIncluded flag on frames.
     * In particular, this occurs for OWL databases projects, which
     * currently never include other projects.  When a file project
     * has imports, these imports are reflected in includes of the OWL
     * projects.  However when such a file project is converted to
     * database mode, all the included projects are put in a single
     * database project.
     *
     * TODO - We probably should fix the database inclusion mechanism
     *        so that it is implemented in terms of included frames
     *        projects.  One thing that would need to be figured out
     *        to do this to determine what triples are provided by the
     *        included projects and what triples are provided in the
     *        including projects.
     */
    private boolean owlMode = false;

    public KnowledgeBase createKnowledgeBase(Collection errors) {
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase(this);
        return kb;
    }

    public KnowledgeBaseSourcesEditor createKnowledgeBaseSourcesEditor(String projectName, PropertyList sources) {
        return new DatabaseKnowledgeBaseSourcesEditor(projectName, sources);
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public static String getDriver(PropertyList sources) {
        return sources.getString(DRIVER_PROPERTY);
    }

    public static String getPassword(PropertyList sources) {
        return sources.getString(PASSWORD_PROPERTY);
    }

    public String getProjectFilePath() {
        return null;
    }

    public static String getTableName(PropertyList sources) {
        return sources.getString(TABLENAME_PROPERTY);
    }

    public static String getURL(PropertyList sources) {
        return sources.getString(URL_PROPERTY);
    }

    public static String getUsername(PropertyList sources) {
        return sources.getString(USERNAME_PROPERTY);
    }
    
    public void includeKnowledgeBase(KnowledgeBase kb, 
                                     PropertyList sources, 
                                     Collection errors) {
      String driver = getDriver(sources);
      String url = getURL(sources);
      if (driver != null && url != null) {
          String username = getUsername(sources);
          String password = getPassword(sources);
          String tablename = getTableName(sources);
          includeKnowledgeBase(kb, driver, url, username, password, tablename, errors);
      }
    }

    public boolean isComplete(PropertyList sources) {
        String tablename = getTableName(sources);
        String url = getURL(sources);
        String driver = getDriver(sources);
        return tablename != null && url != null && driver != null;
    }
    
    public void loadKnowledgeBase(KnowledgeBase kb, 
                                  PropertyList sources, 
                                  Collection errors) {
      String driver = getDriver(sources);
      String url = getURL(sources);
      if (driver != null && url != null) {
          String username = getUsername(sources);
          String password = getPassword(sources);
          String tablename = getTableName(sources);
          loadKnowledgeBase(kb, driver, url, username, password, tablename, errors);
      }
    }

    public void includeKnowledgeBase(KnowledgeBase kb, 
                                     String driver, 
                                     String url, 
                                     String user, 
                                     String password,
                                     String table,
                                     Collection errors) {
        try {
          initializeKB(kb, driver, url, user, password, table, true);
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Unable to load included knowledgebase", e);
            errors.add(e);
        }
    }

    private static MergingNarrowFrameStore getMergingFrameStore(DefaultKnowledgeBase kb) {
        return MergingNarrowFrameStore.get(kb);
    }

    public void loadKnowledgeBase(KnowledgeBase kb,
                                  String driver, 
                                  String url, 
                                  String user, 
                                  String password,
                                  String table,
                                  Collection errors) {
        try {
          initializeKB(kb, driver, url, user, password, table, false);
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Unable to load knowledgebase", e);
            errors.add(e);
        }
    }

    //ESCA-JAVA0130 
   

    public void saveKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        if (kb instanceof DefaultKnowledgeBase) {
            DatabaseFrameDb databaseFrameDb = getDatabaseFrameDb(kb);
            if (databaseFrameDb == null) {
                copyKnowledgeBase(kb, sources, errors);
            } else if (!databaseFrameDb.getTableName().equals(getTableName(sources))) {
                copyKnowledgeBase(kb, sources, errors);
            }
        } else {
            copyKnowledgeBase(kb, sources, errors);
        }
    }

    private static DatabaseFrameDb getDatabaseFrameDb(KnowledgeBase kb) {
        NarrowFrameStore nfs = MergingNarrowFrameStore.get(kb); // Assumes this is the top
                                                                // of the narrow frame stores.
        while ((nfs = nfs.getDelegate()) != null) {
          if (nfs instanceof DatabaseFrameDb) {
            return (DatabaseFrameDb) nfs;
          }
        }
        return null;
    }
    
    private static IncludingDatabaseAdapter getIncludingDatabaseAdapter(KnowledgeBase kb) {
      NarrowFrameStore nfs = MergingNarrowFrameStore.get(kb); // Assumes this is the top
                                                              // of the narrow frame stores.
      while ((nfs = nfs.getDelegate()) != null) {
        if (nfs instanceof IncludingDatabaseAdapter) {
          return (IncludingDatabaseAdapter) nfs;
        }
      }
      return null;
    }

    public static void setDriver(PropertyList sources, String driver) {
        sources.setString(DRIVER_PROPERTY, driver);
    }

    public static void setPassword(PropertyList sources, String password) {
        sources.setString(PASSWORD_PROPERTY, password);
    }

    public static void setTablename(PropertyList sources, String tablename) {
        sources.setString(TABLENAME_PROPERTY, tablename);
    }

    public static void setURL(PropertyList sources, String url) {
        sources.setString(URL_PROPERTY, url);
    }

    public static void setUsername(PropertyList sources, String username) {
        sources.setString(USERNAME_PROPERTY, username);
    }

    public static void setSources(PropertyList sources, String driver, String url, String table, String user,
            String password) {
        setDriver(sources, driver);
        setURL(sources, url);
        setTablename(sources, table);
        setUsername(sources, user);
        setPassword(sources, password);
    }

    public void prepareToSaveInFormat(KnowledgeBase kb, KnowledgeBaseFactory factory, Collection errors) {
      return;
    }

    public IncludingDatabaseAdapter createNarrowFrameStore(String name) {
      DatabaseFrameDb store = new DatabaseFrameDb();
      ValueCachingNarrowFrameStore vcnfs = new ValueCachingNarrowFrameStore(store);
      IncludingDatabaseAdapter ida = new IncludingDatabaseAdapter(vcnfs);
      ida.setName(name);
      return ida;
    }
    
    protected void insertKB(KnowledgeBase kb, String name, Collection<URI> included) {
      NarrowFrameStore nfs = createNarrowFrameStore(name);
      MergingNarrowFrameStore mnfs = getMergingFrameStore((DefaultKnowledgeBase) kb);
      mnfs.addActiveFrameStore(nfs, included);
    }
    
    protected void initializeKB(KnowledgeBase kb, 
                              String driver, 
                              String url, 
                              String user, 
                              String password,
                              String table,
                              boolean isInclude) {
        DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
        FrameFactory factory = dkb.getFrameFactory();
        IncludingDatabaseAdapter ida = getIncludingDatabaseAdapter(dkb);
        ida.initialize(factory, driver, url, user, password, table, isInclude);
        kb.flushCache();
    }
    
    private void copyKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
      String driver = getDriver(sources);
      String url = getURL(sources);
      String username = getUsername(sources);
      String password = getPassword(sources);
      String tablename = getTableName(sources);
      copyKnowledgeBase(kb, driver, url, username, password, tablename, errors);
  }

  private void copyKnowledgeBase(KnowledgeBase inputKb, 
                                 String driver, 
                                 String url, 
                                 String username, 
                                 String password,
                                 String tablename, 
                                 Collection errors) {
      try {
        DatabaseWriter dbw = new DatabaseWriter(inputKb,
                                                driver, url, 
                                                username, password, 
                                                tablename);
        dbw.setOwlMode(owlMode);
        dbw.save();
        /*
          DefaultKnowledgeBase outputKb = new DefaultKnowledgeBase();
          Collection<URI> uris = inputKb.getProject().getDirectIncludedProjectURIs();
          for (URI uri : uris) {
            outputKb.getProject().includeProject(uri, errors);
          }
          insertKB(outputKb, " <new> ", uris);
          initializeKB(outputKb, driver, url, username, password, tablename, false);
          getIncludingDatabaseAdapter(outputKb).overwriteKB(inputKb);
          */
      } catch (Exception e) {
          errors.add(e);
      }
  }

  protected void setOwlMode(boolean owlMode) {
      this.owlMode = owlMode;
  }

  public boolean owlMode() {
      return owlMode;
  }
}