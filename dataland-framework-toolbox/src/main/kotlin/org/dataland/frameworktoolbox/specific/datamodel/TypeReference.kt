package org.dataland.frameworktoolbox.specific.datamodel

import kotlin.collections.plus

/**
 * A reference to a Java/Kotlin DataType
 * @param fullyQualifiedName the fully qualified JVM name of the target class
 * @param nullable true iff the type is nullable
 * @param genericTypeParameters an optional list of generic type parameters
 */
data class TypeReference(
    val fullyQualifiedName: String,
    val nullable: Boolean,
    val genericTypeParameters: List<TypeReference>? = null,
) {
    val imports: Set<String>
        get() = setOf(fullyQualifiedName) + (genericTypeParameters?.flatMap { it.imports } ?: emptySet())

    val name: String =
        fullyQualifiedName.substringAfterLast(".")

    val shortenedQualifier: String
        get() {
            val optionalSuffix = if (nullable) "?" else ""
            val genericParameterSuffix = if (genericTypeParameters?.isNotEmpty() == true) {
                "<${genericTypeParameters.joinToString(", ") { it.shortenedQualifier }}>"
            } else {
                ""
            }
            return "${fullyQualifiedName.substringAfterLast(".")}$genericParameterSuffix$optionalSuffix"
        }

    override fun toString(): String {
        val optionalSuffix = if (nullable) "?" else ""
        val genericParameterSuffix = if (genericTypeParameters?.isNotEmpty() == true) {
            "<${genericTypeParameters.joinToString(", ") { it.toString() }}>"
        } else {
            ""
        }

        return "$fullyQualifiedName$genericParameterSuffix$optionalSuffix"
    }
}
