package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.ComponentMarker
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.TreeNode
import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder

/**
 * A component is a higher-level abstraction for framework elements. Components are arranged in a hierarchy
 * and typically correspond to a single question in a questionnaire.
 * @param identifier the camelCase identifier of the component
 * @param parent the parent node in the hierarchy
 */
@ComponentMarker
open class ComponentBase(
    var identifier: String,
    override var parent: FieldNodeParent,
    var fullyQualifiedNameOfKotlinType: String = "",
) : TreeNode<FieldNodeParent> {

    /**
     * The label of a component is a human-readable short title describing the component
     */
    var label: String? = null

    /**
     * The explanation of a component is a longer description of the component. This variant will be displayed on the
     * upload page.
     */
    var uploadPageExplanation: String? = null

    /**
     * The explanation of a component is a longer description. If set, it will overwrite the explanation of the
     * upload page. If unset, it will default to the upload page explanation.
     */
    var viewPageExplanation: String? = null

    /**
     * The dataModelGenerator allows users to overwrite the DataClass generation of this specific component instance
     */
    var dataModelGenerator: ((dataClassBuilder: DataClassBuilder) -> Unit)? = null

    /**
     * The viewConfigGenerator allows users to overwrite the ViewConfig generation of this specific component instance
     */
    var viewConfigGenerator: ((sectionConfigBuilder: SectionConfigBuilder) -> Unit)? = null

    /**
     * The uploadConfigGenerator allows users to overwrite the UploadConfig generation of
     * this specific component instance
     */
    var uploadConfigGenerator: ((uploadCategoryBuilder: UploadCategoryBuilder) -> Unit)? = null

    /**
     * The fixtureGeneratorGenerator allows users to overwrite the FixtureGeneration generation
     * of this specific component isntance
     */
    var fixtureGeneratorGenerator: ((sectionBuilder: FixtureSectionBuilder) -> Unit)? = null

    /**
     * True iff this component is optional / accepts null values
     */
    var isNullable: Boolean = true

    /**
     * True iff this component is required (just a pointer to !isNullable for convenience)
     */
    var isRequired: Boolean
        get() = !isNullable
        set(value) { isNullable = !value }

    /**
     * A logical condition that decides whether this component is available / shown to users
     */
    var availableIf: FrameworkConditional = FrameworkConditional.AlwaysTrue

    /**
     * Specifies which kind of document-support (Datapoint-type) is desired for this component
     */
    var documentSupport: DocumentSupport = NoDocumentSupport

    /**
     * Obtain a list of all parents of this node until the root node
     */
    fun parents(): Sequence<FieldNodeParent> = sequence {
        var currentNode: Any? = parent
        while (currentNode is FieldNodeParent) {
            yield(currentNode)

            if (currentNode is TreeNode<*>) {
                currentNode = currentNode.parent
            } else {
                break
            }
        }
    }

    /**
     * Build this component instance into the provided Kotlin DataClass using the default
     * generator
     */
    open fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            identifier,
            documentSupport.getJvmTypeReference(
                TypeReference(fullyQualifiedNameOfKotlinType, isNullable),
                isNullable,
            ),
        )
    }

    /**
     * Build this component instance into the provided Kotlin DataClass
     */
    fun generateDataModel(dataClassBuilder: DataClassBuilder) {
        return dataModelGenerator?.let { it(dataClassBuilder) } ?: generateDefaultDataModel(dataClassBuilder)
    }

    /**
     * Build this component instance into the provided view-section configuration
     * using the default generator for this component
     */
    open fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        throw NotImplementedError("This component did not implement view config conversion.")
    }

    /**
     * Build this component instance into the provided upload-section configuration
     * using the default generator for this component
     */
    open fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        throw NotImplementedError("This component did not implement upload config conversion.")
    }

    /**
     * Build this component instance into the provided view-section configuration
     */
    fun generateViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        return viewConfigGenerator?.let { it(sectionConfigBuilder) } ?: generateDefaultViewConfig(sectionConfigBuilder)
    }

    /**
     * Build this component instance into the provided upload-section configuration
     */
    fun generateUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        return uploadConfigGenerator?.let { it(uploadCategoryBuilder) }
            ?: generateDefaultUploadConfig(uploadCategoryBuilder)
    }

    /**
     * Build the fixture code generation for this component using the default generator
     */
    open fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        throw NotImplementedError("This component did not implement fixture code-generation.")
    }

    /**
     * Build the fixture code generation for this component
     */
    fun generateFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        return fixtureGeneratorGenerator?.let { it(sectionBuilder) } ?: generateDefaultFixtureGenerator(sectionBuilder)
    }
}
