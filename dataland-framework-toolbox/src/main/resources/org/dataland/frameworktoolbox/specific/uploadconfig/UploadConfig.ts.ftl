import { type Category } from "@/utils/GenericFrameworkTypes";

export const ${frameworkIdentifier}DataModel : Category[] = [<@loopCategories uploadConfig/>];

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
    <#if fieldConfig.options??>options: [
        <#list fieldConfig.options?sequence as entry>
            {
                label: "${entry.identifier}",
                value: "${entry.identifier}",
            },
        </#list>
        ],</#if>
    unit: "<#if fieldConfig.unit??>${fieldConfig.unit?js_string}</#if>",
    uploadComponentName: "${fieldConfig.uploadComponentName?js_string}",
    required: <#if fieldConfig.required??>true<#else>false</#if>,
    showIf: <@frameworklambda fieldConfig.shouldDisplay/>, <#--is this even needed for upload?-->
    validation: "",
    },
</#macro>

<#macro frameworklambda lambda>(<#if lambda.usesDataset>dataset: ${frameworkDataType}</#if>):${lambda.returnParameter} => ${lambda.lambdaBody}</#macro>

