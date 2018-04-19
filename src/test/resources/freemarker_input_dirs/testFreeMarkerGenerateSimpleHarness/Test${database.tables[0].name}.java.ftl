<#import "/JPortalJavaHelpers.function.ftl" as JavaHelpers>
class Test${database.tables[0].name}
{
    //Fields
<#-- Create field values -->
<#list database.tables[0].fields as field>
    ${JavaHelpers.JPortalJavaFieldTypeLookup(field.type)}${"\t\t"}${field.name};
</#list>

<#-- Create field getter/setters -->
<#list database.tables[0].fields as field>
    //get/set ${field.name}
    void set${field.name}(${JavaHelpers.JPortalJavaFieldTypeLookup(field.type)} in) {
        ${field.name} = in;
    }
    ${JavaHelpers.JPortalJavaFieldTypeLookup(field.type)} get${field.name} {
        return ${field.name};
    }


</#list>

Test${database.tables[0].name}()
{}


}