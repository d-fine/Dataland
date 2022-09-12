<#macro registrationLayout displayInfo=false displayMessage=true displayRequiredFields=false>
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="robots" content="noindex, nofollow">
        <meta name="viewport" content="width=device-width,initial-scale=1">

        <title>DataLand Login</title>
        <link rel="apple-touch-icon" sizes="180x180" href="${url.resourcesPath}/img/apple-touch-icon.png">
        <link rel="icon" type="image/png" sizes="32x32" href="${url.resourcesPath}/img/favicon-32x32.png">
        <link rel="icon" type="image/png" sizes="16x16" href="${url.resourcesPath}/img/favicon-16x16.png">

        <link rel="stylesheet" href="${url.resourcesPath}/css/dist.css"/>
    </head>
    <body style="text-align: left; background: #F7F9FA">
    <!-- Back button -->


    <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-totp-settings-form"
          method="post">


        <button type="submit"
                class="p-tfa-back-button cursor-pointer flex align-items-center absolute ml-3 mt-3 no-underline"
                id="cancelTOTPBtn" name="cancel-aia" value="true"/>
        <span class="material-icons text-primary">arrow_back_ios</span>
        <span class="ml-2 text-primary font-semibold d-letters">BACK</span>
        </button>


    </form>


    <!-- Main Content -->
    <div class="block ml-auto mr-auto pt-6">
        <h1><#nested "header"></h1>
        <div>
            <#-- App-initiated actions should not see warning messages about the need to complete the action -->
            <#-- during login.                                                                               -->
            <#if displayMessage && message?has_content>
                <div class="alert-${message.type}">
                    <span>${kcSanitize(message.summary)?no_esc}</span>
                </div>
            </#if>

            <#nested "form">
        </div>
    </div>
    </body>
    </html>
</#macro>