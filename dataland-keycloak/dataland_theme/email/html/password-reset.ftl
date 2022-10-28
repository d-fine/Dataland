<#import "template.ftl" as layout>
<@layout.emailLayout>

    <p>Hi,<br>
        <br>
        Forgot your password?<br>
        We received a request to reset the password for your preview account.<br>
        <br>
        To reset your password, click on the button below. This link is valid
        for ${msg(linkExpirationFormatter(linkExpiration))}.
    </p>

    <table role="presentation" style="border-collapse:separate;line-height:100%;">
        <tr>
            <td role="presentation"
                style="border:none;border-radius:6px;cursor:auto;padding:11px 20px;background:#e67f3f;">
                <a href="${msg(link)}"
                   style="background:#e67f3f;color:#ffffff;font-family:Helvetica, sans-serif;font-weight:600;line-height:120%;Margin:0;text-decoration:none;text-transform:none;"
                   target="_blank">
                    RESET PASSWORD
                </a>
            </td>
        </tr>
    </table>

    <p>Or copy and paste the URL into your browser.</p>
    <a href="${msg(link)}"> ${msg(link)} </a>
    <br>
    <br>
    <br>
    <br>
    <p>If you didn't request to reset your password, just ignore or delete this message.</p>


</@layout.emailLayout>