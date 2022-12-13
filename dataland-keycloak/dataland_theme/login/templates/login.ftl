<#import "dataland_template.ftl" as layout>
<#import "components/input_field.ftl" as inputField>
<#import "components/social_login.ftl" as socialLogin>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        Login
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <@inputField.dala
                fieldName="username"
                fieldErrorHandlers=["username", "password"]
                fieldHeading="Enter your email address"
                tabindex="1"
                autofocus=true
                autocomplete="email"
                type="text"
                value=(login.username)!''
              />
            <@inputField.dala
                fieldName="password"
                fieldErrorHandlers=["username", "password"]
                fieldHeading="Password"
                tabindex="2"
                autofocus=false
                type="password"
                autocomplete="off"
            />

            <div class="text-left">
                <a tabindex="5" class="text-primary" href="${url.loginResetCredentialsUrl}">Forgot password?</a>
            </div>

            <div class="text-left mt-4 flex align-items-center">
                <#if login.rememberMe??>
                    <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checkex>
                <#else>
                    <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox">
                </#if>
                <label for="rememberMe" class="tex-sm ml-2">Remember me</label>
            </div>

            <div id="kc-form-buttons">
                <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                <input tabindex="4" class="p-button w-full cursor-pointer font-semibold mt-5 p-login-button" name="login" id="kc-login" type="submit" value="LOGIN TO PREVIEW ACCOUNT"/>
            </div>
        </form>

        <@socialLogin.dala prefix="LOGIN WITH"/>

        <div id="kc-registration-container" class="mt-5">
            <div id="kc-registration" class="flex-box">
                    <span>Don't have an account?</span>
                    <Button
                            class="p-button uppercase text-primary justify-content-center bg-white-alpha-10 cursor-pointer font-semibold mt-1"
                            name="join_dataland_button"
                            onclick="location.href='${url.registrationUrl}'">
                            CREATE A PREVIEW ACCOUNT
                    </Button>
            </div>
        </div>

    </#if>

</@layout.registrationLayout>
