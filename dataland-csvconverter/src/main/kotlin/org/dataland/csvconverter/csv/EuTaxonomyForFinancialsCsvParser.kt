package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyTypeParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.commonfieldparsers.EuTaxonomyCommonFieldParser
import org.dataland.csvconverter.csv.commonfieldparsers.FiscalYearParser
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.csvconverter.csv.utils.NullCheckExtension.checkIfAllFieldsAreNull
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import org.dataland.datalandbackend.model.eutaxonomy.financials.CreditInstitutionKpis
import org.dataland.datalandbackend.model.eutaxonomy.financials.EligibilityKpis
import org.dataland.datalandbackend.model.eutaxonomy.financials.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.model.eutaxonomy.financials.InsuranceKpis
import org.dataland.datalandbackend.model.eutaxonomy.financials.InvestmentFirmKpis
import java.util.EnumSet

/**
 * This class contains the parsing logic for the EuTaxonomyForFinancials framework
 */
class EuTaxonomyForFinancialsCsvParser(
    private val commonFieldParser: EuTaxonomyCommonFieldParser,
    private val companyTypeParser: CompanyTypeParser,
    private val dataPointParser: DataPointParser,
    private val assuranceDataParser: AssuranceDataParser,
    private val fiscalYearParser: FiscalYearParser,
    private val companyReportParser: CompanyReportParser,
) : CsvFrameworkParser<EuTaxonomyDataForFinancials> {

    /**
     * general string Mappings
     */
    private val columnMappingEuTaxonomyForFinancials = mapOf(
        "financialServicesType" to "FS - company type",
        "tradingPortfolio" to "Trading portfolio",
        "interbankLoans" to "On-demand interbank loans",
        "tradingPortfolioAndInterbankLoans" to "Trading portfolio & on-demand interbank loans",
        "taxonomyEligibleNonLifeInsuranceActivities" to "Taxonomy-eligible non-life insurance economic activities",
        "greenAssetRatioCreditInstitution" to "Green Asset Ratio Credit Institution",
        "greenAssetRatioInvestmentFirm" to "Green Asset Ratio Investment Firm",
    )

    private val financialServicesParser = EnumCsvParser(
        mapOf(
            "1" to FinancialServicesType.CreditInstitution,
            "2" to FinancialServicesType.InsuranceOrReinsurance,
            "3" to FinancialServicesType.AssetManagement,
            "4" to FinancialServicesType.InvestmentFirm,
        ),
    )

    /**
     * Function retrieving all Financial Service types of the company
     */
    private fun getFinancialServiceTypes(csvLineData: Map<String, String>): EnumSet<FinancialServicesType> {
        val csvData = columnMappingEuTaxonomyForFinancials.getCsvValue("financialServicesType", csvLineData)
        val split = csvData.split(",")
        return EnumSet.copyOf(
            split.map {
                financialServicesParser.parse(
                    "Financial Services Type",
                    it.trim(),
                )
            },
        )
    }

    override fun validateLine(companyData: CompanyInformation, row: Map<String, String>): Boolean {
        return companyTypeParser.getCompanyType(row) == "FS"
    }

    /**
     * Callable function generating the string-maps for the Eligibility KPIs for all Financial Service Types
     */

    private val columnMappingFinancialType = mapOf(
        FinancialServicesType.CreditInstitution to "Credit Institution",
        FinancialServicesType.InvestmentFirm to "Investment Firm",
        FinancialServicesType.AssetManagement to "Asset Management Company",
        FinancialServicesType.InsuranceOrReinsurance to "Insurance/Reinsurance",
    )
    private fun buildEligibilityColumnMapping(type: FinancialServicesType): Map<String, String> {
        return mapOf(
            "investmentNonNfrd" to
                "Exposures to non-NFRD entities ${columnMappingFinancialType[type]}",
            "taxonomyEligibleActivity" to
                "Exposures to taxonomy-eligible economic activities" +
                " ${columnMappingFinancialType[type]}",
            "taxonomyNonEligibleActivity" to
                "Exposures to taxonomy non-eligible economic activities" +
                " ${columnMappingFinancialType[type]}",
            "banksAndIssuers" to
                "Exposures to central governments, central banks, supranational issuers" +
                " ${columnMappingFinancialType[type]}",
            "derivatives" to
                "Exposures to derivatives ${columnMappingFinancialType[type]}",
        )
    }

    /**
     * Callable functions assembling the different types of KPIs
     */
    @Suppress("kotlin:S138")
    private fun buildSingleEligibilityKpis(row: Map<String, String>, type: FinancialServicesType): EligibilityKpis? {
        val eligibilityColumnMapping = buildEligibilityColumnMapping(type)
        val eligibilityKpis = EligibilityKpis(
            taxonomyEligibleActivity = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping,
                row,
                "taxonomyEligibleActivity",
            ),
            taxonomyNonEligibleActivity = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping,
                row,
                "taxonomyNonEligibleActivity",
            ),
            banksAndIssuers = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping,
                row,
                "banksAndIssuers",
            ),
            derivatives = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping,
                row,
                "derivatives",
            ),
            investmentNonNfrd = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping,
                row,
                "investmentNonNfrd",
            ),
        )
        return if (eligibilityKpis.checkIfAllFieldsAreNull()) {
            null
        } else {
            eligibilityKpis
        }
    }

    private fun buildEligibilityKpis(
        row: Map<String, String>,
        types: EnumSet<FinancialServicesType>,
    ): Map<FinancialServicesType, EligibilityKpis> {
        val presentKpis = FinancialServicesType.values()
            .mapNotNull { fsType -> buildSingleEligibilityKpis(row, fsType)?.let { fsType to it } }
            .toMap()
        if (!types.containsAll(presentKpis.keys)) {
            throw IllegalArgumentException(
                "EligibilityKpi values have been specified for ${presentKpis.keys}" +
                    " but the company is only of types $types",
            )
        }
        return presentKpis
    }

    private fun buildCreditInstitutionKpis(
        row: Map<String, String>,
    ): CreditInstitutionKpis? {
        val creditInstitutionKpis = CreditInstitutionKpis(
            tradingPortfolio = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "tradingPortfolio",
            ),
            interbankLoans = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "interbankLoans",
            ),
            tradingPortfolioAndInterbankLoans = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "tradingPortfolioAndInterbankLoans",
            ),
            greenAssetRatio = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "greenAssetRatioCreditInstitution",
            ),
        )
        return if (creditInstitutionKpis.checkIfAllFieldsAreNull()) {
            null
        } else {
            creditInstitutionKpis
        }
    }

    private fun buildInvestmentFirmKpis(row: Map<String, String>): InvestmentFirmKpis? {
        val investmentFirmKpis = InvestmentFirmKpis(
            greenAssetRatio = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials,
                row,
                "greenAssetRatioInvestmentFirm",
            ),
        )
        return if (investmentFirmKpis.checkIfAllFieldsAreNull()) {
            null
        } else {
            investmentFirmKpis
        }
    }

    private fun buildInsuranceKpis(row: Map<String, String>): InsuranceKpis? {
        val insuranceKpis = InsuranceKpis(
            taxonomyEligibleNonLifeInsuranceActivities = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials,
                row,
                "taxonomyEligibleNonLifeInsuranceActivities",
            ),
        )
        return if (insuranceKpis.checkIfAllFieldsAreNull()) {
            null
        } else {
            insuranceKpis
        }
    }

    /**
     Assembles all partial information into one EuTaxonomyDataForFinancials object
     */
    override fun buildData(row: Map<String, String>): EuTaxonomyDataForFinancials {
        val financialServicesTypes = getFinancialServiceTypes(row)
        return EuTaxonomyDataForFinancials(
            eligibilityKpis = buildEligibilityKpis(row, financialServicesTypes),
            creditInstitutionKpis = buildCreditInstitutionKpis(row),
            insuranceKpis = buildInsuranceKpis(row),
            fiscalYearDeviation = fiscalYearParser.getFiscalYearDeviation(row),
            fiscalYearEnd = fiscalYearParser.getFiscalYearEnd(row),
            scopeOfEntities = commonFieldParser.getScopeOfEntities(row),
            reportingObligation = commonFieldParser.getReportingObligation(row),
            activityLevelReporting = commonFieldParser.getActivityLevelReporting(row),
            assurance = assuranceDataParser.buildSingleAssuranceData(row),
            investmentFirmKpis = buildInvestmentFirmKpis(row),
            financialServicesTypes = financialServicesTypes,
            numberOfEmployees = commonFieldParser.getNumberOfEmployees(row),
            referencedReports = companyReportParser.buildMapOfAllCompanyReports(row),
        )
    }
}
