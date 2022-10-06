<#macro emailLayout>
    <html>
    <style>
        p {
            font-family: IBM Plex Sans, Helvetica;
            font-weight: 500;
            font-size: 16px;
        }

        a {
            color: #e67f3f;
        }
    </style>
    <body>
    <img src="https://dataland.com/images/logos/logo_dataland_long.png">
    <div>
        <br>
        <br>
    </div>
    <#nested>
    </body>
    </html>
</#macro>