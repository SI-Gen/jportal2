package bbd.jportal2;


import org.reflections.Reflections;

import java.util.*;

public class BuiltInGeneratorHelpers {
    public <TYPE_TO_TEST> Vector<String> findAllBuiltInGeneratorsOfType(Class<TYPE_TO_TEST> classType) {
        Set<Class<?>> ALL_BUILTIN_GENERATORS = findClasses(classType);

        Vector<String> foundGenerators = new Vector<>();
        for (Class generatorClass : ALL_BUILTIN_GENERATORS) {
            String generator = generatorClass.getSimpleName();
            foundGenerators.add(generator);
        }
        return foundGenerators;
    }

    private Set<Class<?>> findClasses(Class T) {
        Reflections reflections = new Reflections("bbd.jportal2");
        Set<Class<?>> foundClasses = reflections.getSubTypesOf(T);

        return foundClasses;
    }
}
