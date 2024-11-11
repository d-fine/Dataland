<#macro renderValue value>
    <#if value.macroName == "text_macro">
        ${value.value}<#t>
    <#elseif value.macroName == "link_macro">
        <a href="https://${baseUrl}${value.href}">${value.title}</a><#t>
    <#elseif value.macroName == 'list_macro'>
        ${value.start}<#t>
        <#list value.values as subValue>
            <@renderValue subValue/><#sep>${value.separator}</#sep><#t>
        </#list>
        ${value.end}<#t>
    <#elseif value.macroName == 'email_address_with_subscription_status_macro'>
        ${value.emailAddress} (<#if value.subscribed>subscribed<#else>unsubscribed</#if>)<#t>
    <#else>
        Error: Macro not found<#t>
    </#if>
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
    <div class="header">${htmlTitle}</div>
    <#list table as table_row>
        <div class="section"> <span class="bold">${table_row.first}: </span><@renderValue table_row.second/></div>
    </#list>
    <div class="section"> <span class="bold">Environment: </span>${baseUrl}</div>
</div>
</body>
</html>