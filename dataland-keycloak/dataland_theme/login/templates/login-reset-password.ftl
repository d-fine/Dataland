<#import "dataland_template.ftl" as layout>
<#import "components/input_field.ftl" as inputField>
<@layout.registrationLayout displayInfo=true displayMessage=!messagesPerField.existsError('username'); section>
    <#if section = "header">
        Reset password
    <#elseif section = "backUrl">${url.loginUrl}
    <#elseif section = "backName">TO LOGIN
    <#elseif section = "form">
        <form id="kc-reset-password-form" action="${url.loginAction}" method="post">
            <div class="text-left">
                <p>
                    Enter the email address you used to register to Dataland. We'll send you an email with a link to reset your password.
                </p>
            </div>
            <@inputField.dala
            fieldName="username"
            fieldErrorHandlers=["username"]
            fieldHeading="Enter your email address"
            tabindex="1"
            autofocus=true
            autocomplete="email"
            type="text"
            value=(login.username)!''
            />
            <input tabindex="2" class="p-button w-full cursor-pointer font-semibold p-login-button" name="register" type="submit" value="SEND"/>
        </form>
        <div class="mt-5">
            <span style="font-size:16px;">
                Remember the password?
                <Button
                        class="p-button uppercase font-semibold text-primary justify-content-center bg-white-alpha-10 ml-4 cursor-pointer"
                        name="login_button"
                        onclick="location.href='${url.loginUrl}'">
                        LOG IN
                </Button>
            </span>
        </div>
    </#if>
</@layout.registrationLayout>
