<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
########################################################################################################################
################## Generated Code. DO NOT CHANGE THIS CODE. Change it in the generator and regenerate ##################
########################################################################################################################
<#list database.getTables() as table>
from .db_${table.getName()} import DB_${table.getName()}
<#list table.getProcs() as proc>
<#if !proc.isStdExtended() && !proc.isSProc() && proc.name != "">
    <#if proc.lines?size gt 0>
from .db_${table.getName()} import DB_${table.getName()}${proc.name}
    </#if>
</#if>
</#list>
</#list>

ALL_TABLES = [
<#list database.getTables() as table>
    DB_${table.getName()},
</#list>
]

ALL_PROCS = [
<#list database.getTables() as table>
    <#list table.getProcs() as proc>
        <#if !proc.isStdExtended() && !proc.isSProc() && proc.name != "">
            <#if proc.lines?size gt 0>
    DB_${table.getName()}${proc.name},
            </#if>
        </#if>
    </#list>
</#list>
]


__all__ = [
<#list database.getTables() as table>
    "DB_${table.getName()}",
</#list>

<#list database.getTables() as table>
    <#list table.getProcs() as proc>
        <#if !proc.isStdExtended() && !proc.isSProc() && proc.name != "">
            <#if proc.lines?size gt 0>
    "DB_${table.getName()}${proc.name}",
            </#if>
        </#if>
    </#list>
</#list>
]
