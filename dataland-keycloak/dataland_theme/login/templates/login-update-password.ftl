<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        Reset password
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">

        <div>
            <span>Enter a new password for your Dataland account.</span>
        </div>

        <form id="kc-passwd-update-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">

            <input type="text" id="username" name="username" value="${username}" autocomplete="username"
                   readonly="readonly" style="display:none;"/>
            <input type="password" id="password" name="password" autocomplete="current-password" style="display:none;"/>


            <div class="${properties.kcFormGroupClass!} pt-4">
                <div class="${properties.kcInputWrapperClass!} p-relative-position">
                    <input type="password" id="password-new" name="password-new"
                           required autofocus autocomplete="new-password"
                           aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"
                    />
                    <label for="username">New password</label>

                    <#if messagesPerField.existsError('password')>
                        <span id="input-error-password" class="${properties.kcInputErrorMessageClass!} input-error" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('password'))?no_esc}
                        </span>
                    </#if>

                </div>
            </div>


            <div class="${properties.kcFormGroupClass!} pt-4">
                <div class="${properties.kcInputWrapperClass!} p-relative-position">
                    <input type="password" id="password-confirm" name="password-confirm"
                           required autocomplete="new-password"
                           aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"
                    />
                    <label for="username">Repeat password</label>

                    <#if messagesPerField.existsError('password-confirm')>
                        <span id="input-error-password-confirm" class="${properties.kcInputErrorMessageClass!} input-error" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}
                        </span>
                    </#if>

                </div>
            </div>


            <div class="${properties.kcFormGroupClass!} pt-4">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <#if isAppInitiatedAction??>
                            <div class="checkbox">
                                <label><input type="checkbox" id="logout-sessions" name="logout-sessions" value="on" checked> ${msg("logoutOtherSessions")}</label>
                            </div>
                        </#if>
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <#if isAppInitiatedAction??>
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}
                        p-button font-semibold cursor-pointer uppercase w-full" type="submit" value="Reset password" />
                        <button class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}
                        p-button font-semibold cursor-pointer bg-white text-primary uppercase w-full" type="submit" name="cancel-aia" value="true" />${msg("doCancel")}</button>
                    <#else>
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}
                        p-button font-semibold cursor-pointer uppercase w-full" type="submit" value="Reset password" />
                    </#if>
                </div>
            </div>


        </form>
    </#if>
</@layout.registrationLayout>