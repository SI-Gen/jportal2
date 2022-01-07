<#function getEFColumnType field>
    <#switch field.type?c>
        <#case '1'> <#-- BLOB -->
        <#case '19'> <#-- TLOB -->
            <#return "byte[]">
            <#break>
        <#case '2'> <#-- BOOLEAN -->
            <#return "bool">
            <#break>
        <#case '3'> <#-- BYTE -->
            <#return "byte">
            <#break>
        <#case '5'> <#-- DATE -->
        <#case '6'> <#-- DATETIME -->
        <#case '17'> <#-- TIME -->
        <#case '18'> <#-- TIMESTAMP -->
        <#case '20'> <#-- USERSTAMP -->
        <#case '26'> <#-- AUTOTIMESTAMP -->
            <#return "DateTime">
            <#break>
        <#case '7'> <#-- DOUBLE -->
        <#case '13'> <#-- MONEY -->
            <#return "double">
            <#break>
        <#case '9'> <#-- FLOAT -->
            <#return "float">
            <#break>
        <#case '15'> <#-- SHORT -->
            <#return "short">
            <#break>
        <#case '10'> <#-- IDENTITY -->
        <#case '11'> <#-- INT -->
        <#case '14'> <#-- SEQUENCE -->
            <#return "int">
            <#break>
        <#case '12'> <#-- LONG -->
        <#case '24'> <#-- BIGSEQUENCE -->
        <#case '25'> <#-- BIGIDENTITY -->
            <#return "long">
            <#break>
        <#case '4'> <#-- CHAR -->
        <#case '21'> <#-- ANSICHAR -->
        <#case '23'> <#-- XML -->
        <#case '27'> <#-- WCHAR -->
        <#case '28'> <#-- WANSICHAR -->
        <#case '29'> <#-- UTF8 -->
        <#case '30'> <#-- BIGXML -->
            <#return "string">
            <#break>
        <#default><#return "string">
    </#switch>
</#function>
<#function getFields fields>
    <#local currentI = 0>
    <#local retVal = "">
    <#list fields as field>
        <#if (currentI > 0)><#local retVal = retVal + ", "></#if>
        <#local retVal = retVal + field.name>
        <#local currentI = currentI + 1>
    </#list>
    <#return retVal>
</#function>
<#function getTypedFields fields>
    <#local currentI = 0>
    <#local retVal = "">
    <#list fields as field>
        <#if (currentI > 0)><#local retVal = retVal + ", "></#if>
        <#local retVal = retVal + getEFColumnType(field) + " " + field.name>
        <#local currentI = currentI + 1>
    </#list>
    <#return retVal>
</#function>
<#function getPrimaryKeyFieldsString fields>
    <#local currentI = 0>
    <#local retVal = "">
    <#list fields as field>
        <#if field.isPrimaryKey()>
            <#if (currentI > 0)><#local retVal = retVal + ", "></#if>
            <#local retVal = retVal + "\"" + field.name + "\"">
            <#local currentI = currentI + 1>
        </#if>
    </#list>
    <#return retVal>
</#function>
<#function formatEFLines lines>
    <#assign retList = [ ] />
    <#list lines as line>
        <#local addLine = line>
        <#local addLine = addLine?replace("\\", "\"")>
        <#list 1..999 as i>
            <#local current_index = addLine?index_of('?')>
            <#if (current_index > -1)>
                <#local addLine = addLine[0..current_index - 1] + "{${line.getPlaceHolderInputPos()}}" + addLine[current_index + 1..]>
            <#else>
                <#break>
            </#if>
        </#list>
        <#assign retList = retList + [ addLine ] />
    </#list>
    <#return retList>
</#function>