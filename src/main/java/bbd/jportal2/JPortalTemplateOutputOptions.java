package bbd.jportal2;

public class JPortalTemplateOutputOptions {
    public String DatabaseNamePrefix="";
    public String DatabaseNameSuffix="";

    public String SchemaNamePrefix="";
    public String SchemaNameSuffix="";

    public String TableNamePrefix="";
    public String TableNameSuffix="";
    public String FieldNamePrefix="";
    public String FieldNameSuffix="";

    public String FieldVariablePrefix="";
    public String FieldVariableSuffix="";

    public String EngineSugarPrefix = "";
    public String EngineSugarSuffix = "";
    public String EngineSugarOutput = "";
    public String EngineSugarSequence = "";
    public String EngineSugarTail = "";

    public String DynamicVariablePrefix = "";
    public String DynamicVariableSuffix = "";


    private JPortalTemplateOutputOptions() {}

    public static JPortalTemplateOutputOptions newInstance() {
        return new JPortalTemplateOutputOptions();
    }

    public static JPortalTemplateOutputOptions defaultBuiltInOptions() {
        JPortalTemplateOutputOptions options = new JPortalTemplateOutputOptions();
        options.DatabaseNameSuffix="";
        options.DatabaseNamePrefix="";

        options.SchemaNamePrefix="";
        options.SchemaNameSuffix="";

        options.TableNamePrefix="";
        options.TableNameSuffix="";
        options.FieldNamePrefix="";
        options.FieldNameSuffix="";

        options.FieldVariablePrefix=":";
        options.FieldVariableSuffix="";

        options.EngineSugarPrefix = "_ret.";
        options.EngineSugarSuffix = "";
        options.EngineSugarOutput = "Output";
        options.EngineSugarSequence = "Sequence";
        options.EngineSugarTail = "Tail";


        options.DynamicVariablePrefix = "&";
        options.DynamicVariableSuffix = "";

        return options;
    }

    public static JPortalTemplateOutputOptions defaultTemplateOptions() {
        JPortalTemplateOutputOptions options = new JPortalTemplateOutputOptions();
        options.DatabaseNameSuffix="";
        options.DatabaseNamePrefix="";

        options.SchemaNamePrefix="";
        options.SchemaNameSuffix="";

        options.TableNamePrefix="";
        options.TableNameSuffix="";
        options.FieldNamePrefix="";
        options.FieldNameSuffix="";

        options.FieldVariablePrefix=":";
        options.FieldVariableSuffix="";

        options.EngineSugarPrefix = "{_ret.";
        options.EngineSugarSuffix = "}";
        options.EngineSugarOutput = "Output";
        options.EngineSugarSequence = "Sequence";
        options.EngineSugarTail = "Tail";

        options.DynamicVariablePrefix = "{";
        options.DynamicVariableSuffix = "}";

        return options;
    }
}