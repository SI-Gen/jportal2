<#include "JPortalJavaFieldTypeLookups.function">
class Test${database.tables[0].name}
{
    //Fields
<#-- Create field values -->
<#list database.tables[0].fields as field>
    ${JPortalJavaFieldTypeLookup(field.type)}${"\t\t"}${field.name};
</#list>

<#-- Create field getter/setters -->
<#list database.tables[0].fields as field>
    //get/set ${field.name}
    void set${field.name}(${JPortalJavaFieldTypeLookup(field.type)} in) {
        ${field.name} = in;
    }
    ${JPortalJavaFieldTypeLookup(field.type)} get${field.name} {
        return ${field.name};
    }


</#list>

Test${database.tables[0].name}()
{}


}