package ${package}

import io.swagger.v3.oas.annotations.media.Schema

/**
<#list comment_lines as line> * ${line}
</#list>
 */
@Schema(
    enumAsRef = true,
)
enum class ${enumName} {
<#list options as option>
    ${option.identifier},
</#list>
}
