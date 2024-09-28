package org.dataland.frameworktoolbox.specific.datamodel.elements

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import java.nio.file.Path
import javax.lang.model.SourceVersion
import kotlin.io.path.div

/**
 * A PackageBuilder is an in-memory Representation of a Kotlin package that
 * supports code-generation.
 * @param name the name of the package
 * @param parentPackage the package in which this package resides (null iff top-level package)
 * @param childElements a list of contained elements
 */
data class PackageBuilder(
    override val name: String,
    override val parentPackage: PackageBuilder?,
    val childElements: MutableList<DataModelElement> = mutableListOf(),
) : DataModelElement {
    private val logger by LoggerDelegate()

    override val empty: Boolean
        get() = childElements.all { it.empty }

    override val allNullable: Boolean
        get() = childElements.all { it.allNullable }

    val fullyQualifiedName: String
        get() = (parentPackage?.fullyQualifiedName?.plus(".") ?: "") + name

    /**
     * Add a new DataClass to the package
     * @param name the name of the datalcass
     * @param comment the comment
     */
    fun addClass(
        name: String,
        comment: String,
        annotations: MutableList<Annotation> = mutableListOf(),
    ): DataClassBuilder {
        val newDataClass =
            DataClassBuilder(
                name = name,
                parentPackage = this,
                comment = comment,
                annotations = annotations,
            )
        childElements.add(newDataClass)
        return newDataClass
    }

    /**
     * Add a new package to the package
     * @param name the name of the package
     */
    fun addPackage(name: String): PackageBuilder {
        val newPackage =
            PackageBuilder(
                name = name,
                parentPackage = this,
            )
        childElements.add(newPackage)
        return newPackage
    }

    /**
     * Add a new enum to the package
     * @param name the name of the package
     */
    fun addEnum(
        name: String,
        options: Set<SelectionOption>,
        comment: String,
    ): EnumBuilder {
        val newEnum =
            EnumBuilder(
                name = name,
                parentPackage = this,
                options = options,
                comment = comment,
            )
        childElements.add(newEnum)
        return newEnum
    }

    override fun toString(): String = "$name/\n" + childElements.joinToString("\n") { it.toString().prependIndent("  ") }

    override fun build(into: Path) {
        require(SourceVersion.isName(fullyQualifiedName)) {
            "The package path '$fullyQualifiedName' is not a valid java identifier"
        }

        val packagePath = into / fullyQualifiedName.replace(".", "/")

        logger.trace("Building package '{}' into '{}'", fullyQualifiedName, packagePath)

        val packagePathFile = packagePath.toFile()
        packagePathFile.deleteRecursively()
        packagePathFile.mkdirs()

        childElements.forEach { it.build(into) }
    }
}
