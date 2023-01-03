<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout
    displayMessage=false
    formContainerStyle="width: 480px;"
    outerContainerStyle="max-width: 600px;"
; section>
    <#if section = "header">
        ${msg("emailLinkIdpTitle", idpDisplayName)}
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <div class="text-left pb-3 font-semibold">
            <p>
                ${kcSanitize(message.summary)?no_esc}
            </p>
            <p>
                An email with instructions to link your ${idpDisplayName} account <strong>${brokerContext.username}</strong> with your Dataland account has been sent to you.
            </p>
            <p>
                ${msg("emailLinkIdp2")} <a href="${url.loginAction}" class="text-primary">${msg("doClickHere")}</a> ${msg("emailLinkIdp3")}
            </p>
            <p>
                ${msg("emailLinkIdp4")} <a href="${url.loginAction}" class="text-primary">${msg("doClickHere")}</a> ${msg("emailLinkIdp5")}
            </p>
        </div>
    </#if>
</@layout.registrationLayout>
