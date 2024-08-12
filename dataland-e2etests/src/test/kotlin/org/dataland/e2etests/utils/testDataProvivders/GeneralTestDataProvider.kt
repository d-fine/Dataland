package org.dataland.e2etests.utils.testDataProvivders

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.FRONTEND_DISPLAYED_FRAMEWORKS

class GeneralTestDataProvider {

    private fun getRandomAlphaNumericString(): String {
        val allowedChars = ('a'..'z') + ('0'..'9')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun generateCompanyInformation(companyName: String, sector: String?): CompanyInformation {
        return CompanyInformation(
            companyName,
            "DummyCity",
            (
                mapOf(
                    IdentifierType.PermId.value to listOf(getRandomAlphaNumericString()),
                )
                ),
            "DE",
            sector = sector,
        )
    }

    fun generateCompanyInformationWithNameAndIdentifiers(
        lei: String?,
        isins: List<String>?,
        permId: String?,
    ): CompanyInformation {
        var identifiers = emptyMap<String, List<String>>()
        if (!lei.isNullOrEmpty()) identifiers = identifiers + mapOf(IdentifierType.Lei.value to listOf(lei))
        if (!isins.isNullOrEmpty()) identifiers = identifiers + mapOf(IdentifierType.Isin.value to isins)
        if (!permId.isNullOrEmpty()) identifiers = identifiers + mapOf(IdentifierType.PermId.value to listOf(permId))
        return CompanyInformation(
            "DummyCompany",
            "DummyCity",
            identifiers,
            "DE",
        )
    }

    private fun getListOfBackendOnlyFrameworks(): List<DataTypeEnum> {
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
