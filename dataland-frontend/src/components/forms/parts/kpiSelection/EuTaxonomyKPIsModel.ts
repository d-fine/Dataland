import { EuTaxonomyDataForFinancialsFinancialServicesTypesEnum } from "@clients/backend";

export const euTaxonomyKPIsModel = {
  creditInstitutionKpis: [
    "tradingPortfolioInPercent",
    "interbankLoansInPercent",
    "tradingPortfolioAndInterbankLoansInPercent",
    "greenAssetRatioInPercent",
  ],
  insuranceKpis: ["taxonomyEligibleNonLifeInsuranceActivitiesInPercent"],
  investmentFirmKpis: ["greenAssetRatioInPercent"],
  eligibilityKpis: [
    "taxonomyEligibleActivityInPercent",
    "taxonomyNonEligibleActivityInPercent",
    "derivativesInPercent",
    "banksAndIssuersInPercent",
    "investmentNonNfrdInPercent",
  ],
  kpisFieldNameToFinancialServiceType: {
    creditInstitutionKpis: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution,
    insuranceKpis: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance,
    investmentFirmKpis: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm,
    assetManagementKpis: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement,
  },
  euTaxonomyDetailsPerCashFlowType: ["total", "aligned", "eligible"],
  euTaxonomyDetailsPerCashFlowFilesNames: {
    total: "totalAmount",
    aligned: "alignedData",
    eligible: "eligibleData",
  },
};

/**
 * Returns the kpi field name for a financial services type enum that is passed as param.
 * E.g. the input "EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution" should return
 * "creditInstitutionKpis".
 * @param financialServiceType The financial service type enum for which the kpi field name shall be returned
 * @returns the kpi field name
 */
export function getKpiFieldNameForOneFinancialServiceType(
  financialServiceType: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
): string {
  const matchingKeys = [];
  for (const [key, value] of Object.entries(euTaxonomyKPIsModel.kpisFieldNameToFinancialServiceType)) {
    if (value === financialServiceType) {
      matchingKeys.push(key);
    }
  }
  if (matchingKeys.length === 0) {
    throw new Error(`No matching key found for financial service type: ${financialServiceType}`);
  } else if (matchingKeys.length > 1) {
    throw new Error(`Multiple matching keys found for financial service type: ${financialServiceType}`);
  }
  return matchingKeys[0];
}

export const euTaxonomyKpiNameMappings = {
  investmentNonNfrdInPercent: "Exposures To Non-NFRD Entities",
  banksAndIssuersInPercent: "Exposures To Central Governments, Central Banks, Supranational Issuers",
  derivativesInPercent: "Exposures To Derivatives",
  taxonomyNonEligibleActivityInPercent: "Exposures To Taxonomy Non-eligible Economic Activities",
  taxonomyEligibleActivityInPercent: "Exposures To Taxonomy-eligible Economic Activities",
  taxonomyEligibleNonLifeInsuranceActivitiesInPercent: "Taxonomy-eligible Non-life Insurance Economic Activities",
  greenAssetRatioInPercent: "Green Asset Ratio",
  tradingPortfolioInPercent: "Trading Portfolio",
  tradingPortfolioAndInterbankLoansInPercent: "Trading Portfolio & On-demand Interbank Loans",
  interbankLoansInPercent: "On-demand Interbank Loans",
  reportDate: "Report Date",
  reportingPeriod: "Reporting Period",
  totalAmount: "Total Amount",
  alignedPercentage: "Aligned Percentage",
  alignedAmount: "Aligned Amount",
  eligiblePercentage: "Eligible Percentage",
  eligibleAmount: "Eligible Amount",

  iSfS: "IS/FS",
  financialServicesTypes: "Financial Services Types",
  fiscalYear: "Fiscal Year",
  fiscalYearEnd: "Fiscal Year End",
  annualReport: "Annual Report",
  groupLevelAnnualReport: "Group Level Annual Report",
  annualReportDate: "Annual Report Date",
  annualReportCurrency: "Annual Report Currency",

  euTaxonomyActivityLevelReporting: "EU Taxonomy Activity Level Reporting",
  fiscalYearDeviation: "Fiscal Year is deviating",
  numberOfEmployees: "Number Of Employees",
  scopeOfEntities: "Scope Of Entities",
  currency: "Currency used in the report",

  sustainabilityReport: "Sustainability Report",
  groupLevelSustainabilityReport: "Group Level Sustainability Report",
  sustainabilityReportDate: "Sustainability Report Date",
  sustainabilityReportCurrency: "Sustainability Report Currency",
  integratedReport: "Integrated Report",
  groupLevelIntegratedReport: "Group Level Integrated Report",
  integratedReportDate: "Integrated Report Date",
  integratedReportCurrency: "Integrated Report Currency",
  esefReport: "ESEF Report",
  groupLevelEsefReport: "Group Level ESEF Report",
  esefReportDate: "ESEF Report Date",
  esefReportCurrency: "ESEF Report Currency",
  nfrdMandatory: "NFRD Mandatory",

  quality: "Quality",
  page: "Page",
  report: "Report",
  tagName: "Tag Name",
  comment: "Comment",

  assurance: "Assurance",
  provider: "Assurance Provider",
  AssurancePage: "Assurance Page",
  AssuranceReport: "Assurance Report",
  eligiblePercentageRevenue: "Eligible Revenue (%)",
  eligibleRevenue: "Eligible Revenue",
  alignedPercentageRevenue: "Aligned Revenue (%)",
  alignedRevenue: "Aligned Revenue",
  totalRevenue: "Total Revenue",
  eligibleCapExPercentage: "Eligible CapEx (%)",
  eligibleCapEx: "Eligible CapEx",
  alignedCapExPercentage: "Aligned CapEx (%)",
  alignedCapEx: "Aligned CapEx",
  totalCapEx: "Total CapEx",
  eligibleOpExPercentage: "Eligible OpEx (%)",
  eligibleOpEx: "Eligible OpEx",
  alignedOpExPercentage: "Aligned OpEx (%)",
  alignedOpEx: "Aligned OpEx",
  totalOpEx: "Total OpEx",
};
export const euTaxonomyKpiInfoMappings = {
  investmentNonNfrdInPercent:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities.",
  banksAndIssuersInPercent:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers.",
  derivativesInPercent: "For financial companies (FS), the percentage of total assets exposed to derivatives.",
  taxonomyNonEligibleActivityInPercent:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. I.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation.",
  taxonomyEligibleActivityInPercent:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities.",
  taxonomyEligibleNonLifeInsuranceActivitiesInPercent:
    "For Insurance/Reinsurance companies, the percentage of Taxonomy-eligible non-life insurance economics activities. Insurance and reinsurance undertakings other than life insurance undertakings shall calculate the KPI related to underwriting activities and present the ‘gross premiums written’ non-life insurance revenue or, as applicable, reinsurance revenue corresponding to Taxonomy-aligned insurance or reinsurance activities.",
  greenAssetRatioInPercent:
    "The proportion of assets financing and invested in taxonomy-aligned economic activities as a proportion of total covered assets.",
  tradingPortfolioInPercent: "For Credit Institutions, the trading portfolio as a percentage of total assets.",
  tradingPortfolioAndInterbankLoansInPercent:
    "For Credit Institutions, the trading portfolio and the on demand interbank loans as a percentage of total assets.",
  interbankLoansInPercent: "For Credit Institutions, the on demand interbank loans as a percentage of total assets.",
  reportDate: "The date until which the information presented in the report is valid.",
  reportingPeriod: "The reporting period the dataset belongs to (e.g. a fiscal year).",
  nfrdMandatory: "The reporting obligation for companies whose number of employees is greater or equal to 500.",
  totalAmount: "Total Amount",
  alignedPercentage: "Aligned Percentage",
  alignedAmount: "Aligned Amount",
  eligiblePercentage: "Eligible Percentage",
  eligibleAmount: "Eligible Amount",

  isfs: "Distinguishes between non-financial companies (IS) and financial companies (FS). There are two possible values expected: 1- IS, 2- FS.",
  financialServicesTypes:
    "Defines the type of financial company. Credit Institution (1), Insurance/Reinsurance (2), Asset Manager (3), Investment Firm (4).",

  fiscalYearDeviation: "Does the fiscal year deviate from the calendar year?",
  fiscalYear: "Fiscal Year (Deviation/ No Deviation)",
  fiscalYearEnd: "The date at which the fiscal year ends.",

  annualReport: "Link to Annual Report.",
  groupLevelAnnualReport: "Is the Annual Report on a Group level?",
  annualReportDate: "The date until which the information presented in the report is valid.",
  annualReportCurrency: "The 3-letter alpha code that represents the currency used in the report.",

  euTaxonomyActivityLevelReporting: "Does the company report on the EU Taxonomy?",
  numberOfEmployees: "Total number of employees (including temporary workers)",
  scopeOfEntities: "Are all Group legal entities covered in the report?",
  currency: "The 3-letter alpha code that represents the currency used in the report.",

  quality: "The level of confidence associated to the value.",
  page: "The page number of the document from where the information was sourced.",
  report: "The report from where the information was sourced.",
  tagName: "The name of the tag where the information is in the pdf.",
  comment: "Free optional text",

  assurance: "Level of Assurance of the EU Taxonomy disclosure (Reasonable Assurance, Limited Assurance, None).",
  provider: "Name of the Audit company which provides assurance to EU Taxonomy data points.",
  AssurancePage:
    "The page number of the text proving that the EU Taxonomy disclosures are included in Assurance statement.",
  AssuranceReport: "Link to Assurance Report.",

  eligibleRevenue:
    "Percentage of the Revenue where the economic activity meets taxonomy criteria for substantial contribution to climate change mitigation and does no serious harm to the other environmental objectives (DNSH criteria)",
  eligibleRevenueAmount:
    "The revenue where the economic activity meets taxonomy criteria for substantial contribution to either climate change mitigation or climate change adaptation",
  alignedRevenue:
    "Percentage of the Revenue that is taxonomy-aligned, i.e., generated by an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  alignedRevenueAmount:
    "The revenue that is taxonomy-aligned, i.e., generated by an eligible economic activity that is making a substantial contribution to either climate change mitigation or climate change adaptation",
  totalRevenue:
    "Total revenue for the financial year. I.e., income arising in the course of an entity's ordinary activities, the amounts derived from the sale of products and the provision of services after deducting sales rebates and value added tax and other taxes directly linked to turnover. Overall turnover is equivalent to a firm's total revenues over some period of time.",

  eligibleCapEx:
    "Percentage of the CapEx that is part of a plan to meet taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
  eligibleCapExAmount:
    "The CapEx that is part of a plan to meet taxonomy criteria for substantial contribution to either climate change mitigation or climate change adaptation",
  alignedCapEx:
    "Percentage of the CapEx that is either already taxonomy-aligned or is part of a credible plan to extend or reach taxonomy alignment. I.e., an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  alignedCapExAmount:
    "The CapEx that is either already taxonomy-aligned or is part of a credible plan to extend or reach taxonomy alignment, i.e., an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  totalCapEx:
    "Total CapEx for the financial year. A capital expenditure (CapEx) is a payment for goods or services recorded, or capitalized, on the balance sheet instead of expensed on the income statement.",

  eligibleOpEx:
    "Percentage of the OpEx that is part of a plan to meet taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
  eligibleOpExAmount:
    "The OpEx that is part of a plan to meet the taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
  alignedOpEx:
    "Percentage of the OpEx that is associated with taxonomy-aligned activities. I.e., for an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  alignedOpExAmount:
    "The OpEx that is associated with taxonomy-aligned activities. i.e., for an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  totalOpEx:
    "Total OpEx for the financial year. Operating expenses (OpEx) are shorter term expenses required to meet the ongoing operational costs of running a business.",
};
