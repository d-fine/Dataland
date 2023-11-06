package org.dataland.frameworktoolbox.specific.datamodel

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
    val imports: Set<String>
        get() = type.imports + annotations.flatMap { it.imports }
}
