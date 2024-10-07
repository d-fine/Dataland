<#macro dataFixtureSection sectionConfig>{
    <#list sectionConfig.elements as element>
        <@indent>${element.identifier}: <#if element.isSection()><@dataFixtureSection element/><#elseif element.isAtomicExpression()>${element.typescriptExpression}</#if></@indent>,
    </#list>
}</#macro>