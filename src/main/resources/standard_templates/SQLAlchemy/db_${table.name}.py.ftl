<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
########################################################################################################################
################## Generated Code. DO NOT CHANGE THIS CODE. Change it in the generator and regenerate ##################
########################################################################################################################

<#function getSQLAlchemyColumnType field>
    <#if field.type?c == '1'><#return "Binary()">
    <#elseif field.type?c == '2'><#return "Boolean()">
    <#elseif field.type?c == '3'><#return "SmallInteger()">
    <#elseif field.type?c == '4'><#return "String(length=#{field.length; M0})">
    <#elseif field.type?c == '5'><#return "DateTime()">
    <#elseif field.type?c == '6'><#return "DateTime()">
    <#elseif field.type?c == '7'><#return "Numeric(precision=${field.precision}, scale=${field.scale})">
    <#elseif field.type?c == '9'><#return "Float(precision=${field.precision})">
    <#elseif field.type?c == '10'><#return "Integer()">
    <#elseif field.type?c == '11'><#return "Integer()">
    <#elseif field.type?c == '12'><#return "BigInteger()">
    <#elseif field.type?c == '13'><#return "Numeric(precision=${field.precision}, scale=${field.scale})">
    <#elseif field.type?c == '14'><#return "Integer()">
    <#elseif field.type?c == '15'><#return "SmallInteger()">
    <#elseif field.type?c == '17'><#return "DateTime()">
    <#elseif field.type?c == '18'><#return "DateTime()">
    <#elseif field.type?c == '19'><#return "Binary()">
    <#elseif field.type?c == '20'><#return "DateTime()">
    <#elseif field.type?c == '21'><#return "String(length=#{field.length; M0})">
    <#elseif field.type?c == '23'><#return "String(length=#{field.length; M0})">
    <#elseif field.type?c == '24'><#return "BigInteger()">
    <#elseif field.type?c == '25'><#return "BigInteger()">
    <#elseif field.type?c == '26'><#return "DateTime()">
    <#elseif field.type?c == '27'><#return "Unicode(length=#{field.length; M0})">
    <#elseif field.type?c == '28'><#return "Unicode(#{field.length; M0})">
    <#elseif field.type?c == '29'><#return "Unicode(#{field.length; M0})">
    <#elseif field.type?c == '30'><#return "String(length=#{field.length; M0})">
    <#else><#return "String(100)">
    </#if>>
</#function>
<#function getSQLAlchemyBaseType field>
    <#if field.type?c == '1'><#return "Binary">
    <#elseif field.type?c == '2'><#return "Boolean">
    <#elseif field.type?c == '3'><#return "SmallInteger">
    <#elseif field.type?c == '4'><#return "String">
    <#elseif field.type?c == '5'><#return "DateTime">
    <#elseif field.type?c == '6'><#return "DateTime">
    <#elseif field.type?c == '7'><#return "Numeric">
    <#elseif field.type?c == '9'><#return "Float">
    <#elseif field.type?c == '10'><#return "Integer">
    <#elseif field.type?c == '11'><#return "Integer">
    <#elseif field.type?c == '12'><#return "BigInteger">
    <#elseif field.type?c == '13'><#return "Numeric">
    <#elseif field.type?c == '14'><#return "Integer">
    <#elseif field.type?c == '15'><#return "SmallInteger">
    <#elseif field.type?c == '17'><#return "DateTime">
    <#elseif field.type?c == '18'><#return "DateTime">
    <#elseif field.type?c == '19'><#return "Binary">
    <#elseif field.type?c == '20'><#return "DateTime">
    <#elseif field.type?c == '21'><#return "String">
    <#elseif field.type?c == '23'><#return "String">
    <#elseif field.type?c == '24'><#return "BigInteger">
    <#elseif field.type?c == '25'><#return "BigInteger">
    <#elseif field.type?c == '26'><#return "DateTime">
    <#elseif field.type?c == '27'><#return "Unicode">
    <#elseif field.type?c == '28'><#return "Unicode">
    <#elseif field.type?c == '29'><#return "Unicode">
    <#elseif field.type?c == '30'><#return "String">
    <#else><#return "String">
    </#if>>
</#function>
<#function getColumnAttributes field table>
<#-- @ftlvariable name="field" type="bbd.jportal2.Field" -->
<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
    <#local retVal = "">
    <#if field.type?c == '14' || field.type?c == '24'>
        <#local retVal = retVal + ', sa.Sequence("' + table.getName()?upper_case + 'SEQ", metadata=Base.metadata, schema=${table.getName()?upper_case}_SCHEMA)'>
    </#if>
    <#if table.getLinkForField(field)??>
        <#assign link = table.getLinkForField(field)>
        <#if link.getName() != table.name>
        <#local retVal = retVal + ", sa.ForeignKey(DB_" + link.getName() + "." + link.getFirstLinkField() + ")">
        </#if>
    </#if>
    <#if field.isPrimaryKey()>
        <#if field.type?c == '10' || field.type?c == '14' || field.type?c == '24' || field.type?c == '25'>
            <#-- All the sequence types.-->
            <#local retVal = retVal + ", primary_key=True">
        <#else>
            <#local retVal = retVal + ", primary_key=True, autoincrement=False">
        </#if>
    </#if>
    <#if field.isNull()><#local retVal = retVal + ", nullable=True"></#if>
    <#if field.getDefaultValue() != ""><#local retVal = retVal + ", default='" + field.getDefaultValue() +"'"></#if>
    <#if field.type?c == '18'>
        <#local retVal = retVal + ", default=datetime.now, onupdate=datetime.now">
    </#if>
    <#if field.type?c == '10' || field.type?c == '25'>
        <#local retVal = retVal + ", autoincrement=True">
    <#elseif field.type?c == '14' || field.type?c == '24'>
        <#local retVal = retVal + ", autoincrement=False">
    </#if>
    <#return retVal>
</#function>
<#function getPythonType apply_to_pk field>
<#-- @ftlvariable name="field" type="bbd.jportal2.Field" -->

    <#assign prefix = ''>
    <#assign suffix = ''>

    <#if field.isNull() || (apply_to_pk && field.isPrimaryKey())>
        <#assign prefix = 'Optional['>
        <#assign suffix = ']'>
    </#if>

    <#if field.type?c == '1'><#return prefix + "Any" + suffix>
    <#elseif field.type?c == '2'><#return prefix + "bool" + suffix>
    <#elseif field.type?c == '3'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '4'><#return prefix + "str" + suffix>
    <#elseif field.type?c == '5'><#return prefix + "datetime" + suffix>
    <#elseif field.type?c == '6'><#return prefix + "datetime" + suffix>
    <#elseif field.type?c == '7'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '9'><#return prefix + "float" + suffix>
    <#elseif field.type?c == '10'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '11'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '12'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '13'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '14'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '15'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '17'><#return prefix + "datetime" + suffix>
    <#elseif field.type?c == '18'><#return prefix + "datetime" + suffix>
    <#elseif field.type?c == '19'><#return prefix + "Any" + suffix>
    <#elseif field.type?c == '20'><#return prefix + "datetime" + suffix>
    <#elseif field.type?c == '21'><#return prefix + "str" + suffix>
    <#elseif field.type?c == '23'><#return prefix + "str" + suffix>
    <#elseif field.type?c == '24'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '25'><#return prefix + "int" + suffix>
    <#elseif field.type?c == '26'><#return prefix + "datetime" + suffix>
    <#elseif field.type?c == '27'><#return prefix + "str" + suffix>
    <#elseif field.type?c == '28'><#return prefix + "str" + suffix>
    <#elseif field.type?c == '29'><#return prefix + "str" + suffix>
    <#elseif field.type?c == '30'><#return prefix + "str" + suffix>
    <#else><#return prefix + "str" + suffix>
    </#if>>
</#function>

<#function getTableReturnType proc tableName>
    <#if proc.outputs?size <= 0><#return "None">
    <#elseif proc.isSingle()><#return "Optional['${tableName}']">
    <#else><#return "List['${tableName}']">
    </#if>>
</#function>

<#function isMultiLinkFk link table>
<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
<#-- @ftlvariable name="link" type="bbd.jportal2.Link" -->
    <#local tableRefCount = 0>
    <#local externalTableName = link.getName()>

    <#list table.getFields() as field>
        <#if table.getLinkForField(field)??>
            <#local linkL = table.getLinkForField(field) >
            <#if linkL.getName() == externalTableName>
                <#return true>
            </#if>
        </#if>
    </#list>
    <#return false>
</#function>

<#function getFkAdditionalParams link table>
<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
<#-- @ftlvariable name="link" type="bbd.jportal2.Link" -->
    <#if isMultiLinkFk(link, table)>
        <#local externalTableName = link.getName()>
        <#local field = table.getFieldForLink(link) >
        <#return ", foreign_keys=[${field.name}]">
<#--        <#list table.getFields() as field>-->
<#--            <#if table.getLinkForField(field)??>-->
<#--                <#local linkL = table.getLinkForField(field) >-->
<#--                <#if linkL.getName() == externalTableName>-->
<#--                    <#return ", foreign_keys=[${field.name}]">-->
<#--                </#if>-->
<#--            </#if>-->
<#--        </#list>-->
    </#if>
    <#return "">
</#function>

<#function getFkName link table>
<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
<#-- @ftlvariable name="link" type="bbd.jportal2.Link" -->
    <#local retVal = "${link.getName()}">
    <#if isMultiLinkFk(link, table)>
        <#local retVal = retVal + "_" + link.fields?first>
    </#if>

    <#return retVal>
</#function>

from dataclasses import dataclass, field
from datetime import datetime
from typing import List, Any, Optional

import sqlalchemy as sa
from sqlalchemy.orm import Session
from sqlalchemy.sql.expression import TextAsFrom

from bbdcommon.database.db_common import DBMixin, Base, DBColumn
<#list table.getLinks() as link>
<#if link.getName() != table.name>
from .db_${link.getName()} import DB_${link.getName()}
</#if>
</#list>

<#function getConstructorList table>
<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
    <#local fieldNames = table.fields?filter(field -> field.name?lower_case != 'tmstamp' && field.type != 10 && field.type != 14 && field.type != 24 && field.type != 25)?map(field -> field.name + ': ' + getPythonType(true, field))>
    <#return ", " + fieldNames?join(", ")>
</#function>

<#function getConstructorSetList table>
<#-- @ftlvariable name="table" type="bbd.jportal2.Table" -->
    <#local fieldSetNames = table.fields?filter(field -> field.name?lower_case != 'tmstamp' && field.type != 10 && field.type != 14 && field.type != 24 && field.type != 25)?map(field -> field.name + "=" + field.name)>
    <#return fieldSetNames?join(",\n            ")>
</#function>

${table.getName()?upper_case}_SCHEMA = "${table.getDatabase().getSchema()?lower_case}"


class DB_${table.name}(Base, DBMixin):
    <#list table.fields as field>
<#--        <#if field.name?lower_case != 'tmstamp'>-->
    ${field.name}: <#compress>${getPythonType(false, field)}</#compress> = DBColumn("${field.name?lower_case}", <#compress>sa.${getSQLAlchemyColumnType(field)}${getColumnAttributes(field, table)})</#compress>
<#--        </#if>-->
    </#list>
<#--backref="F_${getFkName(link table)}"-->
    <#if table.links?size gt 0>

    # Foreign Key Links
        <#list table.getLinks() as link>
        <#if link.getName() != table.name>
    F_${getFkName(link table)} = sa.orm.relationship(DB_${link.getName()}${getFkAdditionalParams(link, table)})
        </#if>
        </#list>
    </#if>

    __schema__ = ${table.getName()?upper_case}_SCHEMA

    def __init__(self${getConstructorList(table)}):
        super(DB_${table.name}, self).__init__(
            ${getConstructorSetList(table)})
<#list table.procs as proc>
<#if !proc.isStdExtended() && !proc.isSProc() && proc.name != "">
    <#if proc.lines?size gt 0>


@dataclass
class DB_${table.name}${proc.name}:
    <#list proc.outputs as field>
    <#--  !table.hasField(field.name) &&   -->
    ${field.name}: <#compress>${getPythonType(false, field)} = field(default=None)</#compress>
    </#list>

    @classmethod
    def get_statement(cls
                     <#list proc.inputs as field>, ${field.name}: ${getPythonType(false, field)}
                     </#list>) -> TextAsFrom:
        statement = sa.text(<#list proc.lines as pl>
                        <#if pl.getUnformattedLine()?contains("_ret.") != true>"${pl.getUnformattedLine()?replace("\\", "\\\\")}"<#if pl.getUnformattedLine() == " ) "></#if></#if></#list>)
        text_statement = statement.columns(<#list proc.outputs as field>${field.name}=sa.types.${getSQLAlchemyBaseType(field)},
                                      </#list>)
        <#--  statement = statement.columns(<#list proc.outputs as field>column('${field.name}'), \
                                    </#list>)  -->
        <#if proc.inputs?size gt 0>
        text_statement = text_statement.bindparams(<#list proc.inputs as field>${field.name}=${field.name},
                                         </#list>)
        </#if>
        return text_statement

    @classmethod
    def execute(cls, session: Session<#list proc.inputs as field>, ${field.name}: ${getPythonType(false, field)}
                     </#list>) -> ${getTableReturnType(proc, "DB_" + table.name + proc.name)}:
        res = session.execute(cls.get_statement(<#list proc.inputs as field>${field.name},
                     </#list>))
        <#if proc.isSingle()>
        rec = res.fetchone()
        if rec:
            res.close()
            return DB_${table.name}${proc.name}(*rec)

        return None
        <#elseif proc.outputs?size gt 0>
        recs = res.fetchall()
        return [DB_${table.name}${proc.name}(*r) for r in recs]
        <#else>
        res.close()
        </#if>
    </#if>
</#if>
</#list>
