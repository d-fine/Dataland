package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyTypeParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.commonfieldparsers.EuTaxonomyCommonFieldParser
import org.dataland.csvconverter.csv.commonfieldparsers.FiscalYearParser
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
        "greenAssetRatioInvestmentFirm" to "Green Asset Ratio Investment Firm"
    )

    private val columnMappingCompanyType = mapOf(
        FinancialServicesType.CreditInstitution to "Credit Institution",
        FinancialServicesType.InvestmentFirm to "Investment Firm",
        FinancialServicesType.AssetManagement to "Asset Management Company",
        FinancialServicesType.InsuranceOrReinsurance to "Insurance/Reinsurance",
    )

    private val financialServicesMap = mapOf<FinancialServicesType, String>(
        FinancialServicesType.CreditInstitution to "1",
        FinancialServicesType.InsuranceOrReinsurance to "2",
        FinancialServicesType.AssetManagement to "3",
        FinancialServicesType.InvestmentFirm to "4"
    )
    /**
     * Function retrieving all Financial Service types of the company
     */

    private fun getFinancialServiceTypes(csvLineData: Map<String, String>): EnumSet<FinancialServicesType> {
        val csvData = columnMappingEuTaxonomyForFinancials.getCsvValue("financialServicesType", csvLineData)!!
        val split = csvData.split(",").map { it.trim() }
        return EnumSet.copyOf(
            split.map {
                    candidate ->
                FinancialServicesType.values().firstOrNull {
                    candidate.equals(financialServicesMap[it], ignoreCase = true)
                } ?: throw IllegalArgumentException("Could not determine financial services type")
            }
        )
    }

    override fun validateLine(companyData: CompanyInformation, row: Map<String, String>): Boolean {
        return companyTypeParser.getCompanyType(row) == "FS"
    }

    /**
     * Callable function generating the string-maps for the Eligibility KPIs for all Financial Service Types
     */
    private fun buildEligibilityColumnMapping(type: FinancialServicesType): Map<String, String> {
        return mapOf(
            "investmentNonNfrd" to
                "Exposures to non-NFRD entities ${columnMappingCompanyType[type]}",
            "taxonomyEligibleActivity" to
                "Exposures to taxonomy-eligible economic activities" +
                " ${columnMappingCompanyType[type]}",
            "taxonomyNonEligibleActivity" to
                "Exposures to taxonomy non-eligible economic activities" +
                " ${columnMappingCompanyType[type]}",
            "banksAndIssuers" to
                "Exposures to central governments, central banks, supranational issuers" +
                " ${columnMappingCompanyType[type]}",
            "derivatives" to
                "Exposures to derivatives ${columnMappingCompanyType[type]}",
        )
    }

    /**
     * Callable functions assembling the different types of KPIs
     */
    @Suppress("kotlin:S138")
    private fun buildSingleEligibilityKpis(row: Map<String, String>, type: FinancialServicesType): EligibilityKpis {
        val eligibilityColumnMapping = buildEligibilityColumnMapping(type)
        return EligibilityKpis(
            taxonomyEligibleActivity = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping, row,
                "taxonomyEligibleActivity"
            ),
            taxonomyNonEligibleActivity = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping, row,
                "taxonomyNonEligibleActivity"
            ),
            banksAndIssuers = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping, row,
                "banksAndIssuers"
            ),
            derivatives = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping, row,
                "derivatives"
            ),
            investmentNonNfrd = dataPointParser.buildPercentageDataPoint(
                eligibilityColumnMapping, row,
                "investmentNonNfrd"
            ),
        )
    }

    private fun buildEligibilityKpis(
        row: Map<String, String>,
        types: EnumSet<FinancialServicesType>
    ): Map<FinancialServicesType, EligibilityKpis> {
        return types.associateWith { buildSingleEligibilityKpis(row, it) }
    }

    private fun buildCreditInstitutionKpis(
        row: Map<String, String>
    ): CreditInstitutionKpis? {
        val creditInstitutionKpis = CreditInstitutionKpis(
            tradingPortfolio = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row,
                "tradingPortfolio"
            ),
            interbankLoans = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row,
                "interbankLoans"
            ),
            tradingPortfolioAndInterbankLoans = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "tradingPortfolioAndInterbankLoans"
            ),
            greenAssetRatio = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "greenAssetRatioCreditInstitution"
            ),
        )
        return if (creditInstitutionKpis.checkIfAllFieldsAreNull()) null
        else creditInstitutionKpis
    }

    private fun buildInvestmentFirmKpis(row: Map<String, String>): InvestmentFirmKpis? {
        val investmentFirmKpis = InvestmentFirmKpis(
            greenAssetRatio = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "greenAssetRatioInvestmentFirm"
            ),
        )
        return if (investmentFirmKpis.checkIfAllFieldsAreNull()) null
        else investmentFirmKpis
    }

    private fun buildInsuranceKpis(row: Map<String, String>): InsuranceKpis? {
        val insuranceKpis = InsuranceKpis(
            taxonomyEligibleNonLifeInsuranceActivities = dataPointParser.buildPercentageDataPoint(
                columnMappingEuTaxonomyForFinancials, row, "taxonomyEligibleNonLifeInsuranceActivities"
            ),
        )
        return if (insuranceKpis.checkIfAllFieldsAreNull()) null
        else insuranceKpis
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
