<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('firstName','lastName','email','username','password','password-confirm'); section>
    <#if section = "header">
		<div>
			<img src="${url.resourcesPath}/img/logo_dataland_long.svg" class="d-padding d-scaling">
		</div>
		<div>
			${msg("registerTitle")}
		</div>	
    <#elseif section = "form">
        <form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.registrationAction}" method="post">
            

            <div class="${properties.kcFormGroupClass!}">
                <div class="input-group">
                    <input type="text" id="email" class="${properties.kcInputClass!}" name="email"
                           value="${(register.formData.email!'')}" autocomplete="email" type="text" required
                           aria-invalid="<#if messagesPerField.existsError('email')>true</#if>"
                    />
					<span class="highlight"></span>
					<span class="bar"></span>
					<label for="email" class="${properties.kcLabelClass!}">${msg("email")}</label>
                    <#if messagesPerField.existsError('email')>
                        <span id="input-error-email" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('email'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <#if !realm.registrationEmailAsUsername>
                <div class="${properties.kcFormGroupClass!}">
                   
                    <div class="input-group">
                        <input type="text" id="username" class="${properties.kcInputClass!}" name="username"
                               value="${(register.formData.username!'')}" autocomplete="username" type="text" required
                               aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"
                        />
						<span class="highlight"></span>
						<span class="bar"></span>
						<label for="username" class="${properties.kcLabelClass!}">${msg("username")}</label>
                        <#if messagesPerField.existsError('username')>
                            <span id="input-error-username" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.get('username'))?no_esc}
                            </span>
                        </#if>
                    </div>
                </div>
            </#if>

            <#if passwordRequired??>
                <div class="${properties.kcFormGroupClass!}">
                   
                    <div class="input-group">
                        <input type="password" id="password" class="${properties.kcInputClass!}" name="password"
                               autocomplete="new-password" type="password" required
                               aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"
                        />
						<span class="highlight"></span>
						<span class="bar"></span>
						<label for="password" class="${properties.kcLabelClass!}">${msg("password")}</label>
                        <#if messagesPerField.existsError('password')>
                            <span id="input-error-password" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.get('password'))?no_esc}
                            </span>
                        </#if>
                    </div>
                </div>

                <div class="${properties.kcFormGroupClass!}">
                    
                    <div class="input-group">
                        <input type="password" id="password-confirm" class="${properties.kcInputClass!}"
                               name="password-confirm" type="password" required
                               aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"
                        />
						<span class="highlight"></span>
						<span class="bar"></span>
						<label for="password-confirm"class="${properties.kcLabelClass!}">${msg("passwordConfirm")}</label>
                        <#if messagesPerField.existsError('password-confirm')>
                            <span id="input-error-password-confirm" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}
                            </span>
                        </#if>
                    </div>
                </div>
            </#if>

            <#if recaptchaRequired??>
                <div class="form-group">
                    <div class="${properties.kcInputWrapperClass!}">
                        <div class="g-recaptcha" data-size="compact" data-sitekey="${recaptchaSiteKey}"></div>
                    </div>
                </div>
            </#if>

            <div class="${properties.kcFormGroupClass!}">
                

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" style="width: 300px; margin:auto" name="join-button" type="submit" value="${msg("doRegister")}"/>
                </div>
				<div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        
						<span class="d-font-family" style="font-size:16px; padding-top:30px;">${msg("backToLoginPage")}  <Button type="button" label="Login" class="d-letters d-button uppercase p-button justify-content-center" style="background-color:white; color:#e67f3f; font-size:24; margin-bottom:5px;" name="back_to_login_button" onclick="location.href='${url.loginUrl}'">
                                                ${msg("doLogIn")}</Button></span>
                    </div>
					
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>