import { type Category } from "@/utils/GenericFrameworkTypes";
import { ${frameworkIdentifier?cap_first}Data } from "@clients/backend";
import {DropdownOption} from "@/utils/PremadeDropdownDatasets";
<#list uploadConfig as element><#if element.isSection()><@cats element/></#if></#list>
<#macro subcats items><#list items as element><#if element.isSubcategory()><@field element/></#if></#list></#macro>
<#macro loop items><#list items as element><#if element.isCell()><@get element/></#if></#list></#macro>
<#macro cats categoryConfig><@subcats categoryConfig.children/></#macro>
<#macro field subcategoryConfig><#if subcategoryConfig.children??><@loop subcategoryConfig.children/></#if></#macro>
<#macro get fieldConfig><#if fieldConfig.frameworkUploadOptions??>
    <#list fieldConfig.frameworkUploadOptions.imports?sequence as imp>${imp};
    </#list></#if></#macro>

export const ${frameworkIdentifier}DataModel = [<@loopCategories uploadConfig/>] as Category[];

<#macro loopCategories items>
    <@indent>
        <#list items as element><#if element.isSection()><@mldtCategory element/></#if>
        </#list>
    </@indent>
</#macro>

<#macro loopSubcategories items>
    <@indent>
        <#list items as element><#if element.isSubcategory()><@mldtisSubcategory element/></#if>
        </#list>
    </@indent>
</#macro>

<#macro loopFields items>
    <@indent>
        <#list items as element><#if element.isCell()><@mldtField element/></#if>
        </#list>
    </@indent>
</#macro>


<#macro mldtCategory categoryConfig>{
    name: "${categoryConfig.name?js_string}",
    label: "${categoryConfig.label?js_string}",
    color: " ", <#-- not necessary at this point -->
    showIf: <@frameworklambda categoryConfig.shouldDisplay/>,
    subcategories: [<@loopSubcategories categoryConfig.children/>],
    },
</#macro>

<#macro mldtisSubcategory subcategoryConfig>{
    name: "${subcategoryConfig.name?js_string}",
    label: "${subcategoryConfig.label?js_string}",
    fields: [
    <#if subcategoryConfig.children??> <@loopFields subcategoryConfig.children/> </#if>
    ],
    },
</#macro>

<#macro mldtField fieldConfig>{
    name: "${fieldConfig.name?js_string}",
    label: "${fieldConfig.label?js_string}",
    <#if fieldConfig.explanation??>description: "${fieldConfig.explanation?js_string}",</#if>
    options: <#if fieldConfig.frameworkUploadOptions??>${fieldConfig.frameworkUploadOptions.body}<#elseif fieldConfig.options??> [
        <#list fieldConfig.options?sequence as entry>
            {
                label: "${entry.label}",
                value: "${entry.identifier}",
            },
        </#list>
    ]<#else>""</#if>,
    unit: "<#if fieldConfig.unit??>${fieldConfig.unit?js_string}</#if>",
    component: "${fieldConfig.uploadComponentName?js_string}",
    required: <#if fieldConfig.required??>true<#else>false</#if>,
    showIf: <@frameworklambda fieldConfig.shouldDisplay/>, <#--is this even needed for upload?-->
    validation: <#if fieldConfig.required??>"required"<#else>""</#if>,
    },
</#macro>

<#macro frameworklambda lambda>(<#if lambda.usesDataset>dataset: ${frameworkDataType}</#if>):${lambda.returnParameter} => ${lambda.lambdaBody}</#macro>

