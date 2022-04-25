<#import "/helpers/JPortalJavaHelpers.function.ftl" as JavaHelpers>

${database.name}
<#list database.tables as table>
    ${table.name}
    <#list table.fields as field>
        ${field.name} is of type ${JavaHelpers.JPortalJavaFieldTypeLookup(field.type)}
    </#list>

    <#list table.procs as proc>
        Name: ${proc.name}
        IsBuiltin: ${proc.isBuiltIn()?string("yes", "no")}
        <#list proc.inputs as input>
            Name: ${input.name}
            Type: ${input.type}
        </#list>
        <#list proc.outputs as output>
            Name: ${output.name}
            Type: ${output.type}
        </#list>

    </#list>

</#list>
