<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        Log In
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <div class="input-group mt-5 mb-5 text-left">
                <input
                    class="<#if messagesPerField.existsError('username')>error</#if>"
                    tabindex="1"
                    id="username"
                    name="username"
                    value="${(login.username!'')}"
                    type="text"
                    required
                    autofocus
                    autocomplete="email"
                    aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"
                />
                <label for="username">Email</label>

                <#if messagesPerField.existsError('username')>
                    <span class="material-icons-outlined input-error-icon">error</span>
                    <span class="input-error" aria-live="polite">
                        ${kcSanitize(messagesPerField.getFirstError('username'))?no_esc}
                    </span>
                </#if>
            </div>
            <div class="input-group mt-5 mb-4 text-left">
                <input
                    tabindex="2"
                    id="password"
                    name="password"
                    type="password"
                    class="<#if messagesPerField.existsError('password')>error</#if>"
                    required
                    autocomplete="off"
                    aria-invalid="<#if messagesPerField.existsError('password')>true</#if>"
                />
                <label for="password">Password</label>

                <#if messagesPerField.existsError('password')>
                    <span class="material-icons-outlined input-error-icon">error</span>
                    <span class="input-error" aria-live="polite">
                        ${kcSanitize(messagesPerField.getFirstError('password'))?no_esc}
                    </span>
                </#if>
            </div>

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
                <input tabindex="4" class="p-button w-full cursor-pointer font-semibold mt-5" name="login" id="kc-login" type="submit" value="LOG IN TO PREVIEW ACCOUNT"/>
            </div>
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
                                <img src="${url.resourcesPath}/img/in-white-21.png" alt="LinkedIn Logo" class="mr-2 mt-1 mb-1"/>
                                LOG IN WITH LINKEDIN
                            </button>
                        </div>
                    <#else>
                        <div id="social-${p.alias}">
                            <button
                                    label="Join"
                                    class="p-button cursor-pointer w-full bg-white text-primary uppercase mt-3"
                                    onclick="location.href='${p.loginUrl}'"
                            >
                                LOG IN WITH ${p.displayName!}
                            </button>
                        </div>
                    </#if>
                </#list>
            </div>


        </div>



        <div id="kc-registration-container" class="mt-5">
            <div id="kc-registration">
                <span style="font-size:16px;">
                    Don't have an account?
                    <Button
                            label="Join"
                            class="p-button p-component uppercase text-primary justify-content-center bg-white-alpha-10 cursor-pointer"
                            name="join_dataland_button"
                            onclick="location.href='${url.registrationUrl}'">
                            CREATE A PREVIEW ACCOUNT
                    </Button>
                </span>

            </div>
        </div>

    </#if>

</@layout.registrationLayout>
