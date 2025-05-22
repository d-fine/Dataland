// EU Taxonomy Implementation
package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsGeneralFiscalYearDeviationOptions
import org.dataland.datalandbackend.openApiClient.model.ExportFileType
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointEutaxonomyNonFinancialsGeneralFiscalYearDeviationOptions
import org.dataland.datalandbackend.openApiClient.model.QualityOptions
import org.dataland.e2etests.utils.BaseExportTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class EuTaxonomyNonFinancialsExportTest : BaseExportTest<EutaxonomyNonFinancialsData>() {
    private lateinit var fullTestData: EutaxonomyNonFinancialsData
    private lateinit var testDataWithNullField: EutaxonomyNonFinancialsData
    private lateinit var testDataWithNonNullField: EutaxonomyNonFinancialsData
    private val nullFieldName = "nfrdMandatory"
    private val nonNullFieldName = "fiscalYearDeviation"

    @BeforeAll
    fun setup() {
        val numberOfDataSets = 10
        // Get the full test data
        fullTestData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]

        // Create test data with an explicit null general field
        testDataWithNullField =
            fullTestData.copy(
                general =
                    fullTestData.general?.copy(
                        nfrdMandatory = null,
                    ),
                revenue = null,
                capex = null,
                opex = null,
            )

        // Create test data with null and non-null general fields
        testDataWithNonNullField =
            fullTestData.copy(
                general =
                    fullTestData.general?.copy(
                        fiscalYearDeviation =
                            ExtendedDataPointEutaxonomyNonFinancialsGeneralFiscalYearDeviationOptions(
                                value = EutaxonomyNonFinancialsGeneralFiscalYearDeviationOptions.Deviation,
                                quality = QualityOptions.Reported,
                                comment = "test",
                            ),
                    ),
                revenue = null,
                capex = null,
                opex = null,
            )

        // Setup companies and upload data using the base class method
        setupCompaniesAndData()
    }

    override fun getTestDataWithNullField(): EutaxonomyNonFinancialsData = testDataWithNullField

    override fun getTestDataWithNonNullField(): EutaxonomyNonFinancialsData = testDataWithNonNullField

    override fun getFullTestData(): EutaxonomyNonFinancialsData = fullTestData

    override fun getNullFieldName(): String = nullFieldName

    override fun uploadData(
        companyId: String,
        data: EutaxonomyNonFinancialsData,
        reportingPeriod: String,
    ) {
        apiAccessor.uploadWithWait(
            companyId = companyId,
            frameworkData = data,
            reportingPeriod = reportingPeriod,
            uploadFunction = apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
        )
    }

    override fun exportDataAsCsv(
        companyIds: List<String>,
        reportingPeriods: List<String>,
        keepValueFieldsOnly: Boolean,
    ): File =
        apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .exportCompanyAssociatedEutaxonomyNonFinancialsDataByDimensions(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.CSV,
                keepValueFieldsOnly = keepValueFieldsOnly,
            )

    override fun exportDataAsExcel(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File =
        apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .exportCompanyAssociatedEutaxonomyNonFinancialsDataByDimensions(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.EXCEL,
            )

    override fun retrieveData(companyId: String): Any =
        apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .getCompanyAssociatedEutaxonomyNonFinancialsDataByDimensions(
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
    fun `test CSV export with and without dataMetaInformation`() {
        testCsvExportIncludeDataMetaInformationFlag("general.$nonNullFieldName")
    }

    @Test
    fun `test CSV export for both companies has null field column with correct values`() {
        testCsvExportForBothCompanies()
    }

    @Test
    fun `test Excel export for both companies has null field column with correct values`() {
        testExcelExportForBothCompanies()
    }
}
