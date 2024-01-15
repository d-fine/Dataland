package org.dataland.frameworktoolbox.intermediate

import org.dataland.frameworktoolbox.intermediate.group.TopLevelComponentGroup
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.FrameworkFixtureGeneratorBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.FrameworkUploadConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.FrameworkViewConfigBuilder

/**
 * A High-Level intermediate representation of a Dataland Framework
 * @param identifier the short identifier of the framework (e.g., lksg)
 */
class Framework(
    val identifier: String,
    val apiEndpoint: String,
    val label: String,
    val explanation: String,
) {
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

    /**
     * Generate a TypeScript ViewModel for this framework In-Memory.
     */
    fun generateViewModel(): FrameworkViewConfigBuilder {
        val frameworkViewConfigBuilder = FrameworkViewConfigBuilder(this)
        root.children.forEach {
            it.generateViewConfig(frameworkViewConfigBuilder.rootSectionConfigBuilder)
        }
        return frameworkViewConfigBuilder
    }

    /**
     * Generate a TypeScript UploadModel for this framework In-Memory.
     */
    fun generateUploadModel(): FrameworkUploadConfigBuilder {
        val frameworkUploadConfigBuilder = FrameworkUploadConfigBuilder(this)
        root.children.forEach {
            it.generateUploadConfig(frameworkUploadConfigBuilder.rootSectionConfigBuilder)
        }
        return frameworkUploadConfigBuilder
    }

    /**
     * Generate a FixtureGenerator for this framework In-Memory
     */
    fun generateFixtureGenerator(): FrameworkFixtureGeneratorBuilder {
        val frameworkFixtureGenerator = FrameworkFixtureGeneratorBuilder(this)
        root.children.forEach {
            it.generateFixtureGenerator(frameworkFixtureGenerator.rootSectionBuilder)
        }
        return frameworkFixtureGenerator
    }
}
