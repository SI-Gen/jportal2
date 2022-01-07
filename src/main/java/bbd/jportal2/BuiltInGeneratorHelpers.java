package bbd.jportal2;

import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import java.util.*;

public class BuiltInGeneratorHelpers {
    public <TYPE_TO_TEST> Vector<String> findAllBuiltInGeneratorsOfType(Class<TYPE_TO_TEST> classType) {
        Set<Class<?>> ALL_BUILTIN_GENERATORS = findClasses(classType);

        Vector<String> foundGenerators = new Vector<>();
        for (Class<?> generatorClass : ALL_BUILTIN_GENERATORS) {
            String generator = generatorClass.getSimpleName();
            foundGenerators.add(generator);
        }
        return foundGenerators;
    }

    private Set<Class<?>> findClasses(Class T) {
        final Set<Class<?>> foundClasses = new HashSet<>();
        ComponentScanner scanner = new ComponentScanner();

        scanner.getClasses(new ComponentQuery() {

            protected void query() {
                select().from("bbd.jportal2").andStore(
                        thoseImplementing(T).into(foundClasses));
                //thoseAnnotatedWith(SampleAnnotation.class).into(samples));
            }

        });
        return foundClasses;
    }
}
