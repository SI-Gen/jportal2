package bbd.jportal2;

import java.util.Vector;

public class BuiltInGeneratorHelpers {
    public <TYPE_TO_TEST> Vector<String> findAllBuiltInGeneratorsOfType(Class<TYPE_TO_TEST> classType) {
        return new Vector<>(BuiltInGeneratorRegistry.namesAssignableTo(classType));
    }
}
