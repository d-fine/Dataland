<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
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
        <form id="kc-select-credential-form" action="${url.loginAction}" method="post">
            <div class="text-left">
                <#list auth.authenticationSelections as authenticationSelection>
                    <div class="mt-3 cursor-pointer p-button w-full bg-white-alpha-10 text-color flex gap-3 align-items-center" onclick="fillAndSubmit('${authenticationSelection.authExecId}')">
                        <span class="material-icons text-primary">
                            <#if authenticationSelection.authenticationExecution.authenticator="idp-email-verification">
                                email
                            <#elseif authenticationSelection.authenticationExecution.authenticator="idp-username-password-form">
                                password
                            </#if>
                        </span>
                        <div>
                            <strong>${msg('${authenticationSelection.displayName}')}</strong><br>
                            <span>${msg('${authenticationSelection.helpText}')}</span>
                        </div>
                    </div>
                </#list>
                <input type="hidden" id="authexec-hidden-input" name="authenticationExecution" />
            </div>
        </form>
    </#if>
</@layout.registrationLayout>