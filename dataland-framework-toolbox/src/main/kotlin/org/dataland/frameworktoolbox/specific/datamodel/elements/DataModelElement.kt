package org.dataland.frameworktoolbox.specific.datamodel.elements

import java.nio.file.Path

/**
 * A DataModelElement is part of a Kotlin DataModel hierarchy (e.g., DataClasses, Enums, ...)
 * They can be built and compiled into a Dataland repository
 */
sealed interface DataModelElement {
    val name: String
    val parentPackage: PackageBuilder?

    val empty: Boolean
    val allNullable: Boolean

    /**
     * Build this DataModelElement into the provided Dataland repository.
     */
    fun build(into: Path)
}
