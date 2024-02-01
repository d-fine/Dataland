package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the EuTaxonomy-Specific List of Activities component
 */
class EuTaxonomyListOfActivitiesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent, "org.dataland.datalandbackend.") {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            this.documentSupport.getJvmTypeReference(
                TypeReference(
                    "kotlin.collections.MutableList",
                    true,
                    listOf(
                        TypeReference(
                            "org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity",
                            false,
                        ),
                    ),
                ),
                true,
            ),
            this.documentSupport.getJvmAnnotations(),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        uploadCategoryBuilder.addStandardUploadConfigCell(
            frameworkUploadOptions = FrameworkUploadOptions(
                body = "getActivityNamesAsDropdownOptions()",
                imports = setOf(
                    "import { getActivityNamesAsDropdownOptions } from " +
                        "\"@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames\"",
                ),
            ),
            component = this,
            uploadComponentName = "MultiSelectFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            this.identifier,
            this.documentSupport.getFixtureExpression(
                fixtureExpression = "pickSubsetOfElements(Object.values(Activity))",
                nullableFixtureExpression =
                "dataGenerator.valueOrNull(pickSubsetOfElements(Object.values(Activity)))",
                nullable = this.isNullable,
            ),
            imports = setOf(
                TypeScriptImport("Activity", "@clients/backend"),
                TypeScriptImport(
                    "pickSubsetOfElements",
                    "@e2e/fixtures/FixtureUtils",
                ),
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatListOfStringsForDatatable(" +
                    "${this.getTypescriptFieldAccessor()}?.map(it => {\n" +
                    "                  return activityApiNameToHumanizedName(it)}), " +
                    "'${StringEscapeUtils.escapeEcmaScript(this.label)}'" +
                    ")",
                setOf(
                    TypeScriptImport(
                        "activityApiNameToHumanizedName",
                        "@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames",
                    ),
                    TypeScriptImport(
                        "formatListOfStringsForDatatable",
                        "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory",
                    ),
                ),
            ),
        )
    }
}
