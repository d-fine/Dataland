<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        ${msg("emailVerifyTitle")}
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <div class="instruction text-left">
            <p>
                ${msg("emailVerifyInstruction4")}
            </p>
            <p>
                ${msg("emailVerifyInstruction1")} <strong>${msg(user.email)}</strong>
            </p>
            <p>
                ${msg("emailVerifyInstruction2")}
                <a href="${url.loginAction}" class="text-primary">${msg("doClickHere")}</a> ${msg("emailVerifyInstruction3")}
            </p>
        </div>

    </#if>
</@layout.registrationLayout>
