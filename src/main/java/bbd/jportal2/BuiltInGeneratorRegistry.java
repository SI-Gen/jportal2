package bbd.jportal2;

import bbd.jportal2.generators.CSNetCode;
import bbd.jportal2.generators.CliCCode;
import bbd.jportal2.generators.CliCCode2;
import bbd.jportal2.generators.DB2CCode;
import bbd.jportal2.generators.Db2DDL;
import bbd.jportal2.generators.IdlCode;
import bbd.jportal2.generators.JavaJCCode;
import bbd.jportal2.generators.MSSqlCCode;
import bbd.jportal2.generators.MSSqlDDL;
import bbd.jportal2.generators.MySqlCCode;
import bbd.jportal2.generators.OciCCode;
import bbd.jportal2.generators.OracleDDL;
import bbd.jportal2.generators.PostgresCCode;
import bbd.jportal2.generators.PostgresDDL;
import bbd.jportal2.generators.PythonCliCode;
import bbd.jportal2.generators.PythonTreeCode;
import bbd.jportal2.generators.YamlOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Compile-time list of built-in generators.
 *
 * Native images have no classpath to scan, so generators are registered here
 * instead of being discovered with Reflections at startup. Add a new built-in
 * generator to this list when introducing one.
 */
public final class BuiltInGeneratorRegistry {
    private static final Map<String, Registration> REGISTRATIONS = new LinkedHashMap<>();

    static {
        register(CSNetCode.class, CSNetCode::new);
        register(CliCCode.class, CliCCode::new);
        register(CliCCode2.class, CliCCode2::new);
        register(DB2CCode.class, DB2CCode::new);
        register(Db2DDL.class, Db2DDL::new);
        register(IdlCode.class, IdlCode::new);
        register(JavaJCCode.class, JavaJCCode::new);
        register(MSSqlCCode.class, MSSqlCCode::new);
        register(MSSqlDDL.class, MSSqlDDL::new);
        register(MySqlCCode.class, MySqlCCode::new);
        register(OciCCode.class, OciCCode::new);
        register(OracleDDL.class, OracleDDL::new);
        register(PostgresCCode.class, PostgresCCode::new);
        register(PostgresDDL.class, PostgresDDL::new);
        register(PythonCliCode.class, PythonCliCode::new);
        register(PythonTreeCode.class, PythonTreeCode::new);
        register(YamlOutput.class, YamlOutput::new);
    }

    private BuiltInGeneratorRegistry() {
    }

    private static void register(Class<? extends IBuiltInGenerator> type, Supplier<IBuiltInGenerator> factory) {
        REGISTRATIONS.put(type.getSimpleName(), new Registration(type, factory));
    }

    public static IBuiltInGenerator create(String simpleName) {
        Registration registration = REGISTRATIONS.get(simpleName);
        if (registration == null) {
            return null;
        }
        return registration.factory.get();
    }

    public static List<String> namesAssignableTo(Class<?> type) {
        List<String> names = new ArrayList<>();
        for (Registration registration : REGISTRATIONS.values()) {
            if (type.isAssignableFrom(registration.type)) {
                names.add(registration.type.getSimpleName());
            }
        }
        Collections.sort(names);
        return names;
    }

    private static final class Registration {
        private final Class<? extends IBuiltInGenerator> type;
        private final Supplier<IBuiltInGenerator> factory;

        private Registration(Class<? extends IBuiltInGenerator> type, Supplier<IBuiltInGenerator> factory) {
            this.type = type;
            this.factory = factory;
        }
    }
}
