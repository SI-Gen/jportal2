<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
<#import "basefunctions.ftl" as base>
// ########################################################################################################################
// ################## Generated Code. DO NOT CHANGE THIS CODE. Change it in the generator and regenerate ##################
// ########################################################################################################################
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Bbd.Vanguard.EntityFrameworkCore.${database.schema}.Models
{
    public class DB_${table.name}
    {
        <#list table.fields as field>
<#if field.isNull()>
#nullable enable
</#if>
        <#if field.isPrimaryKey()>
        [Key]
            <#if field.type?c == '10' || field.type?c == '14' || field.type?c == '24' || field.type?c == '25'>
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
            <#else>
        [DatabaseGenerated(DatabaseGeneratedOption.None)]
            </#if>
        </#if>
        <#if base.getEFColumnType(field) == 'string'>
        [StringLength(${field.length?c})]
        </#if>
        public <#compress>${base.getEFColumnType(field)}<#if field.isNull()>?</#if></#compress> ${field.name} { get; set; }
<#if field.isNull()>
#nullable disable
</#if>
        </#list>
        <#list table.links as link>
        [ForeignKey(nameof(${link.fields[0]}))]
        public <#compress>DB_${link.getName()} ${link.getName()}</#compress> { get; set; }
        </#list>
    }
    <#list table.procs as proc>
    <#if !proc.isStdExtended() && !proc.isSProc() && proc.name != "">
        <#if proc.lines?size gt 0 && proc.outputs?size gt 0>
    public class DB_${table.name}_${proc.name}
    {
        <#list proc.outputs as field>
<#if field.isNull()>
#nullable enable
</#if>
        public <#compress>${base.getEFColumnType(field)}<#if field.isNull()>?</#if></#compress> ${field.name} { get; set; }
<#if field.isNull()>
#nullable disable
</#if>
        </#list>
    }
        </#if>
    </#if>
    </#list>
}