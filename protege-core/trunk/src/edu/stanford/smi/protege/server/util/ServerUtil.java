package edu.stanford.smi.protege.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.server.metaproject.impl.WrappedProtegeInstanceImpl;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;
import edu.stanford.smi.protege.util.ArchiveManager;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;

public class ServerUtil {

	/**
	 * Backwards compatibility check for the metaproject.
	 * Add missing classes and slots for new version of metaproject.
	 * @param metaproject
	 */
	public static void fixMetaProject(MetaProject metaproject) {
		if (!(metaproject instanceof MetaProjectImpl)) { return; }
		boolean changed = false;
		MetaProjectImpl mp = (MetaProjectImpl) metaproject;
		try {
		    changed = addPolicyControlledObjectClass(mp);
		    changed = addAccessSlots(mp) || changed;
		    changed = changeGroup(mp) || changed;
		    changed = addEmail(mp) || changed;
		    changed = addSalt(mp) || changed;
		    changed = addPropertyValues(mp) || changed;

			/* attempt to use getDesignTimeClsWidget at svn revision 15083 */
			if (changed) {
				//archive old version
				try {
					 ArchiveManager manager = ArchiveManager.getArchiveManager();
				        manager.archive(mp.getKnowledgeBase().getProject(), "Version before the update of the metaproject format from " + new Date());
				     Log.getLogger().info("Archived original metaproject to archive subfolder.");
				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING, "Failed at creating backup of metaproject", e);
				}
				
				//save metaproject
				ArrayList errors = new ArrayList();
				mp.save(errors);
				Log.getLogger().info("The metaproject was updated to a new version. Saved the updated metaproject on " + new Date());
				Log.handleErrors(Log.getLogger(), Level.WARNING, errors);
			}

		} catch (Throwable t) {
			Log.getLogger().log(Level.WARNING, "Failed to fix up metaproject to new version.", t);
		}
	}

	
	@SuppressWarnings("unchecked")
	private static boolean addPropertyValues(MetaProjectImpl mp) {
		boolean changed = false;
	    KnowledgeBase kb = mp.getKnowledgeBase();
	    
	    Cls propertyValueCls = createCls(kb, ClsEnum.PropertyValue.name(), kb.getRootClses());
	    changed = addTemplateSlot(kb, propertyValueCls, SlotEnum.propertyName) || changed;
	    changed = addTemplateSlot(kb, propertyValueCls, SlotEnum.propertyValue) || changed;
	    	    
	    Slot propertiesSlot = kb.getSlot(SlotEnum.properties.name());
	    if (propertiesSlot == null) {
	    	propertiesSlot = kb.createSlot(SlotEnum.properties.name());
	    	propertiesSlot.setValueType(ValueType.INSTANCE);
	    	propertiesSlot.setAllowsMultipleValues(true);
	    	propertiesSlot.setAllowedClses(CollectionUtilities.createCollection(propertyValueCls));
	    	changed = true;
	    }
	    
	    Cls userCls = mp.getCls(ClsEnum.User);
	    changed = addTemplateSlot(kb, userCls, SlotEnum.properties) || changed;

	    Cls policyCtrledObjCls = mp.getCls(ClsEnum.PolicyControlledObject);
	    changed = addTemplateSlot(kb, policyCtrledObjCls, SlotEnum.properties) || changed;

	    Cls groupOpCls = mp.getCls(ClsEnum.GroupOperation);
	    changed = addTemplateSlot(kb, groupOpCls, SlotEnum.properties) || changed;
	    
	    Cls opCls = mp.getCls(ClsEnum.Operation);
	    changed = addTemplateSlot(kb, opCls, SlotEnum.properties) || changed;
	    
	    return changed;
	}
	

	private static boolean addEmail(MetaProjectImpl mp) {
		boolean changed = false;
	    KnowledgeBase kb = mp.getKnowledgeBase();	    
	    Slot emailSlot = kb.getSlot(SlotEnum.email.name());
	    if (emailSlot == null) {
	    	emailSlot = kb.createSlot(SlotEnum.email.name());
	    	Cls userCls = mp.getCls(ClsEnum.User);
	    	userCls.addDirectTemplateSlot(emailSlot);
	    	changed = true;
	    }
		return changed;
	}

	private static boolean addSalt(MetaProjectImpl mp) {
		boolean changed = false;
	    KnowledgeBase kb = mp.getKnowledgeBase();
	    Slot saltSlot = kb.getSlot(SlotEnum.salt.name());
	    if (saltSlot == null) {
	    	saltSlot = kb.createSlot(SlotEnum.salt.name());
	    	Cls userCls = mp.getCls(ClsEnum.User);
	    	userCls.addDirectTemplateSlot(saltSlot);
	    		    	
	    	//change all existing passwords to digest + salt
	    	Slot passSlot = mp.getSlot(SlotEnum.password);
	    	for (User u : mp.getUsers()) {
	    		WrappedProtegeInstanceImpl ui = (WrappedProtegeInstanceImpl) u;
	    		String password = (String) ui.getProtegeInstance().getOwnSlotValue(passSlot);
	    		u.setPassword(password);
	    	}
	    	
	    	changed = true;
	    	Log.getLogger().info("\tEncoded all user passwords as part of the metaproject upgrade." +
	    			" Please adjust the User class forms to use the DigestedPassword slot widget for password field.");
	    }
	    
		return changed;
	}

	private static boolean addPolicyControlledObjectClass(MetaProjectImpl mp)  {
	    boolean changed = false;
	    KnowledgeBase kb = mp.getKnowledgeBase();

        Cls policyCtrledObjCls = kb.getCls(ClsEnum.PolicyControlledObject.name());
        if (policyCtrledObjCls == null) {
            Log.getLogger().info("Fixing up the metaproject to new version. No information will be lost.");
            policyCtrledObjCls = kb.createCls(ClsEnum.PolicyControlledObject.name(), kb.getRootClses());
            changed = true;
        }
        addTemplateSlot(policyCtrledObjCls, mp.getSlot(SlotEnum.name));
        addTemplateSlot(policyCtrledObjCls, mp.getSlot(SlotEnum.description));
        addTemplateSlot(policyCtrledObjCls, mp.getSlot(SlotEnum.allowedGroupOperation));
        Slot hostNameSlot = kb.getSlot(SlotEnum.hostName.name());
        if (hostNameSlot == null) {
            hostNameSlot = kb.createSlot(SlotEnum.hostName.name());
            changed = true;
        }
        Cls serverCls = kb.getCls(ClsEnum.Server.name());
        if (serverCls == null) {
            serverCls = kb.createCls(ClsEnum.Server.name(), CollectionUtilities.createCollection(policyCtrledObjCls));
            changed = true;
        }
        addTemplateSlot(serverCls, hostNameSlot);
        Cls projectCls = mp.getCls(ClsEnum.Project);
        if (!projectCls.hasSuperclass(policyCtrledObjCls)) {
            projectCls.addDirectSuperclass(policyCtrledObjCls);
            projectCls.removeDirectSuperclass(kb.getRootCls());
            changed = true;
        }
        return changed;
	}

	private static boolean addAccessSlots(MetaProjectImpl  mp) {
	    boolean changed = false;
	    KnowledgeBase  kb = mp.getKnowledgeBase();
	    Cls user = kb.getCls(MetaProjectImpl.ClsEnum.User.toString());
	    changed = addTemplateSlot(kb, user, MetaProjectImpl.SlotEnum.lastLogin) || changed;
	    changed = addTemplateSlot(kb, user, MetaProjectImpl.SlotEnum.lastAccess) || changed;
	    return changed;
	}

	private static boolean changeGroup(MetaProjectImpl mp) {
	    boolean changed = false;
	    KnowledgeBase  kb = mp.getKnowledgeBase();
	    Cls policyCtrledObjCls = kb.getCls(ClsEnum.PolicyControlledObject.name());
	    Cls groupCls = kb.getCls(ClsEnum.Group.name());
	    if (!groupCls.hasSuperclass(policyCtrledObjCls)) {
	        groupCls.addDirectSuperclass(policyCtrledObjCls);
	        groupCls.removeDirectSuperclass(kb.getRootCls());

	        Cls operationCls = kb.getCls(ClsEnum.Operation.name());
	        Collection<Cls> operationClsColl = CollectionUtilities.createCollection(operationCls);
	        createCls(kb, "GroupAppliedOperation", operationClsColl);
	        createCls(kb, "ProjectAppliedOperation", operationClsColl);
	        createCls(kb, "ServerAppliedOperation", operationClsColl);

	        changed = true;
	    }
	    return changed;
	}

	private static boolean addTemplateSlot(KnowledgeBase kb, Cls cls, MetaProjectImpl.SlotEnum slotEnum) {
	    String slotName = slotEnum.toString();
	    Slot slot;
	    boolean changed = false;
	    if ((slot = kb.getSlot(slotName)) == null) {
	        slot = kb.createSlot(slotName);
	        changed = true;
	    }
	    return addTemplateSlot(cls, slot) || changed;
	}

	private static boolean addTemplateSlot(Cls cls, Slot slot) {
		if (!cls.hasTemplateSlot(slot)) {
			cls.addDirectTemplateSlot(slot);
			return true;
		}
		return false;
	}

	private static Cls createCls(KnowledgeBase kb, String name, Collection<Cls> parents) {
	    Cls cls = kb.getCls(name);
	    if (cls == null) {
	        cls = kb.createCls(name, parents);
	    }
	    return cls;
	}

}
