<@indent>

export const UploadConfiguration = [
    <#list uploadConfig as element>
        <#if element.isCell()>
            <@mldtcell element/>
        </#if>
        <#if element.isSection()>
            <@section element/>
        </#if>
    </#list> ]

    <#macro section sectionConfig>{
        type: "this should be coming from a section",
        label: "${sectionConfig.label?js_string}",
        },
    </#macro>
    <#macro mldtcell cellConfig>{
        type: "this should be coming from a cell",
        label: "${cellConfig.label?js_string}",
        }
    </#macro>


</@indent>