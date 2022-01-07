########################################################################################################################
# Generated Code. DO NOT CHANGE THIS CODE. change it in the generator and regenerate
########################################################################################################################

<#list database.tables as table>

class T${table.name}(object):
    def __init__(self<#list table.fields as field>, ${field.name}=None</#list>):
    
    <#list table.fields as field>
        self.${field.name} = ${field.name}
    </#list>

<#list table.procs as proc>
class T${table.name}${proc.name}(T${table.name}):
    def __init__(self<#list proc.inputs as input>, ${input.name}=None</#list>, existing_rec=None):
        super(T${table.name}${proc.name}, self).__init__()
        <#list proc.inputs as input>
        self.${input.name} = None
        </#list>

        <#if proc.inputs?size gt 0>
        if existing_rec is not None:
            <#list proc.inputs as input>
            if hasattr(existing_rec, '${input.name}'):
                if existing_rec.${input.name} is not None:
                    self.${input.name} = existing_rec.${input.name}
            </#list>
        </#if>

        <#list proc.inputs as input>
        if ${input.name} is not None:
            self.${input.name} = ${input.name}
        </#list>

    def execute(self, cur):
        self.cur = cur
        

        output = <#if proc.outputs?size gt 0 && proc.outputs?first.isSequence()>"${proc.outputs?first.useName()}"<#else>None</#if>

        query = (<#list proc.lines as pl><#if pl.getlineval()?contains("_ret.") != true>"${pl.getlineval()?replace("\\", "\\\\")}"<#if pl.getlineval() == " ) "><#if proc.isStd() = true && proc.outputs?size gt 0 && proc.outputs?first.isSequence()>
                "OUTPUT Inserted." + output +</#if></#if>
               </#if></#list>)

        self.cur.execute(query, <#list proc.inputs as input>self.${input.name}, </#list>)
    
    def fetch(self):
        # type: () -> T${table.name}
        return self.cur.fetchone()

    def fetch_all(self):
        # type: () -> [T${table.name}]
        return self.cur.fetchall()

    </#list>
</#list>
