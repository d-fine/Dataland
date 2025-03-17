package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.ComponentMarker
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.TreeNode
import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.TopLevelComponentGroup
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getKotlinFieldAccessor
import org.dataland.frameworktoolbox.utils.Naming
import org.dataland.frameworktoolbox.utils.capitalizeEn

/**
 * A component is a higher-level abstraction for framework elements. Components are arranged in a hierarchy
 * and typically correspond to a single question in a questionnaire.
 * @param identifier the camelCase identifier of the component
 * @param parent the parent node in the hierarchy
 */
@Suppress("TooManyFunctions")
@ComponentMarker
open class ComponentBase(
    var identifier: String,
    override var parent: FieldNodeParent,
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
     * The qaModelGenerator allows users to overwrite the DataClass generation of this specific component instance
     * for the QA model
     */
    var qaModelGenerator: ((dataClassBuilder: DataClassBuilder) -> Unit)? = null

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
     * The specificationGenerator allows users to overwrite the Specification generation
     */
    var specificationGenerator: ((categoryBuilder: CategoryBuilder) -> Unit)? = null

    /**
     * True iff this component is optional / accepts null values
     */
    open var isNullable: Boolean = true

    /**
     * True iff this component is required (just a pointer to !isNullable for convenience)
     */
    var isRequired: Boolean
        get() = !isNullable
        set(value) {
            isNullable = !value
        }

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
    fun parents(): Sequence<FieldNodeParent> =
        sequence {
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

    val camelCaseComponentIdentifier: String
        get() {
            return parents()
                .toList()
                .reversed()
                .mapNotNull {
                    when (it) {
                        is ComponentGroup -> Naming.getNameFromLabel(it.identifier).capitalizeEn()
                        is TopLevelComponentGroup -> Naming.getNameFromLabel(it.parent.identifier).capitalizeEn()
                        else -> null
                    }
                }.joinToString("") + identifier.capitalizeEn()
        }

    /**
     * Build this component instance into the provided Kotlin DataClass using the default
     * generator
     */
    open fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder): Unit =
        throw IllegalStateException(
            "This component (${javaClass.canonicalName})" +
                " did not implement Data model generation.",
        )

    /**
     * Build this component instance into the provided Kotlin DataClass
     */
    fun generateDataModel(dataClassBuilder: DataClassBuilder) =
        dataModelGenerator?.let { it(dataClassBuilder) } ?: generateDefaultDataModel(dataClassBuilder)

    /**
     * Build this component instance into the provided Kotlin DataClass using the default
     * generator
     */
    open fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder): Unit =
        throw IllegalStateException(
            "This component (${javaClass.canonicalName})" +
                " did not implement QA model generation.",
        )

    /**
     * Build this component instance into the provided Kotlin DataClass
     */
    fun generateQaModel(dataClassBuilder: DataClassBuilder) {
        val localQaModelGenerator = qaModelGenerator

        // dataModelGenerator != null ==> localQaModelGenerator != null
        require(dataModelGenerator == null || localQaModelGenerator != null) {
            "You should always overwrite dataModelGenerator when using qaModelGenerator"
        }
        if (localQaModelGenerator != null) {
            localQaModelGenerator(dataClassBuilder)
        } else {
            generateDefaultQaModel(dataClassBuilder)
        }
    }

    /**
     * Build this component instance into the provided view-section configuration
     * using the default generator for this component
     */
    open fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder): Unit =
        throw NotImplementedError(
            "This component (${javaClass.canonicalName})" +
                " did not implement view config conversion.",
        )

    /**
     * Build this component instance into the provided upload-section configuration
     * using the default generator for this component
     */
    open fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder): Unit =
        throw NotImplementedError(
            "This component (${javaClass.canonicalName})" +
                " did not implement upload config conversion.",
        )

    /**
     * Build this component instance into the provided view-section configuration
     */
    fun generateViewConfig(sectionConfigBuilder: SectionConfigBuilder) =
        viewConfigGenerator?.let { it(sectionConfigBuilder) } ?: generateDefaultViewConfig(sectionConfigBuilder)

    /**
     * Build this component instance into the provided upload-section configuration
     */
    fun generateUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) =
        uploadConfigGenerator?.let { it(uploadCategoryBuilder) }
            ?: generateDefaultUploadConfig(uploadCategoryBuilder)

    /**
     * Build the fixture code generation for this component using the default generator
     */
    open fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder): Unit =
        throw NotImplementedError(
            "This component (${javaClass.canonicalName})" +
                " did not implement fixture code-generation.",
        )

    /**
     * Build the fixture code generation for this component
     */
    fun generateFixtureGenerator(sectionBuilder: FixtureSectionBuilder) =
        fixtureGeneratorGenerator?.let { it(sectionBuilder) } ?: generateDefaultFixtureGenerator(sectionBuilder)

    /**
     * Returns the list of extended document references of the component
     */
    open fun getExtendedDocumentReference(): List<String> =
        if (documentSupport == ExtendedDocumentSupport) {
            listOf("${this.getKotlinFieldAccessor()}?.dataSource?.fileReference")
        } else {
            emptyList()
        }

    /**
     * Build the specification for this component
     */
    fun generateSpecification(specificationCategoryBuilder: CategoryBuilder) {
        specificationGenerator?.let { it(specificationCategoryBuilder) } ?: generateDefaultSpecification(specificationCategoryBuilder)
    }

    /**
     * Build the specification for this component using the default generator
     */
    open fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder): Unit =
        throw NotImplementedError(
            "This component (${javaClass.canonicalName})" +
                " did not implement specification generation.",
        )

    /**
     * Return constraints as a list of parseable strings
     */
    open fun getConstraints(): List<String>? = null
}
