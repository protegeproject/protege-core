package edu.stanford.smi.protege.code.generator.wrapping;

import java.io.File;
import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;

public interface JavaCodeGeneratorOptions {

    //boolean getAbstractMode();

    String getFactoryClassName();

    File getOutputFolder();

    String getPackage();

    boolean getSetMode();

    Collection<Cls> getClses();
}
