package edu.stanford.smi.protege.storage.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.PropertyList;



public class FileToDatabaseTest extends APITestCase {
    private Logger log = Log.getLogger(FileToDatabaseTest.class);

    
    public void testFileToDatabaseConversion() {
        Collection errors  = new  ArrayList();
        for (DBType dbt : DBType.values()) {
            APITestCase.setDBType(dbt);
            if (!APITestCase.dbConfigured()) {
                continue;
            }
            Project p = new Project("examples/wines/wines.pprj", errors);
            handleErrors(errors);
            KnowledgeBase kb = p.getKnowledgeBase();

            KnowledgeBase dkb = convert(p);
            assertNotNull(dkb.getCls("Beaujolais"));
        }
    }
    
    private KnowledgeBase convert(Project fileProject) {
        Collection errors  = new  ArrayList();
        fileProject.setProjectFilePath("junit/pprj/wines-db.pprj");
        DatabaseKnowledgeBaseFactory factory = new DatabaseKnowledgeBaseFactory();
        PropertyList sources = PropertyList.create(fileProject.getInternalProjectKnowledgeBase());
        DatabaseKnowledgeBaseFactory.setSources(sources, 
                                                APITestCase.getDBProperty(APITestCase.JUNIT_DB_DRIVER_PROPERTY), 
                                                APITestCase.getDBProperty(APITestCase.JUNIT_DB_URL_PROPERTY), 
                                                "JunitNewspaper", 
                                                APITestCase.getDBProperty(APITestCase.JUNIT_DB_USER_PROPERTY), 
                                                APITestCase.getDBProperty(APITestCase.JUNIT_DB_PASSWORD_PROPERTY));
        factory.saveKnowledgeBase(fileProject.getKnowledgeBase(), sources, errors);
        handleErrors(errors);
        fileProject.dispose();
        
        Project dbProject = Project.createNewProject(factory, errors);
        DatabaseKnowledgeBaseFactory.setSources(dbProject.getSources(), 
                                                APITestCase.getDBProperty(APITestCase.JUNIT_DB_DRIVER_PROPERTY), 
                                                APITestCase.getDBProperty(APITestCase.JUNIT_DB_URL_PROPERTY), 
                                                "JunitNewspaper", 
                                                APITestCase.getDBProperty(APITestCase.JUNIT_DB_USER_PROPERTY), 
                                                APITestCase.getDBProperty(APITestCase.JUNIT_DB_PASSWORD_PROPERTY));
        dbProject.createDomainKnowledgeBase(factory, errors, true);
        handleErrors(errors);
        
        dbProject.save(errors);
        handleErrors(errors);
        
        return dbProject.getKnowledgeBase();
    }
    
    private void handleErrors(Collection errors) {
        if (!errors.isEmpty()) {
            for (Object o : errors) {
                if  (o instanceof Throwable) {
                    log.log(Level.WARNING, "exception caught", (Throwable)  o);
                }
                else {
                    log.warning("Error = " + o);
                }
            }
            fail();
        }
    }
    

}
