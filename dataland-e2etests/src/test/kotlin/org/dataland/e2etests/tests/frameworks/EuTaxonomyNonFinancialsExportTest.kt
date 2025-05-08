// EU Taxonomy Implementation
package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.ExportFileType
import org.dataland.e2etests.utils.BaseExportTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class EuTaxonomyNonFinancialsExportTest : BaseExportTest<EutaxonomyNonFinancialsData>() {
    private lateinit var fullTestData: EutaxonomyNonFinancialsData
    private lateinit var testDataWithNullField: EutaxonomyNonFinancialsData
    private lateinit var testDataWithNonNullField: EutaxonomyNonFinancialsData
    private val nullFieldName = "nfrdMandatory"

    @BeforeAll
    fun setup() {
        // Get the full test data
        fullTestData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1)[0]

        // Create test data with an explicit null general field
        testDataWithNullField =
            fullTestData.copy(
                general =
                    fullTestData.general?.copy(
                        nfrdMandatory = null,
//                            fullTestData.general?.nfrdMandatory?.copy(
//                                value = null,
//                                quality = null,
//                                comment = null,
//                                dataSource =
//                                    fullTestData.general?.nfrdMandatory?.dataSource?.copy(
//                                        fileReference = "",
//                                        page = null,
//                                        tagName = null,
//                                        fileName = null,
//                                        publicationDate = null,
//                                    ),
//                            ),
                    ),
                revenue = null,
                capex = null,
                opex = null,
            )

        // Create test data with non-null general fields
        testDataWithNonNullField =
            fullTestData.copy(
                general = fullTestData.general?.copy(),
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
    ): File =
        apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .exportCompanyAssociatedEutaxonomyNonFinancialsDataByDimensions(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.CSV,
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

    override fun exportDataAsCsvWithMetadata(
        companyIds: List<String>,
        reportingPeriods: List<String>,
    ): File =
        apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .exportCompanyAssociatedEutaxonomyNonFinancialsDataByDimensions(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.CSV,
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
        testCsvExportIncludeDataMetaInformationFlag("general.fiscalYearDeviation")
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
