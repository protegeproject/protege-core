package edu.stanford.smi.protege.resource;

//ESCA*JAVA0130

import java.lang.reflect.*;

import junit.framework.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ResourceKey_Test extends TestCase {

    public void testStringExistence() throws IllegalAccessException {
        Class resourceKeyClass = ResourceKey.class;
        Field[] fields = resourceKeyClass.getFields();
        for (int i = 0; i < fields.length; ++i) {
            Field field = fields[i];
            checkField(field);
        }
    }

    private static void checkField(Field field) throws IllegalAccessException {
        ResourceKey key = (ResourceKey) field.get(null);
        assertTrue(key.toString(), LocalizedText.hasText(key));
    }
}
