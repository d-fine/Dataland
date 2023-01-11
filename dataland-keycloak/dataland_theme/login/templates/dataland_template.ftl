<#macro registrationLayout formContainerStyle="width: 364px;" outerContainerStyle="max-width: 500px;" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="robots" content="noindex, nofollow">
        <meta name="viewport" content="width=device-width,initial-scale=1">

        <title>Dataland Login</title>
        <link rel="apple-touch-icon" sizes="180x180" href="${url.resourcesPath}/apple-touch-icon.png">
        <link rel="icon" type="image/png" sizes="32x32" href="${url.resourcesPath}/favicon-32x32.png">
        <link rel="icon" type="image/png" sizes="16x16" href="${url.resourcesPath}/favicon-16x16.png">

        <link rel="stylesheet" href="${url.resourcesPath}/index.css"/>
    </head>
    <body>
        <!-- Back button -->

        <a href="<#compress><#nested "backUrl"></#compress>" class="cursor-pointer flex align-items-center absolute ml-3 mt-3 no-underline" id="back_button">
            <span class="material-icons text-primary">arrow_back_ios</span>
            <span class="text-primary font-semibold d-letters"><#compress><#nested "backName"></#compress></span>
        </a>



        <!-- Main Content -->
        <div class="block ml-auto mr-auto pt-8" style="${outerContainerStyle}">
            <img src="${url.resourcesPath}/logo_dataland_long.svg" class="d-dataland-logo">
            <h1 class="text-6xl"><#compress><#nested "header"></#compress></h1>
            <div class="ml-auto mr-auto" style="${formContainerStyle}">
                <#-- App-initiated actions should not see warning messages about the need to complete the action -->
                <#-- during login. -->
                <#if displayMessage && message?has_content>
                    <#if message.type="success">
                        <div class="d-success-modal d-modal-base">
                            <span class="material-icons">check_circle</span>
                            <span>${kcSanitize(message.summary)?no_esc}</span>
                        </div>
                    <#elseif message.type="error">
                        <div class="d-error-modal d-modal-base">
                            <span class="material-icons">error_outline</span>
                            <span>${kcSanitize(message.summary)?no_esc}</span>
                        </div>
                    <#else>
                        <div class="alert-${message.type}">
                            <span>${kcSanitize(message.summary)?no_esc}</span>
                        </div>
                    </#if>

                </#if>

                <#nested "form">

                <#if auth?has_content && auth.showTryAnotherWayLink()>
                    <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                        <div class="text-left d-primary-login-width ml-auto mr-auto">
                            <input type="hidden" name="tryAnotherWay" value="on"/>
                            <Button
                                    class="p-button uppercase w-full text-primary justify-content-center bg-white-alpha-10 cursor-pointer font-semibold mt-2 p-login-button"
                                    name="join_dataland_button"
                                    onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">
                                Authenticate in a different way
                            </Button>
                        </div>
                    </form>
                </#if>
            </div>
        </div>
    </body>
</html>
</#macro>