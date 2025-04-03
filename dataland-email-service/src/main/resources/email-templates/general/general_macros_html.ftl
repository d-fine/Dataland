<#macro if if then else><#if if>${then}<#else>${else}</#if></#macro>

<#macro spacerRow colspan="3">
    <tr>
        <td colspan="${colspan}" style="height: 20px; line-height: 20px; padding: 0">&nbsp;</td>
    </tr>
</#macro>

<#macro spacerRowSmall colspan="3">
    <tr>
        <td colspan="${colspan}" style="font-size: 10px; height: 10px; line-height: 10px; padding: 0">&nbsp;</td>
    </tr>
</#macro>

<#macro spacerRowTiny colspan="3">
    <tr>
        <td colspan="${colspan}" style="font-size: 5px; height: 5px; line-height: 5px; padding: 0">&nbsp;</td>
    </tr>
</#macro>

<#macro spacerRowHorizontalLine position="bottom" color="#e3e3e3" padding="0" width="520px">
    <tr>
        <td colspan="3" style="border-${position}:2px solid ${color}; height: 20px; width: ${width} ;padding: ${padding};">&nbsp;</td>
    </tr>
</#macro>

<#macro boldTitle(title)>
    <tr>
        <td colspan="2" style="font-size: 28px; font-weight: bold">${title}</td>
    </tr>
</#macro>

<#macro dataLabel(label)>
    <tr>
        <td colspan="3" style="height: 7px; padding: 0">${label}</td>
    </tr>
</#macro>

<#macro dataValue(value)>
    <tr>
        <td colspan="3" style="font-weight: bold; font-size:20px; padding: 0">${value}</td>
    </tr>
</#macro>

<#macro dataLabelCell(label padding="0 10px 0 0")>
    <td style="padding: ${padding}">${label}</td>
</#macro>

<#macro dataValueCell(value padding="0 10px 0 0")>
    <td style="vertical-align: top; font-weight: bold; font-size:20px; padding: ${padding}">${value}</td>
</#macro>


<#macro buttonLink(url, linkText)>
    <tr>
        <td style="text-align: left; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
        <td style="background-color: #ff5c00; border-radius: 30px; text-align: center; padding:0; margin:0; border: 0; height: 54px; width: 468px;">
            <a href="${url}" target="_blank" style="border: 0 none; line-height: 30px; color: #ffffff; font-size: 18px; width: 100%; display: block; text-decoration: none;">
                ${linkText}
            </a>
        </td>
        <td style="background-color: #ffffff; text-align: right; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
    </tr>
</#macro>

<#macro textLink(url, linkText)>
    <a href="${url}" target="_blank" style="border: 0 none; font-weight: bolder; font-size: 18px; color: #FF6813; text-decoration: none;">
        ${linkText} &#10132;
    </a>
</#macro>

<#macro linkWithIcon(url, linkText)>
    <td>
        <span style="color: #ffffff;">
            <a href="${url}" target="_blank" style="border: 0 none; font-weight: bolder; font-size: 16px; color: #ffffff; text-decoration: none;">
                ${linkText}
            </a>
            &nbsp;&nbsp;
            <span style="font-family: Calibri, sans-serif; font-weight: bold;">&#8599;</span>
        </span>
    </td>
</#macro>

<#macro notificationSetting(url)>
    <tr>
        <td colspan="3">
            This is an immediate notification. Future updates will be sent in a <b>weekly summary</b>.<br/>
            <@textLink url="${url}" linkText="Reactivate immediate notifications for your data request"/>
        </td>
    </tr>
</#macro>

<#macro howToProceed items>
    <tr>
        <td colspan="3" style="font-weight: bold;">How to proceed?</td>
    </tr>
    <@spacerRowTiny/>
    <tr>
        <td colspan="3" style="margin:0; padding: 0;">
            <ol style="line-height: 24px; font-size: 16px; margin:0; padding: 0 40px;">
                <#list items as item>
                    <li style="margin:0; padding: 0;">${item}</li>
                </#list>
            </ol>
        </td>
    </tr>
</#macro>

<#macro howToProceedDataRequest>
    <@howToProceed items=["Review the provided data on Dataland.",
                          "Resolve or reopen your data request."]/>
</#macro>

<#macro bulletItemWithOptionalLink(text, url="", linkText="")>
    <tr>
        <td style="vertical-align: top; padding: 0 10px; line-height:24px"><b>&bull;</b></td>
        <td>
            ${text}<br />
            <#if url?has_content && linkText?has_content>
                <@textLink url="${url}" linkText="${linkText}"/>
            </#if>
        </td>
    </tr>
</#macro>
