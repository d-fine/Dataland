<#macro renderValue value>
    <#if value.macroName == "text_macro">
        ${value.value}<#t>
    <#elseif value.macroName == "link_macro">
        ${value.title}[${baseUrl}${value.href}]<#t>
    <#elseif value.macroName == 'list_macro'>
        ${value.start}<#t>
        <#list value.values as element>
            <@renderValue element/><#sep>${value.separator}</#sep><#t>
        </#list>
        ${value.end}<#t>
    <#elseif value.macroName == 'email_address_with_subscription_status_macro'>
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