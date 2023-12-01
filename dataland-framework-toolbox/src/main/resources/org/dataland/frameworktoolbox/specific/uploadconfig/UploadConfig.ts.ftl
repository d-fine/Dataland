<#--import { type Category } from "@/utils/GenericFrameworkTypes";-->

<#--export const ${frameworkIdentifier}DataModel : Category[] = [<@mldtconfig uploadConfig/>];  Category[] incompatible-->
export const ${frameworkIdentifier}DataModel = [<@mldtconfig uploadConfig/>];

<#macro mldtsection sectionConfig>{
    name: "${sectionConfig.name?js_string}",
    label: "${sectionConfig.label?js_string}",
    showIf: <@frameworklambda sectionConfig.shouldDisplay/>,
    subcategories: [<@mldtconfig sectionConfig.children/>],
    },
</#macro>

<#macro mldtcell cellConfig>{
    name: "cell",
    label: "${cellConfig.label?js_string}",
    <#if cellConfig.explanation??>description: "${cellConfig.explanation?js_string}",</#if>
    unit: "",
    component: "",
    required: "",
    showIf: <@frameworklambda cellConfig.shouldDisplay/>,
    validation: "",
    },
</#macro>

<#macro frameworklambda lambda>(<#if lambda.usesDataset>dataset: ${frameworkDataType}</#if>):${lambda.returnParameter} => ${lambda.lambdaBody}</#macro>

<#macro mldtconfig items>
    <@indent>
        <#list items as element><#if element.isCell()><@mldtcell element/></#if><#if element.isSection()><@mldtsection element/></#if>
        </#list>
    </@indent>
</#macro>