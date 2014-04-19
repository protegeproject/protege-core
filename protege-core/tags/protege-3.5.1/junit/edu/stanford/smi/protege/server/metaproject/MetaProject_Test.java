package edu.stanford.smi.protege.server.metaproject;

import edu.stanford.smi.protege.model.framestore.SimpleTestCase;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.URIUtilities;

public class MetaProject_Test extends SimpleTestCase {

	  public static final String METAPROJECT = "junit/pprj/policy/metaproject.pprj";

	  public static final String NATASHA  = "Natasha Noy";
	  public static final String NEWSPAPER = "Newspaper";

	  public static final String NEW_USER_NAME = "MyUser";
	  public static final String NEW_USER_PASS = "MyPassword";

	  public static final String NEW_PROJECT_NAME = "MyProject";
	  public static final String NEW_PROJECT_LOCATION = "in_same_directory.pprj";

	  public static final String NEW_OPERATION_NAME = "MyOperation";
	  public static final Operation ALT_READ = new UnbackedOperationImpl("AltRead", null);

	  public static final String NEW_GROUP_NAME = "MyGroup";

	  public static final String PROP_NAME = "prop1";
	  public static final String PROP_VALUE = "value1";


	  public void test_createUser() {
		  MetaProject mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));

		  assertNotNull(mp.getUser(NATASHA));

		  User user = mp.createUser(NEW_USER_NAME, NEW_USER_PASS);
		  assertTrue(mp.getUsers().contains(user));

		  User sameUser = mp.getUser(NEW_USER_NAME);
		  assertTrue(sameUser.equals(user));
	  }

	  public void test_createProject() {
		  MetaProject mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));


		  ProjectInstance project = mp.createProject(NEW_PROJECT_NAME);
		  assertTrue(mp.getProjects().contains(project));
		  assertEquals(project.getName(), NEW_PROJECT_NAME);

		  ProjectInstance sameProject = mp.getProject(NEW_PROJECT_NAME);
		  assertTrue(sameProject.equals(project));

		  project.setLocation(NEW_PROJECT_LOCATION);
		  assertEquals(NEW_PROJECT_LOCATION, project.getLocation());

		  Log.getLogger().info("Create project: " + project.getName() + " in location: " + project.getLocation());

	  }


	  public void test_createOperation() {
		  MetaProject mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));

		  Operation op = mp.createOperation(NEW_OPERATION_NAME);
		  assertTrue(mp.getOperations().contains(op));

		  Operation sameOp = mp.getOperation(NEW_OPERATION_NAME);
		  assertTrue(sameOp.equals(op));
	  }


	  public void test_createGroup() {
		  MetaProject mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));

		  Group g = mp.createGroup(NEW_GROUP_NAME);
		  assertTrue(mp.getGroups().contains(g));

		  Group sameG = mp.getGroup(NEW_GROUP_NAME);
		  assertTrue(sameG.equals(g));

		  assertTrue(mp.getGroups().contains(g));

		  User user = mp.createUser(NEW_USER_NAME, NEW_USER_PASS);
		  User natasha = mp.getUser(NATASHA);

		  g.addMember(user);
		  g.addMember(natasha);

		  assertEquals(g.getMembers().size(), 2);

	  }


	  public void test_createPolicies() {
		  MetaProject mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));

		  ProjectInstance project = mp.createProject(NEW_PROJECT_NAME);
		  ProjectInstance newspaper = mp.getProject(NEWSPAPER);

		  newspaper.setAnnotationProject(project);
		  assertEquals(project, newspaper.getAnnotationProject());

		  User user = mp.createUser(NEW_USER_NAME, NEW_USER_PASS);
		  User natasha = mp.getUser(NATASHA);

		  Group g = mp.createGroup(NEW_GROUP_NAME);
		  g.addMember(user);
		  g.addMember(natasha);

		  Operation op = mp.createOperation(NEW_OPERATION_NAME);

		  GroupOperation groupOp = mp.createGroupOperation();
		  groupOp.setAllowedGroup(g);
		  groupOp.addAllowedOperation(op);
		  groupOp.addAllowedOperation(mp.getOperation(ALT_READ.getName()));

		  assertTrue(groupOp.getAllowedOperations().size() == 2);

		  project.addAllowedGroupOperations(groupOp);

		  Policy policy = mp.getPolicy();

		  Policy_Test.checkAuthorization(policy, natasha, op, project, true);
		  Policy_Test.checkAuthorization(policy, user, op, project, true);

		  Policy_Test.checkAuthorization(policy, user, MetaProjectConstants.OPERATION_WRITE, project, false);
	  }

	  public void test_Properties() {
	      MetaProject mp = new MetaProjectImpl(URIUtilities.createURI(METAPROJECT));

	      User user = mp.createUser(NEW_USER_NAME, NEW_USER_PASS);
	      assertTrue(user.getPropertyValues().size() == 0);

	      user.addPropertyValue(PROP_NAME, PROP_VALUE);

	      assertTrue(user.getPropertyValues().size() == 1);

	      String value = user.getPropertyValue(PROP_NAME);
	      assertEquals(PROP_VALUE, value);

	      user.removePropertyValue(PROP_NAME, PROP_VALUE);
	      assertTrue(user.getPropertyValues().size() == 0);
	  }

}
