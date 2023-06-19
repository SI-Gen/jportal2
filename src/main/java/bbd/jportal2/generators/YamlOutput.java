package bbd.jportal2.generators;

import bbd.jportal2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.PrintWriter;

public class YamlOutput extends BaseGenerator implements IBuiltInSIProcessor {

    private final Logger logger = LoggerFactory.getLogger(YamlOutput.class);

    public String description() {
        return "Generate a YAML representation of the database definition";
    }

    public String documentation() {
        return "Generate a YAML representation of the database definition.";
    }
    public YamlOutput() {
        super(YamlOutput.class);
    }

    /**
     * Generates a YAML file that contains the database definition
     */
    public void generate(Database database, String output) {
        String fileName;
        if (database.output.length() > 0)
            fileName = database.output;
        else
            fileName = database.name;

        fileName = output + fileName + ".yaml";

        logger.info("YAML: {}", fileName);

        try (PrintWriter outData = openOutputFileForGeneration("yaml", fileName)) {
            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);

            Yaml yaml = new Yaml(representer);
            yaml.dump(database, outData);
            outData.flush();
        } catch (IOException e1) {
            logger.error("Generate YamlOutput IO Error", e1);
        }
    }

}

