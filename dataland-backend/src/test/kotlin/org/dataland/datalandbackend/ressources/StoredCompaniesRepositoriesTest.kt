package org.dataland.datalandbackend.ressources

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackendutils.model.QaStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class StoredCompaniesRepositoriesTest(
    @Autowired val companyAlterationManager: CompanyAlterationManager,
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
@Autowired val companyQueryManager: CompanyQueryManager
) {

    @Test
    fun `Check that only companies with accepted data sets are returned by the query`() {
        val acceptedCompany = companyAlterationManager.addCompany(
            CompanyInformation(
                companyName = "TestName1",
                headquarters = "TestHQ1",
                identifiers = emptyMap(),
                countryCode = "TestCountry",
                isTeaserCompany = false,
                companyAlternativeNames = listOf("alt1"),
                sector = null,
                companyLegalForm = null,
                headquartersPostalCode = null,
                website = null
            )
        )

        dataMetaInformationManager.storeDataMetaInformation(DataMetaInformationEntity(
            dataId = "1",
            company = acceptedCompany,
            dataType = "1",
            uploaderUserId = "1",
            uploadTime = 1,
            reportingPeriod = "1",
            currentlyActive = true,
            qaStatus = QaStatus.Accepted
        )
        )

        val pendingCompany = companyAlterationManager.addCompany(
            CompanyInformation(
                companyName = "TestName1",
                headquarters = "TestHQ1",
                identifiers = emptyMap(),
                countryCode = "TestCountry",
                isTeaserCompany = false,
                companyAlternativeNames = listOf("alt1"),
                sector = null,
                companyLegalForm = null,
                headquartersPostalCode = null,
                website = null
            )
        )

        dataMetaInformationManager.storeDataMetaInformation(DataMetaInformationEntity(
            dataId = "2",
            company = pendingCompany,
            dataType = "2",
            uploaderUserId = "2",
            uploadTime = 2,
            reportingPeriod = "2",
            currentlyActive = null,
            qaStatus = QaStatus.Pending
        )
        )



        println(companyQueryManager.getCompanyById(acceptedCompany.companyId))
        val testQueryResult = companyQueryManager.searchCompaniesAndGetApiModel(
            StoredCompanySearchFilter(
                countryCodeFilter = emptyList(),
                dataTypeFilter = emptyList(),
                sectorFilter = emptyList(),
                searchString = ""
            )
        )
        println(testQueryResult)
    }
}