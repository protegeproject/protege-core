package edu.stanford.smi.protege.code.generator.wrapping;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;


public class OntologyJavaMappingUtil {

	private static final Class<?>[] CONSTRUCTOR_PARAMETERS = {Instance.class};

    private static Map<String, Entry> ontologyClassNameMap  = new HashMap<String, Entry>();
    private static Map<Class<?>, Entry> interfaceMap = new HashMap<Class<?>, Entry>();
    private static Map<Class<?>, Entry> implementationMap = new HashMap<Class<?>, Entry>();

    public static void add(String protegeClassName,
                           Class<?> javaInterface,
                           Class<?> javaImplementation) {
        Entry entry = new Entry(protegeClassName, javaInterface, javaImplementation);
        ontologyClassNameMap.put(protegeClassName, entry);
        interfaceMap.put(javaInterface, entry);
        implementationMap.put(javaImplementation, entry);
    }

	/**
	 * Creates object as the java interface passed as argument. Argument name is the name of the
	 * created wrapped instance. If an instance with the name "name" already exists, it will return null.
	 */
	public static <X> X createObjectAs(KnowledgeBase kb, String name, Class<? extends X> javaInterface) {
		if (name != null) {
			if (kb.getInstance(name) != null) { //instance with this name already exists
				return null;
			}
		}
		Entry entry = interfaceMap.get(javaInterface);
		if (entry == null) {
			return null;
		}
		String clsName = entry.getOntologyClassName();
		Cls	cls = kb.getCls(clsName);
		Instance inst = cls.createDirectInstance(name);
		return createJavaObject(getJavaImplementation(entry.getJavaImplementation(), javaInterface), inst);
	}


	public static <X> X createObject(KnowledgeBase kb, String name, String protegeClsName, Class<? extends X> javaReturnInterface) {
    	if (name != null && kb.getFrame(name) != null) {
    		return null;
    	}
		Cls cls = kb.getCls(protegeClsName);
		if (cls == null) {
			return null;
		}
		String returnClsName = javaReturnInterface.getSimpleName();
		Cls returnCls = kb.getCls(returnClsName);
		if (returnClsName == null) {
			return null;
		}
		if (!cls.equals(returnClsName) && !cls.hasSuperclass(returnCls)) {
			return null;
		}
		Entry entry = ontologyClassNameMap.get(protegeClsName);
		if (entry != null) { //hopefully most cases
			return createJavaObject(getJavaImplementation(entry.getJavaImplementation(), javaReturnInterface), cls.createDirectInstance(name));
		} else { //corresponding java class not found
			for (Iterator iterator = cls.getSuperclasses().iterator(); iterator.hasNext();) {
				Cls supercls = (Cls) iterator.next();
				Entry e = ontologyClassNameMap.get(supercls.getName());
				if (e != null) {
					Instance wrappedInst = cls.createDirectInstance(name);
					return createJavaObject(getJavaImplementation(e.getJavaImplementation(), javaReturnInterface), wrappedInst);
				}
			}
		}
		return null;
	}


	private static <X> X createJavaObject(Class<? extends X> javaImplementationClass, Instance instance) {
		if (javaImplementationClass == null || instance == null) {
			return null;
		}

		X obj = null;
		try {
			Constructor<? extends X> constructor = javaImplementationClass.getConstructor(CONSTRUCTOR_PARAMETERS);
			obj = constructor.newInstance(new Object[] {instance});
		} catch (Exception e) {
			Log.getLogger().log(Level.SEVERE, "Creating Java Object failed. (Java Impl Class: " +
					javaImplementationClass + ", Wrapped Protege instance: " +
					instance + ")", e);
		}
		return obj;
	}


	public static <X> X getJavaObjectAs(KnowledgeBase kb, String name, Class<? extends X> javaInterface) {
		Instance instance = kb.getInstance(name);
		if (instance == null) {
			return null;
		}
		Entry e = interfaceMap.get(javaInterface);
		if (e == null) { return null; }
		return createJavaObject (getJavaImplementation(e.getJavaImplementation(), javaInterface), instance);
	}

	public static <X> X getSpecificObject(KnowledgeBase kb, Instance wrappedInst, Class<? extends X> javaReturnInterface) {
		if (wrappedInst == null) {
			return null;
		}
		Class<?> implClass = getJavaImplementation(wrappedInst.getDirectType());
		if (implClass == null) { return null; }
		return createJavaObject(getJavaImplementation(implClass, javaReturnInterface), wrappedInst);
	}


	public static boolean canAs(Object impl, Class<?> javaInterface) {
		if (javaInterface.isAssignableFrom(impl.getClass())) {
            return true;
        }
		if (!(impl instanceof AbstractWrappedInstance)) {
			return false; //for now
		}
		Instance inst = ((AbstractWrappedInstance) impl).getWrappedProtegeInstance();
		Class<?> implClass = getJavaImplementation(inst.getDirectType());
		if (implClass == null) { return false; }
		return javaInterface.isAssignableFrom(implClass);
	}


	public static <X> X as(Object impl, Class<? extends X> javaInterface) {
	        if (javaInterface.isAssignableFrom(impl.getClass())) {
	            return javaInterface.cast(impl);
	        }
	        if (!(impl instanceof AbstractWrappedInstance)) {
				return null; //for now
			}
			Instance inst = ((AbstractWrappedInstance) impl).getWrappedProtegeInstance();
			return getSpecificObject(inst.getKnowledgeBase(), inst, javaInterface);
	    }


	private static Class<?> getJavaImplementation(Cls cls) {
		if (cls == null) {
			return null;
		}
		Entry entry = ontologyClassNameMap.get(cls.getName());
		if (entry != null) { //hopefully most cases
			return entry.getJavaImplementation();
		} else { //corresponding java class not found
			for (Iterator iterator = cls.getSuperclasses().iterator(); iterator.hasNext();) {
				Cls supercls = (Cls) iterator.next();
				Entry e = ontologyClassNameMap.get(supercls.getName());
				if (e != null) {
					return e.getJavaImplementation();
				}
			}
		}
		return null;
	}

	private static <X> Class<X> getJavaImplementation(Class<?> implClass, Class<? extends X> javaInterface) {
		return (Class<X>) (javaInterface.isAssignableFrom(implClass) ?  implClass : null);
	}


	public static void dispose() {
		implementationMap.clear();
		interfaceMap.clear();
		ontologyClassNameMap.clear();
	}


    private static class Entry{
        private String ontologyClassName;
        private Class<?> javaInterface;
        private Class<?> javaImplementation;

        public Entry(String protegeClass,
                     Class<?> javaInterface,
                     Class<?> javaImplementation) {
            this.ontologyClassName = protegeClass;
            this.javaInterface = javaInterface;
            this.javaImplementation = javaImplementation;
        }

        public String getOntologyClassName() {
            return ontologyClassName;
        }

        public Class<?> getJavaInterface() {
            return javaInterface;
        }

        public Class<?> getJavaImplementation() {
            return javaImplementation;
        }
    }

}
