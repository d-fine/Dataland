<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        Link Accounts?
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <div class="text-left">
            <p>
                ${kcSanitize(message.summary)?no_esc}
            </p>
        </div>
        <form action="${url.loginAction}" method="post">
            <div>
                <button type="submit" class="p-button w-full cursor-pointer font-semibold mt-5 p-login-button" name="submitAction" id="linkAccount" value="linkAccount">LINK ACCOUNTS</button>
            </div>
        </form>
        <div class="mt-2">
            <Button
                    class="p-button w-full uppercase text-primary justify-content-center bg-white-alpha-10 cursor-pointer font-semibold mt-1 p-login-button"
                    name="login_button"
                    onclick="location.href='${url.loginUrl}'">
                BACK TO LOGIN
            </Button>
        </div>
    </#if>
</@layout.registrationLayout>