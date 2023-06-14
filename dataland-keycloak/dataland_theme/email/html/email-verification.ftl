
<#import "template.ftl" as layout>
<@layout.emailLayout>
    <p>Someone has created a Dataland preview account with this email address. If this was you, click the link below to verify your email address</p>
    <table role="presentation" style="border-collapse:separate;line-height:100%;">
        <tr>
            <td  role="presentation" style="border:none;border-radius:6px;cursor:auto;padding:11px 20px;background:#e67f3f;">
                <a href="${msg(link)}" style="background:#e67f3f;color:#ffffff;font-family:Helvetica, sans-serif;font-weight:600;line-height:120%;Margin:0;text-decoration:none;text-transform:none;" target="_blank">
                    Activate your preview account!
                </a>
            </td>
        </tr>
    </table>

    <p>Or copy and paste the URL into your browser.</p>
    <a href="${msg(link)}" style="color: #e67f3f;"> ${msg(link)} </a>
    <br>
    <br>
    <br>
    <br>
    <p>If you didn't create this preview account, just ignore this message.</p>


</@layout.emailLayout>


