<#import "dataland_template.ftl" as layout>
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
            <div class="input-group mt-5 mb-5 text-left">
                <input
                        type="text"
                        id="username"
                        class="<#if messagesPerField.existsError('username')>error</#if>"
                        name="username"
                        autocomplete="email"
                        aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"
                        required
                />
                <label for="username">E-Mail</label>

                <#if messagesPerField.existsError('username')>
                    <span class="material-icons-outlined input-error-icon">error</span>
                    <span class="input-error" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('username'))?no_esc}
                    </span>
                </#if>
            </div>
            <input class="p-button w-full cursor-pointer font-semibold" name="register" type="submit" value="SEND"/>
        </form>
        <div class="mt-5">
            <span style="font-size:16px;">
                Remember the password?
                <Button
                        label="LOG IN"
                        class="p-button p-component uppercase text-primary justify-content-center bg-white-alpha-10 ml-4 cursor-pointer"
                        name="login_button"
                        onclick="location.href='${url.loginUrl}'">
                        LOG IN
                </Button>
            </span>
        </div>
    </#if>
</@layout.registrationLayout>
