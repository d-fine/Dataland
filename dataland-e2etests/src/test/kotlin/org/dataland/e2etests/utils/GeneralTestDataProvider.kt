package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.FRONTEND_DISPLAYED_FRAMEWORKS

class GeneralTestDataProvider {

    private fun getRandomAlphaNumericString(): String {
        val allowedChars = ('a'..'z') + ('0'..'9')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun generateCompanyInformation(companyName: String, sector: String): CompanyInformation {
        return CompanyInformation(
            companyName, "DummyCity", sector,
            (
                listOf(
                    CompanyIdentifier(CompanyIdentifier.IdentifierType.permId, getRandomAlphaNumericString())
                )
                ),
            "DE"
        )
    }

    fun getListOfBackendOnlyFrameworks(): List<DataTypeEnum> {
        return DataTypeEnum.values().toMutableList().filter { !FRONTEND_DISPLAYED_FRAMEWORKS.contains(it) }
    }

    fun generateOneCompanyInformationPerBackendOnlyFramework(): Map<DataTypeEnum, List<CompanyInformation>> {
        val map = mutableMapOf<DataTypeEnum, List<CompanyInformation>>()
        getListOfBackendOnlyFrameworks().forEach {
            map[it] = listOf(generateCompanyInformation(it.name + "Company", it.name + "HiddenSector"))
        }
        return map.toMap()
    }
}
