package edu.stanford.smi.protege.code.generator.wrapping;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.ApplicationProperties;

public class ProjectBasedJavaCodeGeneratorOptions implements EditableJavaCodeGeneratorOptions {

    public final static String ABSTRACT_MODE = "JavaCodeAbstract";

    public final static String FACTORY_CLASS_NAME = "JavaCodeFactoryClassName";

    public final static String FILE_NAME = "JavaCodeFileName";

    public final static String PACKAGE = "JavaCodePackage";

    public final static String SET_MODE = "JavaCodeSet";

    public final static String PREFIX_MODE = "JavaCodeUsePrefix";

    private Collection<Cls> clses = new ArrayList<Cls>();

    public boolean getAbstractMode() {
    	return ApplicationProperties.getBooleanProperty(ABSTRACT_MODE, false);
    }


    public String getFactoryClassName() {
    	return ApplicationProperties.getString(FACTORY_CLASS_NAME, "MyFactory");
    }


    public File getOutputFolder() {
    	String fileName= ApplicationProperties.getString(FILE_NAME, "");
        return new File(fileName);
    }


    public String getPackage() {
    	return ApplicationProperties.getString(PACKAGE);
    }


    public boolean getSetMode() {
    	return ApplicationProperties.getBooleanProperty(SET_MODE, false);
    }

    public void setAbstractMode(boolean value) {
    	ApplicationProperties.setBoolean(ABSTRACT_MODE, value);
    }


    public void setOutputFolder(File file) {
    	ApplicationProperties.setString(FILE_NAME, file == null ? null : file.getAbsolutePath());
    }


    public void setFactoryClassName(String value) {
    	ApplicationProperties.setString(FACTORY_CLASS_NAME, value);
    }


    public void setPackage(String value) {
    	ApplicationProperties.setString(PACKAGE, value);
    }


    public void setSetMode(boolean value) {
    	ApplicationProperties.setBoolean(SET_MODE, value);
    }


	public void setClses(Collection<Cls> clses) {
		this.clses = clses;
	}


	public Collection<Cls> getClses() {
		return clses;
	}

}
