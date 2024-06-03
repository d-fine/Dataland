<#import "dataland_template.ftl" as layout>
<#import "components/input_field.ftl" as inputField>
<#import "components/social_login.ftl" as socialLogin>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('email','password','password-confirm'); section>
    <#if section = "header">
        Create an account
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <form id="kc-register-form" action="${url.registrationAction}" method="post">

            <@inputField.dala
            fieldName="email"
            fieldErrorHandlers=["email"]
            fieldHeading="*Enter your email address"
            tabindex="1"
            autofocus=true
            autocomplete="email"
            type="text"
            value=(register.formData.email!'')
            />

            <div style="display: flex">
                <div style="margin-right: 0.5rem">
                    <@inputField.dala
                    fieldName="firstName"
                    fieldErrorHandlers=[]
                    fieldHeading="First Name"
                    tabindex="2"
                    autofocus=false
                    type="text"
                    required=false
                    value=(register.formData.firstName!'')
                    />
                </div>
                <div style="margin-left: 0.5rem">
                    <@inputField.dala
                    fieldName="lastName"
                    fieldErrorHandlers=[]
                    fieldHeading="Last Name"
                    tabindex="3"
                    autofocus=false
                    type="text"
                    required=false
                    value=(register.formData.lastName!'')
                    />
                </div>
            </div>

            <@inputField.dala
                fieldName="password"
                fieldErrorHandlers=["password", "password-confirm"]
                fieldHeading="*Password"
                wrappingDivAttributes="data-role=\"password-primary\""
                tabindex="4"
                autofocus=false
                type="password"
                autocomplete="off"
            >
                <div class="bg-black-alpha-20 d-progressbar mt-2" id="password-strength-indicator">
                    <div class="d-progressbar-value flex d-password-strength-empty">
                    </div>
                </div>
            </@inputField.dala>

            <@inputField.dala
                fieldName="password-confirm"
                fieldErrorHandlers=["password", "password-confirm"]
                fieldHeading="*Repeat password"
                wrappingDivAttributes="data-role=\"password-confirm\""
                tabindex="5"
                autofocus=false
                type="password"
                autocomplete="off"
            />

            
            <div class="text-left mt-4 flex align-items-center">
                <input id="user.attributes.receive_updates" name="user.attributes.receive_updates" type="checkbox">
                <label for="user.attributes.receive_updates" class="tex-sm ml-2" tabindex="6">Sign me up for updates
                    (optional)</label>
            </div>
            <input class="p-button w-full cursor-pointer font-semibold mt-5 p-login-button" tabindex="7"
                   name="register" id="register_button" type="submit" value="CREATE AN ACCOUNT"/>
        </form>

        <@socialLogin.dala prefix="TEST DATALAND WITH"/>

        <div class="mt-5">
            <span>Already have an account?</span>
            <Button
                    class="p-button uppercase text-primary justify-content-center bg-white-alpha-10 cursor-pointer font-semibold mt-1"
                    name="login_button"
                    onclick="location.href='${url.loginUrl}'">
                LOGIN TO ACCOUNT
            </Button>
        </div>

        <script>
            let submit_button = document.getElementById("register_button");

            let first_name_field = document.getElementById("firstName");
            let last_name_field = document.getElementById("lastName");

            submit_button.addEventListener("click", function () {
                if(!first_name_field.value) first_name_field.value = "-";
                if(!last_name_field.value) last_name_field.value = "-";
            });
        </script>

        <script type="text/javascript" src="${url.resourcesPath}/passwordStrength.js"></script>
    </#if>
</@layout.registrationLayout>
