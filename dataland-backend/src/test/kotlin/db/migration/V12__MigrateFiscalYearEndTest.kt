package db.migration

import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(classes = [org.dataland.datalandbackend.DatalandBackend::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
@DefaultMocks
class V12__MigrateFiscalYearEndTest : BaseFlywayMigrationTest {
    @Autowired
    lateinit var companyAlterationManager: CompanyAlterationManager

    @Autowired
    lateinit var companyRepository: StoredCompanyRepository

    private fun createDummyCompany(
        originalFiscalYearEnd: String,
        expectedNewFiscalYearEnd: String,
    ) = companyAlterationManager
        .addCompany(
            CompanyInformation(
                companyName = expectedNewFiscalYearEnd,
                headquarters = "Nowhere",
                countryCode = "US",
                identifiers = mapOf(IdentifierType.Lei to listOf(UUID.randomUUID().toString())),
                fiscalYearEnd = originalFiscalYearEnd,
            ),
        )

    override fun getFlywayBaselineVersion(): String = "11"

    override fun getFlywayTargetVersion(): String = "12"

    override fun setupBeforeMigration() {
        createDummyCompany("2023-12-31", "31-Dec")
        createDummyCompany("2024-03-07", "07-Mar")
        createDummyCompany("2024-08-01", "01-Aug")
        createDummyCompany("1998-06-22", "22-Jun")
    }

    @Test
    fun `fiscal year end values are migrated correctly`() =
        companyRepository.findAll().forEach {
            assertEquals(it.companyName, it.fiscalYearEnd)
        }
}
