<#macro emailLayout>
    <html>
    <style>
        p {
            font-family: IBM Plex Sans, Helvetica;
            font-weight: 500;
        }
        p > a {
            font-family: Arial;
            font-weight: 100;
            color: white;
        }
    </style>
    <body>
    <img src="https://dataland-local.duckdns.org/images/logos/dataland-logo-lr.png">
    <#nested>
    </body>
    </html>
</#macro>