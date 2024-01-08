package org.dataland.frameworktoolbox.utils.typescript

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption

/**
* Generates a string containing typescript code for a mapping object. This mapping object contains technical names
* of select options as keys, and the respective literal names as values.
* @param options are the objects that contain info about the select options
* @returns a string containing the mapping object as typescript code
*/
fun generateTsCodeForSelectOptionsMappingObject(options: Set<SelectionOption>): String {
    val codeBuilder = StringBuilder()
    codeBuilder.append("const mappings = {\n")

    for (option in options) {
        val escapedLabel = option.label.replace("\"", "\\\"")
        codeBuilder.append("    ${option.identifier}: \"$escapedLabel\",\n")
    }

    codeBuilder.append("}\n")

    return codeBuilder.toString()
}

/**
 * This turns the options of a component into a formatted string as required by the UploadCongig.ts
 * This default behaviour is useful for cases where custom population of the option field is not required.
 * @param options: the options of the respective component
 */
fun generateTsCodeForOptionsOfSelectionFormFields(options: Set<SelectionOption>): String {
    val codeBuilder = StringBuilder()
    codeBuilder.append(" [\n")
    for (option in options) {
        codeBuilder.append("{ \n")
        codeBuilder.append("label: \"" + option.label + "\" ,\n")
        codeBuilder.append("value: \"" + option.identifier + "\" ,\n")
        codeBuilder.append("}, \n")
    }
    codeBuilder.append(" ]")
    return codeBuilder.toString()
}
