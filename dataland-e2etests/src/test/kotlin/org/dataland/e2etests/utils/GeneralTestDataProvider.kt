package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

class GeneralTestDataProvider() {

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

    private fun getListOfBackendOnlyFrameworks(): List<DataTypeEnum> {
        return DataTypeEnum.values().toMutableList().filter { !FRONTEND_DISPLAYED_FRAMEWORKS.contains(it) }
    }

    fun generateOneCompanyInformationPerBackendOnlyFramework(): Map<DataTypeEnum, CompanyInformation> {
        val map = mutableMapOf<DataTypeEnum, CompanyInformation>()
        getListOfBackendOnlyFrameworks().forEach {
            map[it] = generateCompanyInformation(it.name + "Company", it.name + "HiddenSector")
        }
        return map.toMap()
    }
}
