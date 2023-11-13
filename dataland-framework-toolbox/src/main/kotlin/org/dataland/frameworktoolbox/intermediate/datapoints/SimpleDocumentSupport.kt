package org.dataland.frameworktoolbox.intermediate.datapoints

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

data object SimpleDocumentSupport : DocumentSupport {
    override fun getJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference {
        return TypeReference(
            "org.dataland.datalandbackend.model.datapoints.BaseDataPoint",
            nullable,
            listOf(innerType),
        )
    }

    override fun getFrameworkDisplayValueLambda(
        innerLambda: FrameworkDisplayValueLambda,
        fieldLabel: String?,
        dataPointAccessor: String
    ): FrameworkDisplayValueLambda {
        requireNotNull(fieldLabel)
        return  FrameworkDisplayValueLambda(
            "wrapDisplayValueWithDatapointInformation(${innerLambda.lambdaBody}," +
                    " \"${StringEscapeUtils.escapeEcmaScript(fieldLabel)}\"," +
                    " $dataPointAccessor)",
            imports = innerLambda.imports +
                    "import { wrapDisplayValueWithDatapointInformation } from \"@/components/resources/dataTable/conversion/DataPoints\";"
        )
    }

    override fun getDataAccessor(dataPointAccessor: String, nullable: Boolean): String {
        return if (nullable) {
            "$dataPointAccessor?.value"
        } else {
            "$dataPointAccessor.value"
        }
    }
}
