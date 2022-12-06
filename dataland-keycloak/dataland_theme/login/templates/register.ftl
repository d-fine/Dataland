<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('email','username','password','password-confirm'); section>
    <#if section = "header">
        Create a preview account
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <form id="kc-register-form" action="${url.registrationAction}" method="post">
            <div class="input-group mt-5 mb-5 text-left">
                <input
                    type="text"
                    id="email"
                    class="<#if messagesPerField.existsError('email')>error</#if>"
                    name="email"
                    value="${(register.formData.email!'')}"
                    autocomplete="email"
                    aria-invalid="<#if messagesPerField.existsError('email')>true</#if>"
                    required
                />
                <label for="email">E-Mail</label>

                <#if messagesPerField.existsError('email')>
                    <span class="material-icons-outlined input-error-icon">error</span>
                    <span class="input-error" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('email'))?no_esc}
                    </span>
                </#if>
            </div>

            <div class="input-group mt-5 mb-5 text-left">

                <input
                    type="password"
                    id="password"
                    class="<#if messagesPerField.existsError('password', 'password-confirm')>error</#if>"
                    name="password"
                    autocomplete="new-password"
                    aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"
                    required
                />
                <label for="password">Password</label>
                <#if messagesPerField.existsError('password')>
                    <span class="material-icons-outlined input-error-icon">error</span>
                    <span class="input-error" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('password'))?no_esc}
                    </span>
                </#if>
                <span id="password-security-hint"></span>
            </div>

            <div class="input-group mt-5 mb-5 text-left">
                <input
                        type="password"
                        id="password-confirm"
                        class="<#if messagesPerField.existsError('password-confirm')>error</#if>"
                        name="password-confirm"
                        aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"
                        required
                />
                <label for="password-confirm">Repeat password</label>
                <#if messagesPerField.existsError('password-confirm')>
                    <span class="material-icons-outlined input-error-icon">error</span>
                    <span class="input-error" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}
                    </span>
                </#if>
            </div>
            
            <div class="text-left mt-4 flex align-items-center">
                <input id="user.attributes.receive_updates" name="user.attributes.receive_updates" type="checkbox">
                <label for="user.attributes.receive_updates" class="tex-sm ml-2">Sign me up for updates (optional)</label>
            </div>
            <input class="p-button w-full cursor-pointer font-semibold mt-5" name="register" type="submit" value="CREATE A PREVIEW ACCOUNT"/>
        </form>

        <div id="kc-social-providers">
            <div class="d-separator">
                <div class="d-line"></div>
                <h4>OR</h4>
                <div class="d-line"></div>
            </div>

            <div>
                <#list social.providers as p>
                    <#if p.alias="linkedin">
                        <div id="social-linkedin">
                            <button
                                    label="Join"
                                    class="p-button cursor-pointer w-full p-button-linkedin uppercase flex justify-content-center align-items-center"
                                    onclick="location.href='${p.loginUrl}'"
                            >
                                <img src="${url.resourcesPath}/in-white-21.png" alt="LinkedIn Logo" class="mr-2 mt-1 mb-1"/>
                                TEST DATALAND WITH LINKEDIN
                            </button>
                        </div>
                    <#else>
                        <div id="social-${p.alias}">
                            <button
                                    label="Join"
                                    class="p-button cursor-pointer w-full bg-white text-primary uppercase mt-3"
                                    onclick="location.href='${p.loginUrl}'"
                            >
                                TEST DATALAND WITH ${p.displayName!}
                            </button>
                        </div>
                    </#if>
                </#list>
            </div>


        </div>
        <div class="mt-5">
            <span style="font-size:16px;">
                Already have an account?
                <Button
                        label="LOG IN"
                        class="p-button p-component uppercase text-primary justify-content-center bg-white-alpha-10 ml-4 cursor-pointer"
                        name="login_button"
                        onclick="location.href='${url.loginUrl}'">
                        LOG IN TO PREVIEW ACCOUNT
                </Button>
            </span>
        </div>
        <script type="module" crossorigin src="${url.resourcesPath}/passwordStrength.js"></script>
    </#if>
</@layout.registrationLayout>