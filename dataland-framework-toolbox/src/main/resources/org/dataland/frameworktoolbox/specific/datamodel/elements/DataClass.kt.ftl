package ${package}
<#if imports?size != 0>

<#list imports as import>
import ${import}
</#list>
</#if>

/**
<#list comment_lines as line> * ${line}
</#list>
 */
<#list annotations as annotation>@${annotation.shortenedQualifier}(${annotation.rawParameterSpec})
</#list>
data class ${className}(
<#list properties as property>
    val ${property.name}: ${property.type.shortenedQualifier},
</#list>
)
