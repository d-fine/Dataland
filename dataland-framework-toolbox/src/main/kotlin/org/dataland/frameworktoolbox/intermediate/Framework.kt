package org.dataland.frameworktoolbox.intermediate

import org.dataland.frameworktoolbox.intermediate.group.TopLevelComponentGroup
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.FrameworkFixtureGeneratorBuilder
import org.dataland.frameworktoolbox.specific.qamodel.FrameworkQaModelBuilder
import org.dataland.frameworktoolbox.specific.specification.FrameworkSpecificationBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.FrameworkUploadConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.FrameworkViewConfigBuilder
import org.dataland.frameworktoolbox.utils.DatalandRepository

/**
 * A High-Level intermediate representation of a Dataland Framework
 * @param identifier the short identifier of the framework (e.g., lksg)
 */
class Framework(
    val identifier: String,
    val label: String,
    val explanation: String,
    val order: Int,
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
     * Generate a specification for this framework In-Memory.
     */
    fun generateSpecifications(datalandRepository: DatalandRepository): FrameworkSpecificationBuilder {
        val frameworkDataModelBuilder = FrameworkSpecificationBuilder(this, datalandRepository)

        root.children.forEach {
            it.generateSpecification(frameworkDataModelBuilder.rootCategoryBuilder)
        }

        return frameworkDataModelBuilder
    }

    /**
     * Generate a Kotlin QA DataModel for this framework In-Memory.
     */
    fun generateQaModel(): FrameworkQaModelBuilder {
        val frameworkQaModelBuilder = FrameworkQaModelBuilder(this)

        root.children.forEach {
            it.generateQaModel(frameworkQaModelBuilder.rootDataModelClass)
        }

        return frameworkQaModelBuilder
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
