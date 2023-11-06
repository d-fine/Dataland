package org.dataland.frameworktoolbox.intermediate

import org.dataland.frameworktoolbox.intermediate.group.TopLevelComponentGroup
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder

/**
 * A High-Level intermediate representation of a Dataland Framework
 * @param identifier the short identifier of the framework (e.g., lksg)
 */
class Framework(val identifier: String) {
    val root: TopLevelComponentGroup = TopLevelComponentGroup(this)

    /**
     * Generate a Kotlin DataModel for this framework In-Memory.
     */
    fun generateDataModel(): FrameworkDataModelBuilder {
        val frameworkDataModelBuilder = FrameworkDataModelBuilder(this)

        root.children.forEach {
            it.generateDataModel(frameworkDataModelBuilder.rootDataModelClass)
        }

        return frameworkDataModelBuilder
    }
}
