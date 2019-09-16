<#function JPortalJavaFieldTypeLookup typeInt >
    <#if typeInt == 1 >
        <#return "String">        <#-- BLOB -->
    <#elseif typeInt == 2 >
        <#return "BOOLEAN">
    <#elseif typeInt == 3 >
        <#return "byte">          <#-- BYTE -->
    <#elseif typeInt == 4 >
        <#return "String">        <#-- CHAR -->
    <#elseif typeInt == 5 >
        <#return "java.sql.Date"> <#-- DATE -->
    <#elseif typeInt == 6 >
        <#return "Timestamp">     <#-- DATETIME -->
    <#elseif typeInt == 7 >
        <#return "double">        <#-- DOUBLE -->
    <#elseif typeInt == 8 >
        <#return "Unknown">       <#-- Dynamic -->
    <#elseif typeInt == 9 >
        <#return "double">        <#-- FLOAT -->
    <#elseif typeInt == 10>
        <#return "int">           <#-- IDENTITY -->
    <#elseif typeInt == 11>
        <#return "int">           <#-- INT -->
    <#elseif typeInt == 12>
        <#return "long">          <#-- LONG -->
    <#elseif typeInt == 13>
        <#return "double">        <#-- MONEY (I think this should rather be BigDecimal -->
    <#elseif typeInt == 14>
        <#return "int">           <#-- SEQUENCE -->
    <#elseif typeInt == 15>
        <#return "short">         <#-- SHORT -->
    <#elseif typeInt == 16>
        <#return "Unknown">       <#-- STATUS -->
    <#elseif typeInt == 17>
        <#return "Time">          <#-- TIME -->
    <#elseif typeInt == 18>
        <#return "Timestamp">     <#-- TIMESTAMP -->
    <#elseif typeInt == 19>
        <#return "String">        <#-- TLOB -->
    <#elseif typeInt == 20>
        <#return "String">        <#-- USERSTAMP -->
    <#elseif typeInt == 21>
        <#return "String">        <#-- ANSICHAR -->
    <#elseif typeInt == 22>
        <#return "Unknown">       <#-- UID -->
    <#elseif typeInt == 23>
        <#return "Unknown">       <#-- XML -->
    <#elseif typeInt == 24>
        <#return "long">           <#-- BIGSEQUENCE -->
    <#elseif typeInt == 25>
        <#return "long">           <#-- BIGIDENTITY -->
    <#elseif typeInt == 26>
        <#return "Unknown">       <#-- AUTOTIMESTAMP -->
    <#elseif typeInt == 27>
        <#return "Unknown">       <#-- WCHAR -->
    <#elseif typeInt == 28>
        <#return "Unknown">       <#-- WANSICHAR -->
    <#elseif typeInt == 29>
        <#return "Unknown">       <#-- UTF8 -->
    <#elseif typeInt == 30>
        <#return "Unknown">       <#-- BIGXML -->
    </#if>
</#function>