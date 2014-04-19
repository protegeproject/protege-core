package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultCls;
import edu.stanford.smi.protege.model.DefaultFacet;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.DefaultSimpleInstance;
import edu.stanford.smi.protege.model.DefaultSlot;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class DefaultFrameFactory implements FrameFactory {
    public static final int DEFAULT_CLS_JAVA_CLASS_ID = 6;
    public static final int DEFAULT_SLOT_JAVA_CLASS_ID = 7;
    public static final int DEFAULT_FACET_JAVA_CLASS_ID = 8;
    public static final int DEFAULT_SIMPLE_INSTANCE_JAVA_CLASS_ID = 5;

    private static final Class[] CONSTRUCTOR_PARAMETERS = { KnowledgeBase.class, FrameID.class };
    private KnowledgeBase _kb;
    private Collection _packages = new ArrayList();
    private Map _typesToImplementationClassMap = new HashMap();

    public DefaultFrameFactory(KnowledgeBase kb) {
        _kb = kb;
    }

    public KnowledgeBase getKnowledgeBase() {
        return _kb;
    }

    public void addJavaPackage(String packageName) {
        _packages.add(packageName);
    }

    public void removeJavaPackage(String packageName) {
        _packages.remove(packageName);
    }

    public Cls createCls(FrameID id, Collection directTypes) {
        Class implementationClass = getImplementationClass(directTypes, DefaultCls.class);
        return createCls(id, implementationClass);
    }

    protected Cls createCls(FrameID id, Class implementationClass) {
        Cls cls;
        if (implementationClass.equals(DefaultCls.class)) {
            cls = new DefaultCls(_kb, id);
        } else {
            cls = (Cls) createInstance(id, implementationClass);
        }
        configureCls(cls);
        return cls;
    }

    public Slot createSlot(FrameID id, Collection directTypes) {
        Class implementationClass = getImplementationClass(directTypes, DefaultSlot.class);
        return createSlot(id, implementationClass);
    }

    protected Slot createSlot(FrameID id, Class implementationClass) {
        Slot slot;
        if (implementationClass.equals(DefaultSlot.class)) {
            slot = new DefaultSlot(_kb, id);
        } else {
            slot = (Slot) createInstance(id, implementationClass);
        }
        configureSlot(slot);
        return slot;

    }

    public Facet createFacet(FrameID id, Collection directTypes) {
        Class implementationClass = getImplementationClass(directTypes, DefaultFacet.class);
        return createFacet(id, implementationClass);
    }

    protected Facet createFacet(FrameID id, Class implementationClass) {
        Facet facet;
        if (implementationClass.equals(DefaultFacet.class)) {
            facet = new DefaultFacet(_kb, id);
        } else {
            facet = (Facet) createInstance(id, implementationClass);
        }
        configureFacet(facet);
        return facet;
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection directTypes) {
        Class implementationClass = getImplementationClass(directTypes, DefaultSimpleInstance.class);
        return createSimpleInstance(id, implementationClass);
    }

    protected SimpleInstance createSimpleInstance(FrameID id, Class implementationClass) {
        SimpleInstance instance;
        if (implementationClass.equals(DefaultSimpleInstance.class)) {
            instance = new DefaultSimpleInstance(_kb, id);
        } else {
            instance = (SimpleInstance) createInstance(id, implementationClass);
        }
        configureSimpleInstance(instance);
        return instance;
    }

    protected void configureFacet(Facet facet) {
        if (facet.isSystem()) {
            Facet cachedSystemFacet = (Facet) getCachedSystemFrame(facet.getFrameID());
            if (cachedSystemFacet != null) {
                facet.setConstraint(cachedSystemFacet.getConstraint());
            }
        }
    }

    protected void configureSimpleInstance(SimpleInstance simpleInstance) {
        // do nothing (for now)
    }

    protected void configureCls(Cls cls) {
        // do nothing (for now)
    }

    protected void configureSlot(Slot slot) {
        // do nothing (for now)
    }

    private Frame getCachedSystemFrame(FrameID id) {
        return _kb.getSystemFrames().getFrame(id);
    }

    protected Class getImplementationClass(Collection directTypes, Class defaultClass) {
        Class implementationClass;
        if (_packages.isEmpty() || directTypes.size() != 1) {
            implementationClass = defaultClass;
        } else {
            Cls directType = (Cls) CollectionUtilities.getFirstItem(directTypes);
            implementationClass = (Class) _typesToImplementationClassMap.get(directType);
            if (implementationClass == null) {
                implementationClass = getJavaImplementationClass(directType, defaultClass);
            }
            _typesToImplementationClassMap.put(directType, implementationClass);
        }
        return implementationClass;
    }

    public boolean isCorrectJavaImplementationClass(FrameID id, Collection types, Class clas) {
        return getImplementationClass(types, clas).equals(clas);
    }

    private Instance createInstance(FrameID id, Class type) {
        Instance instance = null;
        try {
            Constructor constructor = type.getConstructor(CONSTRUCTOR_PARAMETERS);
            instance = (Instance) constructor.newInstance(new Object[] { _kb, id });
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return instance;
    }

    private Class getJavaImplementationClass(Cls type, Class baseClass) {
        Class implementationClass = null;
        Iterator i = _packages.iterator();
        while (i.hasNext() && implementationClass == null) {
            String packageName = (String) i.next();
            implementationClass = getJavaImplementationClass(packageName, type);
        }
        if (implementationClass == null) {
            implementationClass = baseClass;
        } else if (!isValidImplementationClass(implementationClass, baseClass)) {
            Log.getLogger().warning("Java implementation class of wrong type: " + implementationClass);
            implementationClass = baseClass;
        }
        return implementationClass;
    }

    private static boolean isValidImplementationClass(Class implementationClass, Class defaultClass) {
        return defaultClass.isAssignableFrom(implementationClass);
    }

    private Class getJavaImplementationClass(String packageName, Cls type) {
        String typeName = getJavaClassName(type);
        String className = packageName + "." + typeName;
        return SystemUtilities.forName(className, true);
    }

    protected String getJavaClassName(Cls type) {
        StringBuffer className = new StringBuffer();
        String typeName = type.getName();
        for (int i = 0; i < typeName.length(); ++i) {
            char c = typeName.charAt(i);
            if (isValidCharacter(c, className.length())) {
                className.append(c);
            }
        }
        return className.toString();
    }

    //ESCA-JAVA0130 
    protected boolean isValidCharacter(char c, int i) {
        return (i == 0) ? Character.isJavaIdentifierStart(c) : Character.isJavaIdentifierPart(c);
    }

    public int getJavaClassId(Frame frame) {
        int javaClassId;
        if (frame instanceof Cls) {
            javaClassId = DEFAULT_CLS_JAVA_CLASS_ID;
        } else if (frame instanceof Slot) {
            javaClassId = DEFAULT_SLOT_JAVA_CLASS_ID;
        } else if (frame instanceof Facet) {
            javaClassId = DEFAULT_FACET_JAVA_CLASS_ID;
        } else {
            javaClassId = DEFAULT_SIMPLE_INSTANCE_JAVA_CLASS_ID;
        }
        return javaClassId;
    }

    public Frame createFrameFromClassId(int javaClassId, FrameID id) {
        Frame frame;
        switch (javaClassId) {
            case DEFAULT_CLS_JAVA_CLASS_ID:
                frame = createCls(id, DefaultCls.class);
                break;
            case DEFAULT_SLOT_JAVA_CLASS_ID:
                frame = createSlot(id, DefaultSlot.class);
                break;
            case DEFAULT_FACET_JAVA_CLASS_ID:
                frame = createFacet(id, DefaultFacet.class);
                break;
            case DEFAULT_SIMPLE_INSTANCE_JAVA_CLASS_ID:
                frame = createSimpleInstance(id, DefaultSimpleInstance.class);
                break;
            default:
                throw new RuntimeException("Invalid java class id: " + javaClassId);
        }
        return frame;
    }

    private static Collection createRange(int value) {
        Collection c = new ArrayList();
        c.add(new Integer(value));
        return c;
    }

    public Collection getClsJavaClassIds() {
        return createRange(DEFAULT_CLS_JAVA_CLASS_ID);
    }

    public Collection getSlotJavaClassIds() {
        return createRange(DEFAULT_SLOT_JAVA_CLASS_ID);
    }

    public Collection getFacetJavaClassIds() {
        return createRange(DEFAULT_FACET_JAVA_CLASS_ID);
    }

    public Collection getSimpleInstanceJavaClassIds() {
        return createRange(DEFAULT_SIMPLE_INSTANCE_JAVA_CLASS_ID);
    }

    @SuppressWarnings("unchecked")
    public Frame rename(Frame original, String name) {
      if (original.getFrameID().getName().equals(name)) {
        return original;
      }
      int javaClassId = getJavaClassId(original);                                   // somewhat
      Frame newFrame = createFrameFromClassId(javaClassId, new FrameID(name));      //   wasteful...
      DefaultKnowledgeBase kb = (DefaultKnowledgeBase) original.getKnowledgeBase();
      kb.getHeadFrameStore().replaceFrame(original, newFrame);
      return newFrame;
    }

}
