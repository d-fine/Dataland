package org.dataland.frameworktoolbox.specific.datamodel.elements

import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.ClassProperty
import org.dataland.frameworktoolbox.specific.datamodel.ImportFormatter
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import java.nio.file.Path
import javax.lang.model.SourceVersion
import kotlin.io.path.div

/**
 * A DataClassBuilder is an in-memory Representation of a Kotlin-DataClass that
 * supports code-generation.
 * @param name the name of the DataClass
 * @param parentPackage the package in which the DataClass resides
 * @param comment a JavaDoc comment for the class
 * @param properties a list of properties for the DataClass
 * @param annotations a list of annotations for the DataClass
 */
data class DataClassBuilder(
    override val name: String,
    override val parentPackage: PackageBuilder,
    val comment: String,
    val properties: MutableList<ClassProperty> = mutableListOf(),
    val annotations: MutableList<Annotation> = mutableListOf(),
) : DataModelElement {
    private val logger by LoggerDelegate()

    override val empty: Boolean
        get() = properties.isEmpty()

    override val allNullable: Boolean
        get() = properties.all { it.type.nullable }

    val fullyQualifiedName: String
        get() = parentPackage.fullyQualifiedName + "." + name

    val imports: Set<String>
        get() = (properties.flatMap { it.imports } + annotations.flatMap { it.imports }).toSet()

    /**
     * Create a type-reference for this DataClass
     * @param nullable true iff the reference should allow null values
     */
    fun getTypeReference(nullable: Boolean): TypeReference = TypeReference(fullyQualifiedName, nullable)

    /**
     * Add a new property to this DataClass
     * @param name the java-name of the property
     * @param type the type of the newly created property
     * @param annotations a list of annotations for the created property
     */
    fun addProperty(
        name: String,
        type: TypeReference,
        annotations: List<Annotation> = emptyList(),
    ): ClassProperty {
        val newProperty = ClassProperty(name, type, annotations)
        properties.add(newProperty)
        return newProperty
    }

    private fun getFreeMarkerContext(): Map<String, *> =
        mapOf(
            "package" to parentPackage.fullyQualifiedName,
            "className" to name,
            "properties" to properties,
            "annotations" to annotations,
            "imports" to imports.filter { it.contains(".") },
            "import_formatter" to ImportFormatter,
            "comment_lines" to comment.lines(),
        )

    override fun build(into: Path) {
        require(SourceVersion.isName(fullyQualifiedName)) {
            "The class-identifier '$fullyQualifiedName' is not a valid java identifier"
        }
        require(!name[0].isLowerCase()) { "The class-name '$name' does not start with an upper-case letter" }

        val classPath = into / "${fullyQualifiedName.replace(".", "/")}.kt"
        logger.trace("Building class '{}' into '{}'", fullyQualifiedName, classPath)

        val freemarkerTemplate =
            FreeMarker.configuration
                .getTemplate("/specific/datamodel/elements/DataClass.kt.ftl")

        val writer = FileWriter(classPath.toFile())
        freemarkerTemplate.process(getFreeMarkerContext(), writer)
        writer.close()
    }

    override fun toString(): String = "$name.kt:\n" + properties.joinToString("\n") { it.toString().prependIndent("  ") }
}
