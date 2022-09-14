<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('totp'); section>
    <#if section="header">
        Mobile Authenticator
    <#elseif section="form">
        <form id="kc-otp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}"
              method="post">

            <#if otpLogin.userOtpCredentials?size gt 1>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcInputWrapperClass!}">
                        <#list otpLogin.userOtpCredentials as otpCredential>
                            <input id="kc-otp-credential-${otpCredential?index}" class="${properties.kcLoginOTPListInputClass!}" type="radio" name="selectedCredentialId" value="${otpCredential.id}" <#if otpCredential.id == otpLogin.selectedCredentialId>checked="checked"</#if>>
                            <label for="kc-otp-credential-${otpCredential?index}" class="${properties.kcLoginOTPListClass!}" tabindex="${otpCredential?index}">
                                <span class="${properties.kcLoginOTPListItemHeaderClass!}">
                                    <span class="${properties.kcLoginOTPListItemIconBodyClass!}">
                                      <i class="${properties.kcLoginOTPListItemIconClass!}" aria-hidden="true"></i>
                                    </span>
                                    <span class="${properties.kcLoginOTPListItemTitleClass!}">${otpCredential.userLabel}</span>
                                </span>
                            </label>
                        </#list>
                    </div>
                </div>
            </#if>

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!} text-left">
                    <label for="otp" class="${properties.kcLabelClass!} font-medium mb-2">${msg("loginOtpOneTime")}
                    </label>
                </div>

                <div class="${properties.kcInputWrapperClass!} mt-4">
                    <input id="otp" name="otp" autocomplete="off" type="text" placeholder="One time code"
                           class="${properties.kcInputClass!} p-tfa-input-grey-bottom-border" autofocus aria-invalid="<#if messagesPerField.existsError('totp')>true</#if>"/>

                    <#if messagesPerField.existsError('totp')>
                        <span id="input-error-otp-code" class="${properties.kcInputErrorMessageClass!} input-error"
                              aria-live="polite">
                        ${kcSanitize(messagesPerField.get('totp'))?no_esc}
                    </span>
                    </#if>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!} mt-4">
                    <input
                            class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!} p-button font-semibold cursor-pointer w-full"
                            name="login" id="kc-login" type="submit" value="SUBMIT" />
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>