<#import "dataland_template.ftl" as layout>
<#import "components/input_field.ftl" as inputField>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('totp'); section>
    <#if section="header">
        Two-factor authentication
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section="form">
        <form id="kc-otp-login-form" action="${url.loginAction}" method="post">
            <#if otpLogin.userOtpCredentials?size gt 1>
                <div class="mb-5">
                    <div class="${properties.kcLabelWrapperClass!} text-left mb-2">
                        <label for="otp" class="${properties.kcLabelClass!} font-medium">
                            <div class="mb-2"> Multiple devices for 2-Factor-Authentication have been registered with your account. </div>
                            Please choose the one device you wish to use now:
                        </label>
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <div class="${properties.kcInputWrapperClass!}">
                            <#list otpLogin.userOtpCredentials as otpCredential>
                                <div class="flex mb-2">
                                    <input id="kc-otp-credential-${otpCredential?index}"
                                           class="${properties.kcLoginOTPListInputClass!}" type="radio"
                                           name="selectedCredentialId" value="${otpCredential.id}"
                                           <#if otpCredential.id == otpLogin.selectedCredentialId>checked="checked"</#if>>
                                    <label for="kc-otp-credential-${otpCredential?index}"
                                           class="${properties.kcLoginOTPListClass!} ml-2"
                                           tabindex="${otpCredential?index}">
                                <span class="${properties.kcLoginOTPListItemHeaderClass!}">
                                    <span class="${properties.kcLoginOTPListItemIconBodyClass!}">
                                      <i class="${properties.kcLoginOTPListItemIconClass!}" aria-hidden="true"></i>
                                    </span>
                                    <span class="${properties.kcLoginOTPListItemTitleClass!}">${otpCredential.userLabel}</span>
                                </span>
                                    </label>
                                </div>
                            </#list>
                        </div>
                    </div>
                </div>
            </#if>

            <span class="font-medium mb-2">${msg("loginOtpOneTime")}</span>
            <@inputField.dala
            fieldName="otp"
            fieldErrorHandlers=["totp"]
            fieldHeading="One time code"
            tabindex="1"
            autofocus=true
            type="text"
            />

            <div id="kc-form-buttons">
                <input tabindex="2" class="p-button w-full cursor-pointer font-semibold p-login-button" name="login" id="kc-login" type="submit" value="SUBMITT"/>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
