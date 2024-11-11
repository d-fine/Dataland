<#macro renderValue value>
    <#if value.macro_name == "text_macro">
        ${value.value}
    <#elseif value.macro_name == "link_macro">
        ${value.title}[https://${baseUrl}${value.href}]
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
    <#list value.values as element>
        <@renderValue element/><#sep>${value.separator}</#sep>
    </#list>
    ${value.end}
</#macro>

${textTitle}

<#list table as table_row>
    ${table_row.first} <@renderValue table_row.second/>
</#list>
Environment: ${baseUrl}