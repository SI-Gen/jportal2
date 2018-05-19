Generated Files
===============
<#list database.generatedOutputFiles as generatedFiles>
    Generator Type: ${(generatedFiles.generatorName)!"NO_GENERATOR_NAME_SPECIFIED"}
    <#list generatedFiles.fileGroups as fileGroup>
        File Group: ${fileGroup.fileGroupName}
        <#list fileGroup.files as outputFileName>
            ${outputFileName.toString()} (${outputFileName.getFileName()})
        </#list>

    </#list>

</#list>
