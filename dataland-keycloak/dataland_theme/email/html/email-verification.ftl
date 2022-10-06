
<#import "template.ftl" as layout>
<@layout.emailLayout>
    ${kcSanitize(msg("emailVerificationBodyHtml1"))?no_esc}
    <table role="presentation" style="border-collapse:separate;line-height:100%;">
        <tr>
            <td  role="presentation" style="border:none;border-radius:6px;cursor:auto;padding:11px 20px;background:#e67f3f;">
                <a href="${msg(link)}" style="background:#e67f3f;color:#ffffff;font-family:Helvetica, sans-serif;font-size:18px;font-weight:600;line-height:120%;Margin:0;text-decoration:none;text-transform:none;" target="_blank">
                    Activate your account!
                </a>
            </td>
        </tr>
    </table>
    <strong>${(msg("emailVerificationBodyHtml2", linkExpiration, linkExpirationFormatter(linkExpiration)))?no_esc}</strong>
</@layout.emailLayout>


