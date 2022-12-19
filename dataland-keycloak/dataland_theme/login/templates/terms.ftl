<#import "dataland_template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        Almost there
    <#elseif section = "backUrl">/
    <#elseif section = "backName">HOME
    <#elseif section = "form">
        <div class="mt-4">
            <p class="font-semibold">Please review and accept the Terms and Conditions and Privacy Policy before joining Dataland.</p>
        </div>

        <div class="text-left mt-4 flex align-items-center">
            <input id="accept_terms" name="accept_terms" type="checkbox" required>
            <label for="accept_terms" class="tex-sm ml-2">I agree with the <a class="text-primary" href="">Terms and Conditions</a></label>
        </div>
        <div id="accept_terms_error" class="text-left align-items-center checkbox-error mt-2" style="display: none;">
            <span class="material-icons-outlined ">error</span>
            <span aria-live="polite">
                Please accept the Terms and Conditions to continue
            </span>
        </div>

        <div class="text-left mt-4 flex align-items-center">
            <input id="accept_privacy" name="accept_privacy" type="checkbox" required>
            <label for="accept_privacy" class="tex-sm ml-2">I accept the <a class="text-primary" href="">Privacy Policy</a></label>
        </div>

        <div id="accept_privacy_error" class="text-left  align-items-center checkbox-error mt-2" style="display: none;">
            <span class="material-icons-outlined">error</span>
            <span aria-live="polite">
                Please accept the Privacy Policy to continue
            </span>
        </div>

        <form id="toc-form" class="form-actions" action="${url.loginAction}" method="POST">
            <input id="response" name="cancel" type="hidden" value="1"/>
        </form>

        <Button
                class="w-full p-button uppercase cursor-pointer mt-5 font-semibold p-login-button"
                id="accept_button"
                name="accept_button"
        >
            ACCEPT & CONTINUE
        </Button>

        <Button
            class="w-full p-button uppercase cursor-pointer mt-5 font-semibold bg-white-alpha-10 text-primary p-login-button"
            id="cancel_button"
            name="cancel_button"
            >
            CANCEL
        </Button>

        <script type="text/javascript" src="${url.resourcesPath}/terms.js"></script>
    </#if>
</@layout.registrationLayout>
