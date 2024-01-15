package org.dataland.frameworktoolbox.specific.datamodel

/**
 * An In-Memory representation of a kotlin annotation
 * @param fullyQualifiedName the fully qualified identifier of the annotation
 * @param rawParameterSpec the parameters for the annotation (annotation will get compiled to @FQN(rawParameterSpec)
 * @param applicationTargetPrefix a prefix applied before the annotation that specifies the target (e.g., field:)
 */
open class Annotation(
    val fullyQualifiedName: String,
    val rawParameterSpec: String = "",
    val applicationTargetPrefix: String? = null,
) {
    val shortenedQualifier: String
        get() = fullyQualifiedName.substringAfterLast(".")

    val imports: Set<String>
        get() = setOf(fullyQualifiedName)
}
