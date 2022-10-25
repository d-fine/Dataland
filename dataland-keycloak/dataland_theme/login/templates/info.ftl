<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if messageHeader??>
            ${messageHeader}
        <#else>
            <#if message.summary == msg('confirmEmailAddressVerification')>
                ${kcSanitize(msg("confirmEmailAddressVerificationHeader"))?no_esc}
            <#elseif message.summary == msg('emailVerifiedMessage')>
                ${kcSanitize(msg("emailVerifiedMessageHeader"))?no_esc}
            <#else>
                ${message.summary}
            </#if>
        </#if>
    <#elseif section = "backUrl">/<#elseif section = "backName">HOME
    <#elseif section = "form">
        <div id="kc-info-message">
            <p class="instruction">
                <#if message.summary == msg('confirmEmailAddressVerification')>
                    ${kcSanitize(msg("confirmEmailAddressVerification"))?no_esc}
                <#elseif  message.summary == msg('emailVerifiedMessage')>
                    ${kcSanitize(msg("emailVerifiedMessage"))?no_esc}
                <#else>
                    ${message.summary}
                </#if>
                <#if requiredActions??><#list requiredActions>: <b><#items as reqActionItem>${msg("requiredAction.${reqActionItem}")}<#sep>, </#items></b></#list><#else></#if></p>
            <#if skipLink??>
            <#else>
                <#if pageRedirectUri?has_content>
                    <p><a href="${pageRedirectUri}" class="text-primary">${kcSanitize(msg("backToApplication"))?no_esc}</a></p>
                <#elseif actionUri?has_content>
                    <p><a href="${actionUri}" class="text-primary">${kcSanitize(msg("proceedWithAction"))?no_esc}</a></p>
                <#elseif (client.baseUrl)?has_content>
                    <p><a href="${client.baseUrl}" class="text-primary">${kcSanitize(msg("backToApplication"))?no_esc}</a></p>
                </#if>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>