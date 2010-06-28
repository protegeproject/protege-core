package edu.stanford.smi.protege.code.generator.wrapping;

import java.io.File;
import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;

public interface EditableJavaCodeGeneratorOptions extends JavaCodeGeneratorOptions {

    void setAbstractMode(boolean value);

    void setFactoryClassName(String value);

    void setOutputFolder(File file);

    void setPackage(String value);

    void setSetMode(boolean value);

    void setClses(Collection<Cls> clses);

}
