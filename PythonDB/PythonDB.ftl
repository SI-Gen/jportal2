########################################################################################################################
# Generated Code. DO NOT CHANGE THIS CODE. change it in the generator and regenerate
########################################################################################################################

import node_manager

<#list database.tables as table>
class ${table.name}_DB:
    NodeManager = None
    def __init__(self, nm):
        self.nm = nm

    <#list table.procs as proc>
    def Q_${proc.name}(self, key<#list proc.inputs as input>, val${input?index}</#list>):
        if not self.nm:
             raise ValueError("NodeManager is empty")

        #name list to store names of fields for return dict
        nameList = []

        access = node_manager.Query()
        query = "<#list proc.lines as pl>${pl.getlineval()}\
                </#list>"

        access.command = query
        access.params = node_manager.TransferRecord(${proc.inputs?size?c})
        access.out = node_manager.TransferRecord(${proc.outputs?size?c})
        ###################
        # inputs
        ###################
        <#list proc.inputs as input>
        # ${input.getName()}
        param${input?index} = node_manager.FieldValue()
        param${input?index}.attr = node_manager.ListOfAttribs([${input.type?c}, ${input.length?c}, ${input.scale?c}, ${input.precision?c}])
        param${input?index}.szValue = val${input?index?c}
        access.params[${input?index}] = param${input?index?c}

        </#list>
        ###################
        # outputs
        ###################
        <#list proc.outputs as output>
        # ${output.getName()}
        nameList.append('${output.getName()}')
        outs${output?index} = node_manager.FieldValue()
        outs${output?index}.attr = node_manager.ListOfAttribs([${output.type?c}, ${output.length?c}, ${output.scale?c}, ${output.precision?c}])
        access.out[${output?index}] = outs${output?index}

        </#list>
        ret = node_manager.TransferRecord()

        <#list proc.outputs as output>
        outsrec${output?index} = node_manager.FieldValue()
        outsrec${output?index}.attr = node_manager.ListOfAttribs([${output.type?c}, ${output.length?c}, ${output.scale?c}, ${output.precision?c}])
        ret.append(outsrec${output?index})

        </#list>
        if not self.nm.Execute(access, key):
            raise ValueError("Could not execute query")


        rows_ret = self.nm.GetAll(ret, key)
        ret = []
        for row in rows_ret:
            row_l = {}
            i = 0
            for col in row:
                val = None
                if col.dValue != 0:
                    val = col.dValue
                if col.fValue != 0:
                    val = col.fValue
                if col.iValue != 0:
                    val = col.iValue
                if col.i16Value != 0:
                    val = col.i16Value
                if col.i64Value != 0:
                    val = col.i64Value
                if col.szValue:
                    val = col.szValue
                row_l[nameList[i]] = val
                i = i + 1
            ret.append(row_l)
        return ret

    </#list>
</#list>