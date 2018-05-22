<#import "/helpers/JPortalJavaHelpers.function.ftl" as JavaHelpers>

${database.name}
<#list database.tables as table>
    ${table.name}
    <#list table.fields as field>
        ${field.name} is of type ${JavaHelpers.JPortalJavaFieldTypeLookup(field.type)}
    </#list>
    <#list table.procs as proc>
        ${proc.name}
        <#list proc.inputs as input>
            ${input.name} is of type ${JavaHelpers.JPortalJavaFieldTypeLookup(input.type)}
        </#list>
    </#list>

</#list>
