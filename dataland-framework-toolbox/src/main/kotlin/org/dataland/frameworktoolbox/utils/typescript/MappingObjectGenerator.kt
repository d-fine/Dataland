package org.dataland.frameworktoolbox.utils.typescript

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption

/**
* Generates a string containing typescript code for a mapping object. This mapping object contains technical names
* of select options as keys, and the respective literal names as values.
* @param options are the objects that contain info about the select options
* @returns a string containing the mapping object as typescript code
*/
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
