package org.dataland.frameworktoolbox.specific.datamodel.elements

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import java.nio.file.Path
import javax.lang.model.SourceVersion
import kotlin.io.path.div

/**
 * A EnumBuilder is an in-memory Representation of a Kotlin-Enum that
 * supports code-generation.
 * @param name the name of the enum
 * @param parentPackage the package in which the enum resides
 * @param comment a JavaDoc comment for the enum
 * @param options the possible values the enum can take
 */
data class EnumBuilder(
    override val name: String,
    override val parentPackage: PackageBuilder,
    val comment: String,
    val options: Set<SelectionOption>,
) : DataModelElement {

    private val logger by LoggerDelegate()

    override val empty: Boolean
        get() = options.isEmpty()

    override val allNullable: Boolean
        get() = true

    val fullyQualifiedName: String
        get() = parentPackage.fullyQualifiedName + "." + name

    /**
     * Create a type-reference for this enum
     * @param nullable true iff the reference should allow null values
     */
    fun getTypeReference(nullable: Boolean): TypeReference {
        return TypeReference(fullyQualifiedName, nullable)
    }

    private fun getFreeMarkerContext(): Map<String, *> {
        return mapOf(
            "package" to parentPackage.fullyQualifiedName,
            "enumName" to name,
            "options" to options,
            "comment_lines" to comment.lines(),
        )
    }

    override fun build(into: Path) {
        require(SourceVersion.isName(fullyQualifiedName)) {
            "The enum-identifier '$fullyQualifiedName' is not a valid java identifier"
        }

        options.forEach {
            require(SourceVersion.isName(it.identifier)) {
                "The enum-option '$it' is not a valid java identifier"
            }
        }

        require(!name[0].isLowerCase()) { "The enum-name '$name' does not start with an upper-case letter" }

        val classPath = into / "${fullyQualifiedName.replace(".", "/")}.kt"
        logger.trace("Building enum '{}' into '{}'", fullyQualifiedName, classPath)

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/datamodel/elements/Enum.kt.ftl")

        val writer = FileWriter(classPath.toFile())
        freemarkerTemplate.process(getFreeMarkerContext(), writer)
        writer.close()
    }

    override fun toString(): String {
        return "$name.kt: enum of $options"
    }
}
