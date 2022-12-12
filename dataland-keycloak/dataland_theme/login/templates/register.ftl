<#import "dataland_template.ftl" as layout>
<#import "components/input_field.ftl" as inputField>
<#import "components/social_login.ftl" as socialLogin>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('email','username','password','password-confirm'); section>
    <#if section = "header">
        Create a preview account
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <form id="kc-register-form" action="${url.registrationAction}" method="post">
            <@inputField.dala
            fieldName="email"
            fieldErrorHandlers=["email"]
            fieldHeading="Enter your email address"
            tabindex="1"
            autofocus=true
            autocomplete="email"
            type="text"
            value=(register.formData.email!'')
            />

            <@inputField.dala
            fieldName="password"
            fieldErrorHandlers=["password", "password-confirm"]
            fieldHeading="Password"
            tabindex="2"
            autofocus=false
            type="password"
            autocomplete="off"
            />

            <@inputField.dala
            fieldName="password-confirm"
            fieldErrorHandlers=["password", "password-confirm"]
            fieldHeading="Repeat password"
            tabindex="3"
            autofocus=false
            type="password"
            autocomplete="off"
            />
            
            <div class="text-left mt-4 flex align-items-center">
                <input id="user.attributes.receive_updates" name="user.attributes.receive_updates" type="checkbox">
                <label for="user.attributes.receive_updates" class="tex-sm ml-2" tabindex="4">Sign me up for updates (optional)</label>
            </div>
            <input class="p-button w-full cursor-pointer font-semibold mt-5 p-login-button" tabindex="5" name="register" type="submit" value="CREATE A PREVIEW ACCOUNT"/>
        </form>

        <@socialLogin.dala prefix="TEST DATALAND WITH"/>

        <div class="mt-5">
            <span>Already have an account?</span>
            <Button
                    class="p-button uppercase text-primary justify-content-center bg-white-alpha-10 cursor-pointer font-semibold mt-1"
                    name="login_button"
                    onclick="location.href='${url.loginUrl}'">
                LOGIN TO PREVIEW ACCOUNT
            </Button>
        </div>
    </#if>
</@layout.registrationLayout>
