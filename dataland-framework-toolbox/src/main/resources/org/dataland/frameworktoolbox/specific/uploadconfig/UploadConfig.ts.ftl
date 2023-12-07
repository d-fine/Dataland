<#--import { type Category } from "@/utils/GenericFrameworkTypes";-->

<#--export const ${frameworkIdentifier}DataModel : Category[] = [<@mldtconfig uploadConfig/>];  Category[] incompatible-->
export const ${frameworkIdentifier}DataModel = [<@mldtconfig uploadConfig/>];

<#macro mldtconfig items>
    <@indent>
<#--        <#list items as element><#if element.isCell()><@mldtcell element/></#if><#if element.isSubcategory()><@mldtisSubcategory element/></#if><#if element.isSection()><@mldtsection element/></#if>-->
        <#list items as element><#if element.isSubcategory()><@mldtisSubcategory element/></#if><#if element.isSection()><@mldtsection element/></#if>
        </#list>
    </@indent>
</#macro>

<#macro mldtconfig2 items>
    <@indent>
        <#list items as element><#if element.isCell()><@mldtcell element/></#if>
        </#list>
    </@indent>
</#macro>

<#macro mldtsection sectionConfig>{
    name: "${sectionConfig.name?js_string}",
    label: "${sectionConfig.label?js_string}",
    color: " ", <#-- not necessary at this point -->
    showIf: <@frameworklambda sectionConfig.shouldDisplay/>,
    subcategories: [<@mldtconfig sectionConfig.children/>],
    },
</#macro>

<#macro mldtisSubcategory subcategoryConfig>{
    name: "${subcategoryConfig.name?js_string}",
    label: "${subcategoryConfig.label?js_string}",
    fields: [
    <#if subcategoryConfig.children??> <@mldtconfig2 subcategoryConfig.children/> </#if>
    ],
    },
</#macro>

<#macro mldtcell cellConfig>{
    name: "${cellConfig.name?js_string}",
    label: "${cellConfig.label?js_string}",
    <#if cellConfig.explanation??>description: "${cellConfig.explanation?js_string}",</#if>
    <#if cellConfig.options??>options: [
        <#list cellConfig.options?sequence as entry>
            {
                identifier: "${entry.identifier}",
                label: "${entry.identifier}",
            },
        </#list>
        ],
    </#if>
    unit: "<#if cellConfig.unit??>${cellConfig.unit?js_string}</#if>",
    uploadComponentName: "${cellConfig.uploadComponentName?js_string}",
    required: <#if cellConfig.required??>true<#else>false</#if>,
    showIf: <@frameworklambda cellConfig.shouldDisplay/>, <#--is this even needed for upload?-->
    validation: "",
    },
</#macro>

<#macro frameworklambda lambda>(<#if lambda.usesDataset>dataset: ${frameworkDataType}</#if>):${lambda.returnParameter} => ${lambda.lambdaBody}</#macro>

