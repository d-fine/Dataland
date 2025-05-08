// SFDR Test Implementation
package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.ExportFileType
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.e2etests.utils.BaseExportTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class SfdrExportTest : BaseExportTest<SfdrData>() {
    private lateinit var fullTestData: SfdrData
    private lateinit var testDataWithNullField: SfdrData
    private lateinit var testDataWithNonNullField: SfdrData
    private val nullFieldName = "humanRightsLegalProceedings"

    @BeforeAll
    fun setup() {
        // Get the full test data
        fullTestData = apiAccessor.testDataProviderForSfdrData.getTData(1)[0]

        // Create test data with null fields as specified
        testDataWithNullField =
            fullTestData.copy(
                // Keep general as is
                environmental = null, // Set environmental to null
                social =
                    fullTestData.social?.copy(
                        socialAndEmployeeMatters =
                            fullTestData.social?.socialAndEmployeeMatters?.copy(
                                humanRightsLegalProceedings = null,
                            ),
                    ),
            )

        // Create test data with non-null fields
        testDataWithNonNullField = fullTestData.copy()

        // Setup companies and upload data using the base class method
        setupCompaniesAndData()
    }

    override fun getTestDataWithNullField(): SfdrData = testDataWithNullField

    override fun getTestDataWithNonNullField(): SfdrData = testDataWithNonNullField

    override fun getFullTestData(): SfdrData = fullTestData

    override fun getNullFieldName(): String = nullFieldName

    override fun uploadData(
        companyId: String,
        data: SfdrData,
        reportingPeriod: String,
    ) {
        apiAccessor.uploadWithWait(
            companyId = companyId,
            frameworkData = data,
            reportingPeriod = reportingPeriod,
            uploadFunction = apiAccessor::sfdrUploaderFunction,
        )
    }

    override fun exportDataAsCsv(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File =
        apiAccessor.dataControllerApiForSfdrData
            .exportCompanyAssociatedSfdrDataByDimensions(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.CSV,
            )

    override fun exportDataAsCsvWithMetadata(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File =
        apiAccessor.dataControllerApiForSfdrData
            .exportCompanyAssociatedSfdrDataByDimensions(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.CSV,
                includeDataMetaInformation = true,
            )

    override fun exportDataAsExcel(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File =
        apiAccessor.dataControllerApiForSfdrData
            .exportCompanyAssociatedSfdrDataByDimensions(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.EXCEL,
            )

    override fun retrieveData(companyId: String): Any =
        apiAccessor.dataControllerApiForSfdrData
            .getCompanyAssociatedSfdrDataByDimensions(
                reportingPeriod = reportingPeriod,
                companyId = companyId,
            )

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
    fun `test CSV export with metadata includes metaInformation fields`() {
        testCsvExportWithMetadataIncludesMetaInformationFields()
    }
}
