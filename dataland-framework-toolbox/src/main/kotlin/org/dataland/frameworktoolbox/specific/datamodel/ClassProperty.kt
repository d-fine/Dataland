package org.dataland.frameworktoolbox.specific.datamodel

import javax.lang.model.SourceVersion

/**
 * An In-Memory representation of a single property of a Kotlin class
 * @param name the name of the property
 * @param type the type of the property
 * @param annotations a list of annotations applied to the property
 */
data class ClassProperty(
    val name: String,
    val type: TypeReference,
    val annotations: List<Annotation>,
) {
    init {
        require(SourceVersion.isIdentifier(name)) {
            "The property-name '$name' is not a valid java identifier"
        }
        require(name.length >= 2 && name[0].isLowerCase() && name[1].isLowerCase()) {
            "The first two letters of the property name '$name' are not lower-case. This typically results in weired " +
                "behaviour as Kotlin + Jackson + Swagger do not align in this case. " +
                "(See https://github.com/FasterXML/jackson-module-kotlin/issues/92). "
        }
    }

    val imports: Set<String>
        get() = type.imports + annotations.flatMap { it.imports }
}
