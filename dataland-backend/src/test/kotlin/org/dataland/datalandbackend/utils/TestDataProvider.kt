package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import java.time.Instant

class TestDataProvider(
    @Autowired var objectMapper: ObjectMapper,
) {
    private val jsonFile = File("./build/resources/test/CompanyInformationWithLksgData.json")
    private val testCompanyInformationWithLksgData =
        objectMapper.readValue(
            jsonFile,
            object : TypeReference<List<CompanyInformationWithData<LksgData>>>() {},
        )

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> =
        testCompanyInformationWithLksgData
            .slice(
                0 until requiredQuantity,
            ).map { it.companyInformation }

    fun getCompanyInformationWithoutIdentifiers(requiredQuantity: Int): List<CompanyInformation> =
        getCompanyInformation(requiredQuantity)
            .map { it.copy(identifiers = IdentifierType.entries.associateWith { emptyList() }) }

    fun getLksgDataset(): LksgData = testCompanyInformationWithLksgData.first().t

    fun getEmptyStoredCompanyEntity(): StoredCompanyEntity =
        StoredCompanyEntity(
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

    fun addCompanyAndReturnStorableDatasetForIt(
        companyAlterationManager: CompanyAlterationManager,
        frameworkName: String,
    ): StorableDataset {
        val companyInformation = getCompanyInformation(1).first()
        val companyId = companyAlterationManager.addCompany(companyInformation).companyId
        return StorableDataset(
            companyId,
            DataType(frameworkName),
            "USER_ID_OF_AN_UPLOADING_USER",
            Instant.now().toEpochMilli(),
            "",
            "someData",
        )
    }
}
