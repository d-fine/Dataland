<#macro renderValue value>
    <#if value.macro_name == "text_macro">
        ${value.value}<#t>
    <#elseif value.macro_name == "link_macro">
        ${value.title}[https://${baseUrl}${value.href}]<#t>
    <#elseif value.macro_name == 'list_macro'>
        ${value.start}<#t>
        <#list value.values as element>
            <@renderValue element/><#sep>${value.separator}</#sep><#t>
        </#list>
        ${value.end}<#t>
    <#elseif value.macro_name == 'email_address_with_subscription_status_macro'>
        ${value.emailAddress} (<#if value.subscribed>subscribed<#else>unsubscribed</#if>)<#t>
    <#else>
        Error: Macro not found<#t>
    </#if>
</#macro>
${textTitle}

<#list table as table_row>
${table_row.first} <@renderValue table_row.second/>
</#list>
Environment: ${baseUrl}