import { type ${frameworkDataType} } from "@clients/backend";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
export const ${frameworkIdentifier?cap_first}UploadConfig = [<@mldtconfig uploadConfig/>];

<#macro mldtsection sectionConfig>{
    name: "insertNameHere",
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