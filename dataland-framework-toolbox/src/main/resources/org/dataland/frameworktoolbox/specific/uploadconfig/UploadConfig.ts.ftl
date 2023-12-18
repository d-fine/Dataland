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
    <#if fieldConfig.options??>options: [
        <#list fieldConfig.options?sequence as entry>
            {
                label: "${entry.label}",
                value: "${entry.identifier}",
            },
        </#list>
        ],</#if>
    unit: "<#if fieldConfig.unit??>${fieldConfig.unit?js_string}</#if>",
    component: "${fieldConfig.uploadComponentName?js_string}",
    required: ${fieldConfig.required?c},
    showIf: <@frameworklambda fieldConfig.shouldDisplay/>,
    validation: <#if fieldConfig.required>"required"<#else>""</#if>,
    },
</#macro>
<#macro frameworklambda lambda>(<#if lambda.usesDataset>dataset: ${frameworkDataType}</#if>):${lambda.returnParameter} => ${lambda.lambdaBody}</#macro>
import { type Category } from "@/utils/GenericFrameworkTypes";
import { ${frameworkIdentifier?cap_first}Data } from "@clients/backend";

export const ${frameworkIdentifier}DataModel = [<@loopCategories uploadConfig/>] as Category[];
