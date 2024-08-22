package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.springframework.beans.factory.annotation.Autowired
import java.io.File

class TestDataProvider(@Autowired var objectMapper: ObjectMapper) {
    private val jsonFile = File("./build/resources/test/CompanyInformationWithEutaxonomyNonFinancialsData.json")
    private val testCompanyInformationWithEutaxonomyNonFinancialsData =
        objectMapper.readValue(
            jsonFile,
            object : TypeReference<List<CompanyInformationWithData<EutaxonomyNonFinancialsData>>>() {},
        )

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> {
        return testCompanyInformationWithEutaxonomyNonFinancialsData.slice(
            0 until requiredQuantity,
        ).map { it.companyInformation }
    }

    fun getCompanyInformationWithoutIdentifiers(requiredQuantity: Int): List<CompanyInformation> {
        return getCompanyInformation(requiredQuantity)
            .map { it.copy(identifiers = IdentifierType.entries.associateWith { emptyList() }) }
    }

    fun getEuTaxonomyNonFinancialsDataset(): EutaxonomyNonFinancialsData {
        return testCompanyInformationWithEutaxonomyNonFinancialsData.first().t
    }

    fun getEmptyStoredCompanyEntity(): StoredCompanyEntity {
        return StoredCompanyEntity(
            "",
            "",
            null,
            null,
            null,
            "",
            null,
            "",
            "",
            mutableListOf(),
            null,
            mutableListOf(),
            "",
            false,
            null,
        )
    }
}
