<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        <div>
			<img src="${url.resourcesPath}/img/logo_dataland_long.svg" class="d-padding d-scaling">
		</div>
		<div>
			${msg("loginAccountTitle")}
		</div>
    <#elseif section = "form">
    <div id="kc-form">
		<div id="kc-form-wrapper">
			<#if realm.password>
				<form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
					<#if !usernameHidden??>
						<div class="${properties.kcFormGroupClass!}">
                        
							<div class="input-group">
								<input tabindex="1" id="username" class="${properties.kcInputClass!}" name="username" value="${(login.username!'')}"  type="text" required autofocus autocomplete="email"
									aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
								/>
								<span class="highlight"></span>
								<span class="bar"></span>
								<label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>

								<#if messagesPerField.existsError('username','password')>
									<span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
										${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
									</span>
								</#if>
							</div>
						</div>
					</#if>

					<div class="${properties.kcFormGroupClass!}">
						<div class="input-group">

							<input tabindex="2" id="password" class="${properties.kcInputClass!}" name="password" type="password" required autocomplete="off"
							aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
							/>
							<span class="highlight"></span>
							<span class="bar"></span>
							<label for="password" class="${properties.kcLabelClass!} control-label">${msg("password")}</label>

							<#if usernameHidden?? && messagesPerField.existsError('username','password')>
								<span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
									${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
								</span>
							</#if>
						</div>
					</div>

					<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
						<div id="kc-form-options">
							<#if realm.rememberMe && !usernameHidden??>
								<div class="checkbox">
									<label>
										<#if login.rememberMe??>
											<input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
										<#else>
											<input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> ${msg("rememberMe")}
										</#if>
									</label>
								</div>
							</#if>
							</div>
							<div class="${properties.kcFormOptionsWrapperClass!}  d-slink">
								<#if realm.resetPasswordAllowed>
									<span><a tabindex="5" class="d-text-primary" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
								</#if>
							</div>

					</div>

					<div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
						<input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
						<input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" style="width: 300px; margin:auto" name="login" id="kc-login" type="submit" value="${msg('doLogIn')}"/>
					</div>
				</form>
			</#if>
        </div>
    </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span style="font-size:16px;">${msg("noAccount")}  <Button label="Join" class="d-letters d-button uppercase p-button justify-content-center d-margin" style="background-color:white; color:#e67f3f; font-size:24;" name="join_dataland_button" onclick="location.href='${url.registrationUrl}'">
                                                ${msg("doRegisterLogin")}</Button></span>

                </div>
            </div>
        </#if>
    <#elseif section = "socialProviders" >
        <#if realm.password && social.providers??>
            <div id="kc-social-providers" class="${properties.kcFormSocialAccountSectionClass!}">
                <hr/ class="d-hidden">
				<div class="separator">
					<div class="line"></div>
						<h4>${msg("identity-provider-login-label")}</h4>
					<div class="line"></div>
				</div>

                <ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
                    <#list social.providers as p>
                        <a id="social-${p.alias}" class="${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>"
                                type="button" style="width: 300px;" href="${p.loginUrl}">
                            <#if p.iconClasses?has_content>
                                <i class="${properties.kcCommonLogoIdP!} ${p.iconClasses!}" aria-hidden="true"></i>
                                <span class="${properties.kcFormSocialAccountNameClass!} kc-social-icon-text">${msg("LoginSocialLinksMessage")} ${p.displayName!}</span>
                            <#else>
                                <span class="${properties.kcFormSocialAccountNameClass!}">${p.displayName!}</span>
                            </#if>
                        </a>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
