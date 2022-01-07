########################################################################################################################
# Generated Code. DO NOT CHANGE THIS CODE. change it in the generator and regenerate
########################################################################################################################

from bbdservicebus.node_manager import node_manager
import datetime
import sys
py2 = False
if sys.version_info[0] < 3:
    py2 = True

<#--  <#list database.tables as table>  -->

class T${table.name}(object):
    def __init__(self<#list table.fields as field>, ${field.name?lower_case}=None</#list>):

    <#list table.fields as field>
        self.${field.name?lower_case} = ${field.name?lower_case}
    </#list>

        self.dbcontroller = None


    <#list table.procs as proc>
class T${table.name}${proc.name}(T${table.name}):
    def __init__(self<#list proc.inputs as input>, ${input.name?lower_case}=None</#list>, existing_rec=None):
        super(T${table.name}${proc.name}, self).__init__()
        <#list proc.inputs as input>
        self.${input.name?lower_case} = None
        </#list>
        <#list proc.outputs as output>
        self.${output.name?lower_case} = None
        </#list>

        <#if proc.inputs?size gt 0>
        if existing_rec is not None:
            <#list proc.inputs as input>
            if hasattr(existing_rec, '${input.name?lower_case}'):
                if existing_rec.${input.name?lower_case} is not None:
                    if hasattr(existing_rec.${input.name?lower_case}, 'encode') and py2:
                        self.${input.name?lower_case} = str(existing_rec.${input.name?lower_case}.encode('utf-8'))
                    else:
                        self.${input.name?lower_case} = str(existing_rec.${input.name?lower_case})
            </#list>
        </#if>

        <#list proc.inputs as input>
        if ${input.name?lower_case} is not None:
            if hasattr(${input.name?lower_case}, 'encode') and py2:
                self.${input.name?lower_case} = str(${input.name?lower_case}.encode('utf-8'))
            else:
                self.${input.name?lower_case} = str(${input.name?lower_case})
        </#list>

        self._dbcontroller = None
        self.QueryObj = None

    def execute(self, db_controller):
        self._dbcontroller = db_controller
        self.QueryObj = node_manager.Query()


        output = <#if proc.outputs?size gt 0 && proc.outputs?first.isSequence()>"${proc.outputs?first.useName()}"<#else>None</#if>

        query = (<#list proc.lines as pl><#if pl.getlineval()?contains("_ret.") != true>"${pl.getlineval()?replace("\\", "\\\\")}"<#if pl.getlineval() == " ) ">
        </#if>
    </#if></#list>)

        self.QueryObj.command = query
        self.QueryObj.params = node_manager.TransferRecord(${proc.inputs?size?c})
        self.QueryObj.out = node_manager.TransferRecord(${proc.outputs?size?c})

        ###################
        # inputs          #
        ###################
        <#list proc.inputs as input>
        # ${input.getName()}
        param${input?index} = node_manager.FieldValue()
        param${input?index}.attr = node_manager.ListOfAttribs([${input.type?c}, ${input.length?c}, ${input.scale?c}, ${input.precision?c}, ${input.isNull()?then(1, 0)}, ${input.isNull()?then(input?index, 0)}])
        if self.${input.name?lower_case} is not None:
            if py2:
                param${input?index}.szValue = <#if input.type?c == '1'>self.${input.name?lower_case}.encode('utf-8')
            <#elseif input.type?c == '4'>self.${input.name?lower_case}.encode('utf-8')
            <#elseif input.type?c == '21'>self.${input.name?lower_case}.encode('utf-8')
            <#elseif input.type?c == '23'>self.${input.name?lower_case}.encode('utf-8')
            <#else>self.${input.name?lower_case}</#if>
            else:
                param${input?index}.szValue = <#if input.type?c == '1'>self.${input.name?lower_case}
        <#elseif input.type?c == '4'>self.${input.name?lower_case}
        <#elseif input.type?c == '21'>self.${input.name?lower_case}
        <#elseif input.type?c == '23'>self.${input.name?lower_case}
        <#else>self.${input.name?lower_case}</#if>
            param${input?index}.hasszValue = True
        else:
            <#if input.defaultValue == "" && !input.isNull()>raise ValueError("No default Value specified and no Value passed in for ${input.name?lower_case} for Query: ${proc.name}.")</#if>
            <#if input.defaultValue == "" && input.isNull()>param${input?index}.szValue = '-1'</#if>
            <#if input.defaultValue != "">param${input?index}.szValue = '${input.defaultValue}'</#if>
        self.QueryObj.params[${input?index}] = param${input?index?c}

        </#list>

        <#if proc.outputs?size gt 0>
        ###################
        # outputs         #
        ###################

            <#list proc.outputs as output>
        # ${output.getName()}
        outs${output?index} = node_manager.FieldValue()
        outs${output?index}.attr = node_manager.ListOfAttribs([${output.type?c}, ${output.length?c}, ${output.scale?c}, ${output.precision?c}, ${output.isNull()?then(1, 0)}, ${output.isNull()?then(output?index, 0)}])
        self.QueryObj.out[${output?index}] = outs${output?index}

            </#list>
        <#else>
        # Proc has no outputs
        </#if>

        <#if proc.outputs?size gt 0>
    @staticmethod
    def get_row_obj():
        ret = node_manager.TransferRecord()

            <#list proc.outputs as output>
        outsrec${output?index} = node_manager.FieldValue()
        outsrec${output?index}.attr = node_manager.ListOfAttribs([${output.type?c}, ${output.length?c}, ${output.scale?c}, ${output.precision?c}, ${output.isNull()?then(1, 0)}, ${output.isNull()?then(output?index, 0)}])
        ret.append(outsrec${output?index})

            </#list>

        return ret

    def fetch(self):
        temp = T${table.name}()
        row = self._dbcontroller.fetch(self.get_row_obj())

        # check to see if any rows were returned
        if row.size() == 0:
            return None
        
        
            <#list proc.outputs as output>
        if row[${output?index}].hasdValue:
            temp.${output.name?lower_case} = row[${output?index}].dValue
        elif row[${output?index}].hasfValue:
            temp.${output.name?lower_case} = row[${output?index}].fValue
        elif row[${output?index}].hasiValue:
            temp.${output.name?lower_case} = row[${output?index}].iValue
        elif row[${output?index}].hasi16Value:
            temp.${output.name?lower_case} = row[${output?index}].i16Value
        elif row[${output?index}].hasi64Value:
            temp.${output.name?lower_case} = row[${output?index}].i64Value
        elif row[${output?index}].szValue:
            temp.${output.name?lower_case} = <#if output.type?c == '1'>row[${output?index}].szValue.decode('hex')<#else>row[${output?index}].szValue</#if>
            <#if output.type?c == '18' || output.type?c == '6'>temp.${output.name?lower_case} = datetime.datetime.strptime(temp.${output.name?lower_case}, '%Y%m%d%H%M%S').isoformat()</#if>
        else:
            temp.${output.name?lower_case} = None

            </#list>
        return temp

    def fetch_all(self):
        all_rows = []
        rows_ret = self._dbcontroller.fetch_all(self.get_row_obj())

        for row in rows_ret:
            temp = T${table.name}()
            <#list proc.outputs as output>
            if row[${output?index}].hasdValue:
                temp.${output.name?lower_case} = row[${output?index}].dValue
            elif row[${output?index}].hasfValue:
                temp.${output.name?lower_case} = row[${output?index}].fValue
            elif row[${output?index}].hasiValue:
                temp.${output.name?lower_case} = row[${output?index}].iValue
            elif row[${output?index}].hasi16Value:
                temp.${output.name?lower_case} = row[${output?index}].i16Value
            elif row[${output?index}].hasi64Value:
                temp.${output.name?lower_case} = row[${output?index}].i64Value
            elif row[${output?index}].szValue:
                temp.${output.name?lower_case} = <#if output.type?c == '1'>row[${output?index}].szValue.decode('hex')<#else>row[${output?index}].szValue</#if>
                <#if output.type?c == '18' || output.type?c == '6'>temp.${output.name?lower_case} = datetime.datetime.strptime(temp.${output.name?lower_case}, '%Y%m%d%H%M%S').isoformat()</#if>
            else:
                temp.${output.name?lower_case} = None

            </#list>
            all_rows.append(temp)
        return all_rows

        </#if>
    </#list>
<#--  </#list>  -->
	