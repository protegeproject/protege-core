package edu.stanford.smi.protege.server.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;
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

			/* attempt to use getDesignTimeClsWidget at svn revision 15083 */
			if (changed) {
				ArrayList errors = new ArrayList();
				mp.save(errors);
				for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					Log.getLogger().warning("Error at save: " + object);
				}
			}

		} catch (Throwable t) {
			Log.getLogger().log(Level.WARNING, "Failed to fix up metaproject to new version.", t);
		}


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
	    boolean  changed = false;
	    KnowledgeBase  kb = mp.getKnowledgeBase();
	    Cls user = kb.getCls(MetaProjectImpl.ClsEnum.User.toString());
	    changed = addTemplateSlot(kb, user, MetaProjectImpl.SlotEnum.lastLogin) || changed;
	    changed = addTemplateSlot(kb, user, MetaProjectImpl.SlotEnum.lastAccess) || changed;
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

}
