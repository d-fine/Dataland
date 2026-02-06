package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.ExportJobInfo
import org.dataland.datalandbackend.openApiClient.model.ExportLatestRequestData
import org.dataland.datalandbackend.openApiClient.model.ExportRequestData
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.LksgGeneralProductionSpecific
import org.dataland.e2etests.utils.BaseExportTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class LksgExportTest : BaseExportTest<LksgData>() {
    private lateinit var fullTestData: LksgData
    private lateinit var testDataWithNullField: LksgData
    private lateinit var testDataWithNonNullField: LksgData
    private val nullFieldName = "numberOfProductionSites"

    @BeforeAll
    fun setup() {
        // Get the full test data
        fullTestData = apiAccessor.testDataProviderForLksgData.getTData(1)[0]

        // Create test data with null fields as specified
        testDataWithNullField =
            fullTestData.copy(
                // Keep general as is
                environmental = null, // Set environmental to null
                general =
                    fullTestData.general.copy(
                        productionSpecific =
                            fullTestData.general.productionSpecific?.copy(
                                listOfProductionSites = null, // addresses contain commas, which break the simple CSV parsing in the test
                                numberOfProductionSites = null,
                            ),
                    ),
            )

        // Create test data with non-null fields
        testDataWithNonNullField =
            fullTestData.copy(
                // Keep general as is
                environmental = null, // Set environmental to null
                general =
                    fullTestData.general.copy(
                        productionSpecific =
                            LksgGeneralProductionSpecific(
                                numberOfProductionSites = null,
                            ),
                    ),
            )
        testDataWithNonNullField =
            fullTestData.copy(
                // Keep general as is
                environmental = null, // Set environmental to null
                general =
                    fullTestData.general.copy(
                        productionSpecific =
                            LksgGeneralProductionSpecific(
                                numberOfProductionSites = BigDecimal("47176870"),
                            ),
                    ),
            )

        // Setup companies and upload data using the base class method
        setupCompaniesAndData()
    }

    override fun getTestDataWithNullField(): LksgData = testDataWithNullField

    override fun getTestDataWithNonNullField(): LksgData = testDataWithNonNullField

    override fun getFullTestData(): LksgData = fullTestData

    override fun getNullFieldName(): String = nullFieldName

    override fun retrieveData(
        companyId: String,
        reportingPeriod: String,
    ) = apiAccessor.dataControllerApiForLksgData
        .getCompanyAssociatedLksgDataByDimensions(
            reportingPeriod = reportingPeriod,
            companyId = companyId,
        )

    override fun uploadData(
        companyId: String,
        data: LksgData,
        reportingPeriod: String,
    ) {
        apiAccessor.uploadWithWait(
            companyId = companyId,
            frameworkData = data,
            reportingPeriod = reportingPeriod,
            uploadFunction = apiAccessor::lksgUploaderFunction,
        )
    }

    override fun getExportJobPostingFunction(): (ExportRequestData, Boolean?, Boolean?) -> ExportJobInfo =
        apiAccessor.dataControllerApiForLksgData::postExportJobCompanyAssociatedLksgDataByDimensions

    override fun getExportLatestJobPostingFunction(): (ExportLatestRequestData, Boolean?, Boolean?) -> ExportJobInfo =
        apiAccessor.dataControllerApiForLksgData::postExportLatestJobCompanyAssociatedLksgDataByDimensions

    @Test
    fun `test CSV export omits column for null field when only company with null value is exported`() {
        testCsvExportOmitsColumnForNullField()
    }

    @Test
    fun `test CSV export includes column for null field when company with non null value is exported`() {
        testCsvExportIncludesColumnForNonNullField()
    }

    @Test
    fun `test CSV export for both companies has null field column with correct values`() {
        testCsvExportForBothCompanies()
    }

    @Test
    fun `test Excel export for both companies has null field column with correct values`() {
        testExcelExportForBothCompanies()
    }

    @Test
    fun `test that exporting latest data yields the data with the latest reporting period`() {
        testExportLatest()
    }
}
