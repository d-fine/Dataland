package org.dataland.frameworktoolbox.frameworks.sfdr.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * Represents the SFDR-Specific "High Impact Climate Sectors" component
 */
class SfdrHighImpactClimateSectors(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        require(documentSupport is NoDocumentSupport) {
            "Data-Model generation for this component does not support any document support"
        }
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "Map", isNullable,
                listOf(
                    TypeReference("org.dataland.datalandbackend.frameworks.sfdr.custom.HighImpactClimateSector", false),
                    TypeReference("org.dataland.datalandbackend.frameworks.sfdr.custom.SfdrHighImpactClimateSectorEnergyConsumption", false),
                ),
            ),
            listOf(
                Annotation(
                    fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                    rawParameterSpec = "example = JsonExampleFormattingConstants.HIGH_IMPACT_CLIMATE_SECTORS_DEFAULT_VALUE",
                    applicationTargetPrefix = "field",
                    additionalImports = setOf("org.dataland.datalandbackend.utils.JsonExampleFormattingConstants"),
                ),
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        require(documentSupport is NoDocumentSupport) {
            "View-Page generation for this component does not support any document support"
        }
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatHighImpactClimateSectorForDisplay(${getTypescriptFieldAccessor(true)})",
                setOf(
                    "import { formatHighImpactClimateSectorForDisplay } from " +
                        "\"@/components/resources/dataTable/conversion/HighImpactClimateGetterFactory\";",
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        require(documentSupport is NoDocumentSupport) {
            "Upload-Page generation for this component does not support any document support"
        }
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "HighImpactClimateSectorsFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        require(documentSupport is NoDocumentSupport) {
            "Fake-Fixture generation for this component does not support any document support"
        }
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.generateHighImpactClimateSectors()",
        )
    }
}
