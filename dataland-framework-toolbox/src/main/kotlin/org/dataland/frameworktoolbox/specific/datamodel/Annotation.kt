package org.dataland.frameworktoolbox.specific.datamodel

/**
 * An In-Memory representation of a kotlin annotation
 * @param fullyQualifiedName the fully qualified identifier of the annotation
 * @param rawParameterSpec the parameters for the annotation (annotation will get compiled to @FQN(rawParameterSpec)
 */
data class Annotation(
    val fullyQualifiedName: String,
    val rawParameterSpec: String = "",
) {
    val shortenedQualifier: String
        get() = fullyQualifiedName.substringAfterLast(".")

    val imports: Set<String>
        get() = setOf(fullyQualifiedName)
}
