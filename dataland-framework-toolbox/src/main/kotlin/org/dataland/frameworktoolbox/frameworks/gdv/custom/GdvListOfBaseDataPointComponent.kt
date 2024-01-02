package org.dataland.frameworktoolbox.frameworks.gdv.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * In-memory representation of a field that contains a list of base-data-points.
 * The base-data-points have strings as their values.
 */
class GdvListOfBaseDataPointComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    lateinit var descriptionColumnHeader: String
    lateinit var documentColumnHeader: String

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference(
                "List",
                isNullable,
                listOf(
                    SimpleDocumentSupport.getJvmTypeReference(
                        TypeReference("String", false),
                        false,
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "{\n" +
                    "return formatListOfBaseDataPoint(\n" +
                    "'${StringEscapeUtils.escapeEcmaScript(label)}',\n" +
                    "${getTypescriptFieldAccessor()},\n" +
                    "\"$descriptionColumnHeader\",\n" +
                    "\"$documentColumnHeader\",\n" +
                    ")\n" +
                    "}",
                setOf(
                    "import { formatListOfBaseDataPoint } from " +
                        "\"@/components/resources/dataTable/conversion/gdv/GdvListOfBaseDataPointGetterFactory\";",
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        uploadCategoryBuilder.addStandardUploadConfigCell(
            frameworkUploadOptions = null,
            component = this,
            uploadComponentName = "ListOfBaseDataPointsFormField",
            validation = null,
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.valueOrNull(" +
                "generateArray(" +
                "() => dataGenerator.guaranteedBaseDataPoint(dataGenerator.guaranteedShortString()), 1, 5, 0" +
                "))",
            setOf("import { generateArray } from \"@e2e/fixtures/FixtureUtils\";"),

        )
    }
}
