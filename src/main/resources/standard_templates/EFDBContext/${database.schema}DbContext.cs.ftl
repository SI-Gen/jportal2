<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
<#import "basefunctions.ftl" as base>
// ########################################################################################################################
// ################## Generated Code. DO NOT CHANGE THIS CODE. Change it in the generator and regenerate ##################
// ########################################################################################################################
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using Bbd.Vanguard.EntityFrameworkCore.${database.schema}.Models;

namespace Bbd.Vanguard.EntityFrameworkCore.${database.schema}
{
    public abstract class ${database.schema}DbContext : DbContext, I${database.schema}DbContext
    {
        public ${database.schema}DbContext([NotNull] DbContextOptions options) : base(options) { }

        <#list database.getTables() as table>
        public DbSet<DB_${table.name}> DB_${table.name} { get; set; }
        <#list table.procs as proc>
        <#if !proc.isStdExtended() && !proc.isSProc() && proc.name != "" && proc.name != "Identity" && proc.lines?size gt 0 && proc.outputs?size gt 0>
        protected DbSet<DB_${table.name}_${proc.name}> ${table.name}_${proc.name}Set { get; set; }
        </#if>
        </#list>
        </#list>
        protected virtual string GetTableName(string name) => name;
        protected virtual string GetSchemaName(string name) => name;
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            <#list database.getTables() as table>
            modelBuilder.Entity<DB_${table.name}>().ToTable(GetTableName("${table.name}"), GetSchemaName("${database.getSchema()}"))<#if !table.hasPrimaryKey>.HasNoKey()<#elseif table.hasPrimaryKey && !table.hasIdentity>.HasKey(${base.getPrimaryKeyFieldsString(table.fields)})</#if>;
            <#list table.procs as proc>
            <#if !proc.isStdExtended() && !proc.isSProc() && proc.name != "" && proc.name != "Identity" && proc.lines?size gt 0 && proc.outputs?size gt 0>
            modelBuilder.Entity<DB_${table.name}_${proc.name}>().HasNoKey();
            </#if>
            </#list>
            </#list>
        }

        <#list database.getTables() as table>
        <#list table.procs as proc>
        <#if !proc.isStdExtended() && !proc.isSProc() && proc.name != "" && proc.name != "Identity" && proc.lines?size gt 0 && proc.outputs?size gt 0>
        <#--  SI Specific Implementation if ever needed?  -->
        <#--  public abstract <#if proc.isSingle()>Task<${table.name}_${proc.name}><#elseif proc.outputs?size gt 0>Task<IEnumerable<${table.name}_${proc.name}>><#else>Task</#if> Execute${table.name}_${proc.name}  -->
        private static string ${table.name}${proc.name}Statement = @"
<#list base.formatEFLines(proc.lines) as pl>
${pl}
</#list>
";
        public virtual IQueryable<DB_${table.name}_${proc.name}> DB_${table.name}_${proc.name}(${base.getTypedFields(proc.inputs)})
            => ${table.name}_${proc.name}Set.FromSqlRaw(${table.name}${proc.name}Statement<#if proc.inputs?size gt 0>, </#if><#list proc.inputs as field>${field.name}<#compress><#sep>,</#compress></#list>);
        </#if>
        </#list>
        </#list>
    }
}