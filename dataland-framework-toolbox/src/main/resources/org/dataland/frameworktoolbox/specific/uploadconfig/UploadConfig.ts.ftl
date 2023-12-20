import { type Category } from "@/utils/GenericFrameworkTypes";
import { ${frameworkIdentifier?cap_first}Data } from "@clients/backend";
<#list uploadConfig as element><@cats element/></#list><#macro subcats items>
<#list items as element><@loopSubcats element/></#list>
</#macro><#macro loop items><#list items as element><@loopOptions element/></#list></#macro>
<#macro cats categoryConfig><@subcats categoryConfig.children/></#macro>
<#macro loopSubcats subcategoryConfig><#if subcategoryConfig.children??><@loop subcategoryConfig.children/></#if></#macro>
<#macro loopOptions fieldConfig><#if fieldConfig.frameworkUploadOptions?? && fieldConfig.frameworkUploadOptions.imports??><#list fieldConfig.frameworkUploadOptions.imports?sequence as imp>
${imp};
</#list></#if></#macro>

export const ${frameworkIdentifier}DataModel = [<@loopCategories uploadConfig/>] as Category[];

<#macro loopCategories items>
    <@indent>
        <#list items as element><@uploadConfigCategory element/>
        </#list>
    </@indent>
</#macro>
<#macro loopSubcategories items>
    <@indent>
        <#list items as element><@uploadConfigSubcategory element/>
        </#list>
    </@indent>
</#macro>
<#macro loopFields items>
    <@indent>
        <#list items as element><@uploadConfigCell element/>
        </#list>
    </@indent>
</#macro>
<#macro uploadConfigCategory categoryConfig>{
    name: "${categoryConfig.name?js_string}",
    label: "${categoryConfig.label?js_string}",
    color: "<#if categoryConfig.labelBadgeColor??>${categoryConfig.labelBadgeColor?c}</#if>",
    showIf: <@frameworklambda categoryConfig.shouldDisplay/>,
    subcategories: [<@loopSubcategories categoryConfig.children/>],
    },
</#macro>
<#macro uploadConfigSubcategory subcategoryConfig>{
    name: "${subcategoryConfig.name?js_string}",
    label: "${subcategoryConfig.label?js_string}",
    fields: [
    <#if subcategoryConfig.children??> <@loopFields subcategoryConfig.children/> </#if>
    ],
    },
</#macro>
<#macro uploadConfigCell fieldConfig>{
    name: "${fieldConfig.name?js_string}",
    label: "${fieldConfig.label?js_string}",
    <#if fieldConfig.explanation??>description: "${fieldConfig.explanation?js_string}",</#if>
    options: <#if fieldConfig.frameworkUploadOptions??>${fieldConfig.frameworkUploadOptions.body},<#else>"",</#if>
    unit: "<#if fieldConfig.unit??>${fieldConfig.unit?js_string}</#if>",
    component: "${fieldConfig.uploadComponentName?js_string}",
    required: ${fieldConfig.required?c},
    showIf: <@frameworklambda fieldConfig.shouldDisplay/>,
    validation: <#if fieldConfig.validation??>${fieldConfig.validation.body}<#elseif fieldConfig.required>"required"<#else>""</#if>,
    },
</#macro>
<#macro frameworklambda lambda>(<#if lambda.usesDataset>dataset: ${frameworkDataType}</#if>):${lambda.returnParameter} => ${lambda.lambdaBody}</#macro>
