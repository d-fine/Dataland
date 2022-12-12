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

        <div class="text-left mt-4 flex align-items-center" style="display: none;">
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
                label="Accept"
                class="w-full p-button uppercase cursor-pointer mt-5 font-semibold"
                name="accept_button"
                onClick="checkAndContinue();"
        >
            ACCEPT & CONTINUE
        </Button>

        <Button
            label="CANCEL"
            class="w-full p-button uppercase cursor-pointer mt-5 font-semibold bg-white-alpha-10 text-primary"
            name="cancel"
            onclick="cancel();"
            >
            CANCEL
        </Button>

        <script type="text/javascript">
            function requireChecked(checkBoxId) {
                if (document.getElementById(checkBoxId).checked) {
                    document.getElementById(checkBoxId + "_error").style.display = "none";
                    return true;
                } else {
                    document.getElementById(checkBoxId + "_error").style.display = "flex";
                    return false;
                }
            }

            function checkAndContinue() {
                var termsOkay = requireChecked("accept_terms");
                var privacyOkay = requireChecked("accept_privacy");
                if (termsOkay && privacyOkay) {
                    document.getElementById("response").name = "accept";
                    document.getElementById("toc-form").submit();
                }
            }

            function cancel() {
                document.getElementById("response").name = "cancel";
                document.getElementById("toc-form").submit();
            }
        </script>
    </#if>
</@layout.registrationLayout>
