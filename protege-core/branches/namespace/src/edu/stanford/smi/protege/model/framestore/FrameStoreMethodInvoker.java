package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

public class FrameStoreMethodInvoker {
    private static Map methodNameToInvokerMap = new HashMap();

    static {
        addInvokers();
        checkInvokers();
    }

    private static void checkInvokers() {
        Method[] methods = FrameStore.class.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            if (!methodNameToInvokerMap.containsKey(methods[i].getName())) {
                Log.getLogger().severe("Missing method invoker: " + methods[i].getName());
            }
        }
    }

    private static void addInvoker(String methodName, Invoker invoker) {
        Object previous = methodNameToInvokerMap.put(methodName, invoker);
        if (previous != null) {
            Log.getLogger().warning("replaced previous invoker: " + methodName);
        }
    }

    public static Object invoke(Method method, Object[] args, FrameStore frameStore) {
        Invoker invoker = (Invoker) methodNameToInvokerMap.get(method.getName());
        return invoker.invoke(frameStore, args);
    }

    interface Invoker {
        Object invoke(FrameStore delegate, Object[] args);
    }

    private static int getInteger(Object o) {
        return ((Integer) o).intValue();
    }

    private static boolean getBoolean(Object o) {
        return ((Boolean) o).booleanValue();
    }

    private static void addInvokers() {
        addInvoker("getFrameName", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getFrameName((Frame) args[0]);
            }
        });
        addInvoker("getName", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getName();
            }
        });
        addInvoker("getClsCount", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return new Integer(delegate.getClsCount());
            }
        });
        addInvoker("getSlotCount", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return new Integer(delegate.getSlotCount());
            }
        });
        addInvoker("getFacetCount", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return new Integer(delegate.getFacetCount());
            }
        });
        addInvoker("getSimpleInstanceCount", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return new Integer(delegate.getSimpleInstanceCount());
            }
        });
        addInvoker("getFrameCount", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return new Integer(delegate.getFrameCount());
            }
        });
        addInvoker("getClses", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getClses();
            }
        });
        addInvoker("getSlots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getSlots();
            }
        });
        addInvoker("getFacets", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getFacets();
            }
        });
        addInvoker("getFrames", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getFrames();
            }
        });
        addInvoker("getFrame", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                Object rval;
                if (args[0] instanceof String) {
                    rval = delegate.getFrame((String) args[0]);
                } else {
                    rval = delegate.getFrame((FrameID) args[0]);
                }
                return rval;
            }
        });
        addInvoker("createCls", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.createCls((FrameID) args[0], (Collection) args[1],
                        (Collection) args[2], getBoolean(args[3]));
            }
        });
        addInvoker("createSlot", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.createSlot((FrameID) args[0], (Collection) args[1],
                        (Collection) args[2], getBoolean(args[3]));
            }
        });
        addInvoker("createFacet", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.createFacet((FrameID) args[0], (Collection) args[1],
                        getBoolean(args[2]));
            }
        });
        addInvoker("createSimpleInstance", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.createSimpleInstance((FrameID) args[0], (Collection) args[1], getBoolean(args[2]));
            }
        });

        addInvoker("deleteCls", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.deleteCls((Cls) args[0]);
                return null;
            }
        });
        addInvoker("deleteSlot", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.deleteSlot((Slot) args[0]);
                return null;
            }
        });
        addInvoker("deleteFacet", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.deleteFacet((Facet) args[0]);
                return null;
            }
        });
        addInvoker("deleteSimpleInstance", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.deleteSimpleInstance((SimpleInstance) args[0]);
                return null;
            }
        });
        addInvoker("getOwnSlots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getOwnSlots((Frame) args[0]);
            }
        });
        addInvoker("getOwnSlotValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getOwnSlotValues((Frame) args[0], (Slot) args[1]);
            }
        });
        addInvoker("getDirectOwnSlotValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectOwnSlotValues((Frame) args[0], (Slot) args[1]);
            }
        });
        addInvoker("getDirectOwnSlotValuesCount", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return new Integer(delegate.getDirectOwnSlotValuesCount((Frame) args[0], (Slot) args[1]));
            }
        });
        addInvoker("moveDirectOwnSlotValue", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.moveDirectOwnSlotValue((Frame) args[0], (Slot) args[1], getInteger(args[2]),
                        getInteger(args[3]));
                return null;
            }
        });
        addInvoker("setDirectOwnSlotValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.setDirectOwnSlotValues((Frame) args[0], (Slot) args[1], (Collection) args[2]);
                return null;
            }
        });

        addInvoker("getOwnFacets", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getOwnFacets((Frame) args[0], (Slot) args[1]);
            }
        });
        addInvoker("getOwnFacetValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getOwnFacetValues((Frame) args[0], (Slot) args[1], (Facet) args[2]);
            }
        });
        addInvoker("getTemplateSlots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getTemplateSlots((Cls) args[0]);
            }
        });
        addInvoker("getDirectTemplateSlots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectTemplateSlots((Cls) args[0]);
            }
        });
        addInvoker("getDirectDomain", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectDomain((Slot) args[0]);
            }
        });
        addInvoker("getDomain", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDomain((Slot) args[0]);
            }
        });
        addInvoker("getOverriddenTemplateSlots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getOverriddenTemplateSlots((Cls) args[0]);
            }
        });
        addInvoker("getDirectlyOverriddenTemplateSlots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectlyOverriddenTemplateSlots((Cls) args[0]);
            }
        });
        addInvoker("addDirectTemplateSlot", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.addDirectTemplateSlot((Cls) args[0], (Slot) args[1]);
                return null;
            }
        });
        addInvoker("removeDirectTemplateSlot", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.removeDirectTemplateSlot((Cls) args[0], (Slot) args[1]);
                return null;
            }
        });
        addInvoker("moveDirectTemplateSlot", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.moveDirectTemplateSlot((Cls) args[0], (Slot) args[1], getInteger(args[2]));
                return null;
            }
        });

        addInvoker("getTemplateSlotValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getTemplateSlotValues((Cls) args[0], (Slot) args[1]);
            }
        });
        addInvoker("getDirectTemplateSlotValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectTemplateSlotValues((Cls) args[0], (Slot) args[1]);
            }
        });
        addInvoker("setDirectTemplateSlotValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.setDirectTemplateSlotValues((Cls) args[0], (Slot) args[1], (Collection) args[2]);
                return null;
            }
        });
        addInvoker("getTemplateFacets", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getTemplateFacets((Cls) args[0], (Slot) args[1]);
            }
        });
        addInvoker("getOverriddenTemplateFacets", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getOverriddenTemplateFacets((Cls) args[0], (Slot) args[1]);
            }
        });
        addInvoker("getDirectlyOverriddenTemplateFacets", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectlyOverriddenTemplateFacets((Cls) args[0], (Slot) args[1]);
            }
        });
        addInvoker("removeDirectTemplateFacetOverrides", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.removeDirectTemplateFacetOverrides((Cls) args[0], (Slot) args[1]);
                return null;
            }
        });
        addInvoker("getTemplateFacetValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getTemplateFacetValues((Cls) args[0], (Slot) args[1], (Facet) args[2]);
            }
        });
        addInvoker("getDirectTemplateFacetValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectTemplateFacetValues((Cls) args[0], (Slot) args[1], (Facet) args[2]);
            }
        });
        addInvoker("setDirectTemplateFacetValues", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.setDirectTemplateFacetValues((Cls) args[0], (Slot) args[1], (Facet) args[2],
                        (Collection) args[3]);
                return null;
            }
        });
        addInvoker("getDirectSuperclasses", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectSuperclasses((Cls) args[0]);
            }
        });
        addInvoker("getSuperclasses", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getSuperclasses((Cls) args[0]);
            }
        });
        addInvoker("getDirectSuperclasses", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectSuperclasses((Cls) args[0]);
            }
        });
        addInvoker("getDirectSubclasses", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectSubclasses((Cls) args[0]);
            }
        });
        addInvoker("getSubclasses", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getSubclasses((Cls) args[0]);
            }
        });
        addInvoker("getDirectSuperclasses", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectSuperclasses((Cls) args[0]);
            }
        });
        addInvoker("addDirectSuperclass", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.addDirectSuperclass((Cls) args[0], (Cls) args[1]);
                return null;
            }
        });
        addInvoker("removeDirectSuperclass", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.removeDirectSuperclass((Cls) args[0], (Cls) args[1]);
                return null;
            }
        });
        addInvoker("moveDirectSubclass", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.moveDirectSubclass((Cls) args[0], (Cls) args[1], getInteger(args[2]));
                return null;
            }
        });

        addInvoker("getDirectSuperslots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectSuperslots((Slot) args[0]);
            }
        });
        addInvoker("getSuperslots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getSuperslots((Slot) args[0]);
            }
        });
        addInvoker("getDirectSubslots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getDirectSubslots((Slot) args[0]);
            }
        });
        addInvoker("getSubslots", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getSubslots((Slot) args[0]);
            }
        });

        addInvoker("addDirectSuperslot", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.addDirectSuperslot((Slot) args[0], (Slot) args[1]);
                return null;
            }
        });
        addInvoker("removeDirectSuperslot", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.removeDirectSuperslot((Slot) args[0], (Slot) args[1]);
                return null;
            }
        });
        addInvoker("moveDirectSubslot", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                delegate.moveDirectSubslot((Slot) args[0], (Slot) args[1], getInteger(args[2]));
                return null;
            }
        });

        addInvoker("getFrameName", new Invoker() {
            public Object invoke(FrameStore delegate, Object[] args) {
                return delegate.getFrameName((Frame) args[0]);
            }
        });
    }
}