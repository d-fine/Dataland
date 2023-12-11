package org.dataland.frameworktoolbox.utils.typescript

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption

fun generateTsCodeForSelectOptionsMappingObject(options: MutableSet<SelectionOption>): String {
    val codeBuilder = StringBuilder()
    codeBuilder.append("const mappings = {\n")

    for (option in options) {
        val escapedLabel = option.label.replace("\"", "\\\"")
        codeBuilder.append("    ${option.identifier}: \"$escapedLabel\",\n")
    }

    codeBuilder.append("}\n")

    return codeBuilder.toString()
}