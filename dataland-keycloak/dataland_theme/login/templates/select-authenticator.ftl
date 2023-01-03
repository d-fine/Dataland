<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout
    displayMessage=false
    formContainerStyle="width: 635px;"
    outerContainerStyle=""
    ; section>
    <#if section = "header">
        <script type="text/javascript">
            function fillAndSubmit(authExecId) {
                document.getElementById('authexec-hidden-input').value = authExecId;
                document.getElementById('kc-select-credential-form').submit();
            }
        </script>
        ${msg("loginChooseAuthenticator")}
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <form id="kc-select-credential-form" class="pt-5" action="${url.loginAction}" method="post">
            <#list auth.authenticationSelections as authenticationSelection>
                <div class="mt-4 cursor-pointer d-link-account-box w-full flex justify-content-between align-items-center px-3" onclick="fillAndSubmit('${authenticationSelection.authExecId}')">
                    <span class>${msg('${authenticationSelection.helpText}')}</span>

                    <div class="text-primary flex align-items-center gap-3 py-5">
                        <span class="font-semibold">
                            ${msg('${authenticationSelection.displayName}')}
                        </span>
                        <span class="material-icons">
                        <#if authenticationSelection.authenticationExecution.authenticator="idp-email-verification">
                            verified
                        <#elseif authenticationSelection.authenticationExecution.authenticator="idp-username-password-form">
                            login
                        </#if>
                    </span>
                    </div>
                </div>
            </#list>
            <input type="hidden" id="authexec-hidden-input" name="authenticationExecution" />
        </form>
    </#if>
</@layout.registrationLayout>
