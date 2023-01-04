<#import "dataland_template.ftl" as layout>
<#import "components/input_field.ftl" as inputField>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        Reset password
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <div class="text-left">Enter a new password for your <strong class="white-space-nowrap">${username}</strong> account.</div>

        <form id="kc-passwd-update-form" action="${url.loginAction}" method="post">

            <input type="text" id="username" name="username" value="${username}" autocomplete="username"
                   readonly="readonly" style="display:none;"/>
            <input type="password" id="password" name="password" autocomplete="current-password" style="display:none;"/>

            <@inputField.dala
                fieldName="password-new"
                fieldErrorHandlers=["password", "password-confirm"]
                fieldHeading="Password"
                wrappingDivAttributes="data-role=\"password-primary\""
                tabindex="1"
                autofocus=false
                type="password"
                autocomplete="off"
            >
                <div class="bg-black-alpha-20 d-progressbar mt-2" id="password-strength-indicator">
                    <div class="d-progressbar-value flex d-password-strength-empty">
                    </div>
                </div>
            </@inputField.dala>

            <@inputField.dala
                fieldName="password-confirm"
                fieldErrorHandlers=["password", "password-confirm"]
                fieldHeading="Repeat password"
                wrappingDivAttributes="data-role=\"password-confirm\""
                tabindex="2"
                autofocus=false
                type="password"
                autocomplete="off"
            />

            <#if isAppInitiatedAction??>
                <div class="text-left flex align-items-center">
                    <input tabindex="3" id="logout-sessions" name="accept_terms" type="checkbox">
                    <label for="logout-sessions" class="tex-sm ml-2">${msg("logoutOtherSessions")}</label>
                </div>
            </#if>

            <div id="kc-form-buttons">
                <input tabindex="4" class="p-button w-full cursor-pointer font-semibold mt-5 p-login-button" type="submit" value="Reset password" />
                <#if isAppInitiatedAction??>
                    <button tabindex="5" formnovalidate class="p-button uppercase w-full text-primary justify-content-center bg-white-alpha-10 cursor-pointer font-semibold mt-1 p-login-button" type="submit" name="cancel-aia" value="true" />Cancel</button>
                </#if>
            </div>


        </form>
        <script type="text/javascript" src="${url.resourcesPath}/passwordStrength.js"></script>
    </#if>
</@layout.registrationLayout>
