package org.dataland.frameworktoolbox.specific.datamodel.elements

import org.dataland.frameworktoolbox.utils.DatalandRepository

/**
 * A DataModelElement is part of a Kotlin DataModel hierarchy (e.g., DataClasses, Enums, ...)
 * They can be built and compiled into a Dataland repository
 */
sealed interface DataModelElement {
    val name: String
    val parentPackage: PackageBuilder?

    /**
     * Build this DataModelElement into the provided Dataland repository.
     */
    fun build(into: DatalandRepository)
}
