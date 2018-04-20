<#import "/JPortalJavaHelpers.function.ftl" as JavaHelpers>

${database.name}
<#list database.tables as table>
    ${table.name}
    <#list table.fields as field>
        ${field.name} is of type ${JavaHelpers.JPortalJavaFieldTypeLookup(field.type)}
    </#list>
</#list>
