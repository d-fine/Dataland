<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout
    displayMessage=false
    formContainerStyle="width: 550px;"
    outerContainerStyle="max-width: 600px;"
    ;section>
    <#if section = "header">
        Link Accounts
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <p class="font-semibold">
            ${kcSanitize(message.summary)?no_esc}
        </p>
        <div class="d-primary-login-width ml-auto mr-auto">
            <form action="${url.loginAction}" method="post">
                <div>
                    <button type="submit" class="p-button w-full cursor-pointer font-semibold mt-5 p-login-button" name="submitAction" id="linkAccount" value="linkAccount">LINK ACCOUNTS</button>
                </div>
            </form>
            <div class="mt-5">
                Login using existing account?
                <Button
                        class="p-button uppercase font-semibold text-primary justify-content-center bg-white-alpha-10 ml-4 cursor-pointer"
                        name="login_button"
                        onclick="location.href='${url.loginRestartFlowUrl}'">
                        TO LOGIN
                </Button>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>
