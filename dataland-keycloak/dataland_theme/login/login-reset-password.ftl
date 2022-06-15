<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true displayMessage=!messagesPerField.existsError('username'); section>
    <#if section = "header">
        <div>
			<img src="${url.resourcesPath}/img/logo_dataland_long.svg" class="d-padding d-scaling">
		</div>
		<div>
			${msg("emailForgotTitle")}
		</div>
    <#elseif section = "form">
        <form id="kc-reset-password-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
               
                <div class="input-group">
                    <input type="text" id="username" name="username" class="${properties.kcInputClass!}" autofocus value="${(auth.attemptedUsername!'')}" type="password" required aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"/>
                    <span class="highlight"></span>
					<span class="bar"></span>
					<label for="username" class="${properties.kcLabelClass!} control-label"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
					<#if messagesPerField.existsError('username')>
                        <span id="input-error-username" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.get('username'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>
            <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}" style="margin-bottom:20px;">
                        <span><a class="d-text-primary" href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" style="width: 300px; margin:auto" type="submit" value="${msg("doSubmit")}"/>
                </div>
            </div>
        </form>
    <#elseif section = "info" >
        <#if realm.duplicateEmailsAllowed>
            <div class="d-margin">
			${msg("emailInstructionUsername")}
			</div>
        <#else>
		<div class="d-margin">
            ${msg("emailInstruction")}
			</div>
        </#if>
    </#if>
</@layout.registrationLayout>
