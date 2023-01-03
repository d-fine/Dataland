<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        Link Accounts
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <div>
            <p class="font-semibold">
                ${kcSanitize(message.summary)?no_esc}
            </p>
        </div>
        <form action="${url.loginAction}" method="post">
            <div>
                <button type="submit" class="p-button w-full cursor-pointer font-semibold mt-5 p-login-button" name="submitAction" id="linkAccount" value="linkAccount">LINK ACCOUNTS</button>
            </div>
        </form>
        <div class="mt-5">
            <span style="font-size:16px;">
                Login using existing account?
                <Button
                        class="p-button uppercase font-semibold text-primary justify-content-center bg-white-alpha-10 ml-4 cursor-pointer"
                        name="login_button"
                        onclick="location.href='${url.loginRestartFlowUrl}'">
                        TO LOGIN
                </Button>
            </span>
        </div>
    </#if>
</@layout.registrationLayout>
