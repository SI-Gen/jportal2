You can specify a properties file using the `--properties-file` command-line parameter, to configure how
JPortal2 must generate the output SQL syntax. The properties file supports the following entries:

```properties
TemplateOutputOptions.DatabaseNameSuffix=[
TemplateOutputOptions.DatabaseNamePrefix=]
TemplateOutputOptions.SchemaNamePrefix=[
TemplateOutputOptions.SchemaNameSuffix=]

TemplateOutputOptions.TableNamePrefix=[
TemplateOutputOptions.TableNameSuffix=]
TemplateOutputOptions.FieldNamePrefix=[
TemplateOutputOptions.FieldNameSuffix=]

TemplateOutputOptions.FieldVariablePrefix=:
TemplateOutputOptions.FieldVariableSuffix=

TemplateOutputOptions.EngineSugarPrefix=
TemplateOutputOptions.EngineSugarSuffix=
TemplateOutputOptions.EngineSugarSequence=default,
TemplateOutputOptions.EngineSugarOutput=OUTPUT
TemplateOutputOptions.EngineSugarTail=RETURNING

TemplateOutputOptions.DynamicVariablePrefix={
TemplateOutputOptions.DynamicVariableSuffix=}
```
