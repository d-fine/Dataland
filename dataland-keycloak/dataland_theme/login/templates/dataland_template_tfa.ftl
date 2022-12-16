<#macro registrationLayout displayInfo=false displayMessage=true displayRequiredFields=false>
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="robots" content="noindex, nofollow">
        <meta name="viewport" content="width=device-width,initial-scale=1">

        <link rel="stylesheet" href="${url.resourcesPath}/index.css"/>
    </head>


    <body class="p-tfa-body">
    <!-- Back button -->
    <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-totp-settings-form"
          method="post">
        <button type="submit"
                class="p-tfa-button-back cursor-pointer flex align-items-center absolute ml-3 mt-3 no-underline"
                id="cancelTOTPBtn" name="cancel-aia" value="true"/>
        <span class="material-icons text-primary">arrow_back_ios</span>
        <span class="ml-2 text-primary font-semibold d-letters">BACK</span>
        </button>
    </form>

    <!-- Main Content -->
    <div class="block ml-auto mr-auto pt-6">
        <h1 class="text-6xl"><#nested "header"></h1>
        <div>
            <#nested "form">
        </div>
    </div>
    </body>
    </html>
</#macro>