<#macro spacerRow>
    <tr>
        <td colspan="1" style="height: 20px">&nbsp;</td>
    </tr>
</#macro>

<#macro spacerRowTiny>
    <tr>
        <td colspan="1" style="font-size: 5px; height: 5px">&nbsp;</td>
    </tr>
</#macro>

<#macro spacerRowHorizontalLineTop>
    <tr>
        <td colspan="3" style="border-top:1px solid #e3e3e3; height: 20px">&nbsp;</td>
    </tr>
</#macro>

<#macro renderBoldTitle(title)>
    <tr>
        <td colspan="2" style="font-size: 28px; font-weight: bold">${title}</td>
    </tr>
</#macro>

<#macro renderStyledLink(url, linkText)>
    <tr>
        <td style="text-align: left; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
        <td style="background-color: #ff5c00; text-align: center; padding:0; margin:0; border: 0; height: 54px; width: 468px;">
            <a href="${url}" target="_blank" style="border: 0 none; line-height: 30px; color: #ffffff; font-size: 18px; width: 100%; display: block; text-decoration: none;">
                ${linkText}
            </a>
        </td>
        <td style="background-color: #ffffff; text-align: right; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
    </tr>
</#macro>
