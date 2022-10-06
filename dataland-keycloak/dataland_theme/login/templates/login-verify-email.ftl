<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        ${msg("emailVerifyTitle")}
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <p class="instruction">
            ${msg("emailVerifyInstruction1")} <strong>${msg(user.email)}</strong> </p>
        <p>
            ${msg("emailVerifyInstruction2")}
            <a href="${url.loginAction}" class="text-primary">${msg("doClickHere")}</a> ${msg("emailVerifyInstruction3")}
        </p>

    </#if>
</@layout.registrationLayout>