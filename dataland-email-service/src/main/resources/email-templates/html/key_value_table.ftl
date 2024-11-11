<#macro renderValue value>
    <#if value.macro_name == "text_macro">
        ${value.value}
    <#elseif value.macro_name == "link_macro">
        <a href="https://${baseUrl}${value.href}">${value.title}</a>
    <#elseif value.macro_name == 'list_macro'>
        <@renderList value/>
    <#elseif value.macro_name == 'email_address_with_subscription_status'>
        value.emailAddress (<#if value.isSubscribed>subscribed<#else>unsubscribed</#if>)
    <#else>
        Error: Macro not found
    </#if>
</#macro>

<#macro renderList value>
    ${value.start}
    <#list value.values as subValue>
        <@renderValue subValue/><#sep>${value.separator}</#sep>
    </#list>
    ${value.end}
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