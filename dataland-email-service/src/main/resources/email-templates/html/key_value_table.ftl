<#macro renderValue value>
    <#if value.macro_name == "text_macro">
        <@text value/>
    <#else>
        Error: Macro not found
    </#if>
</#macro>

<#macro text value>
${value.value}
</#macro>

<html>
<head>
<style>
    body {
        font-family: Arial, sans-serif;
        color: #333;
    }
    .container {
        max-width: 600px;
        margin: 0 auto;
        padding: 20px;
        border-radius: 10px;
    }
    .header {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 10px;
    }
    .section {
        margin-bottom: 10px;
    }
    .bold {
        font-weight: bold;
    }
</style>
</head>
<body>
<div class="container">
    <div class="header">${title}</div>
    <#list table as table_row>
        <div class="section"> <span class="bold">${table_row.first}: </span> <@renderValue table_row.second/> </div>
    </#list>
</div>
</body>
</html>