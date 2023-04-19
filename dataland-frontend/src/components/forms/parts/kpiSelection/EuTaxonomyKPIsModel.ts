export const euTaxonomyKPIsModel = {
  creditInstitutionKpis: ["tradingPortfolio", "interbankLoans", "tradingPortfolioAndInterbankLoans", "greenAssetRatio"],
  insuranceKpis: ["taxonomyEligibleNonLifeInsuranceActivities"],
  investmentFirmKpis: ["greenAssetRatio"],
  eligibilityKpis: [
    "taxonomyEligibleActivity",
    "taxonomyNonEligibleActivity",
    "derivatives",
    "banksAndIssuers",
    "investmentNonNfrd",
  ],
  companyTypeToEligibilityKpis: {
    creditInstitutionKpis: "CreditInstitution",
    insuranceKpis: "InsuranceOrReinsurance",
    investmentFirmKpis: "InvestmentFirm",
    assetManagementKpis: "AssetManagement",
  },
  euTaxonomyDetailsPerCashFlowType: ["total", "aligned", "eligible"],
  euTaxonomyDetailsPerCashFlowFilesNames: {
    total: "totalAmount",
    aligned: "alignedPercentage",
    eligible: "eligiblePercentage",
  },
};

export const euTaxonomyKpiNameMappings = {
  investmentNonNfrd: "Exposures To Non-NFRD Entities",
  banksAndIssuers: "Exposures To Central Governments, Central Banks, Supranational Issuers",
  derivatives: "Exposures To Derivatives",
  taxonomyNonEligibleActivity: "Exposures To Taxonomy Non-eligible Economic Activities",
  taxonomyEligibleActivity: "Exposures To Taxonomy-eligible Economic Activities",
  taxonomyEligibleNonLifeInsuranceActivities: "Taxonomy-eligible Non-life Insurance Economic Activities",
  greenAssetRatio: "Green Asset Ratio",
  tradingPortfolio: "Trading Portfolio",
  tradingPortfolioInterbankLoans: "Trading Portfolio & On-demand Interbank Loans",
  interbankLoans: "On-demand Interbank Loans",
  reportDate: "Report Date",
  reportingPeriod: "Reporting Period",
  reportingObligation: "NFRD Mandatory",
  totalAmount: "Total Amount",
  alignedPercentage: "Aligned Percentage",
  eligiblePercentage: "Eligible Percentage",

  iSfS: "IS/FS",
  financialServicesTypes: "Financial Services Types",
  fiscalYear: "Fiscal Year",
  fiscalYearEnd: "Fiscal Year End",
  annualReport: "Annual Report",
  groupLevelAnnualReport: "Group Level Annual Report",
  annualReportDate: "Annual Report Date",
  annualReportCurrency: "Annual Report Currency",

  activityLevelReporting: "EU Taxonomy Activity Level Reporting",
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
  eligibleRevenue: "Eligible Revenue",
  alignedRevenue: "Aligned Revenue",
  totalRevenue: "Total Revenue",
  eligibleCapEx: "Eligible CapEx",
  alignedCapEx: "Aligned CapEx",
  totalCapEx: "Total CapEx",
  eligibleOpEx: "Eligible OpEx",
  alignedOpEx: "Aligned OpEx",
  totalOpEx: "Total OpEx",
};
export const euTaxonomyKpiInfoMappings = {
  investmentNonNfrd: "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  banksAndIssuers:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  derivatives: "For financial companies (FS), the percentage of total assets exposed to derivatives",
  taxonomyNonEligibleActivity:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  taxonomyEligibleActivity:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  taxonomyEligibleNonLifeInsuranceActivities:
    "For Insurance/Reinsurance companies, the percentage of Taxonomy-eligible non-life insurance economics activities. Insurance and reinsurance undertakings other than life insurance undertakings shall calculate the KPI related to underwriting activities and present the ‘gross premiums written’ non-life insurance revenue or, as applicable, reinsurance revenue corresponding to Taxonomy-aligned insurance or reinsurance activities",
  greenAssetRatio:
    "The proportion of the of credit institution’s assets financing and invested in taxonomy-aligned economic activities as a proportion of total covered assets",
  tradingPortfolio: "For Credit Institutions, the trading portfolio as a percentage of total assets",
  tradingPortfolioInterbankLoans:
    "For Credit Institutions, the trading portfolio and the on demand interbank loans as a percentage of total assets",
  interbankLoans: "For Credit Institutions, the on demand interbank loans as a percentage of total assets",
  reportDate: "The date the report was published",
  reportingPeriod: "The reporting period the dataset belongs to (e.g. a fiscal year)",
  reportingObligation: "The reporting obligation for companies whose number of employees is greater or equal to 500",
  totalAmount: "Total Amount",
  alignedPercentage: "Aligned Percentage",
  eligiblePercentage: "Eligible Percentage",

  isfs: "Distinguishes between non-financial companies (IS) and financial companies (FS). There are two possible values expected: 1- IS, 2- FS",
  financialServicesTypes:
    "Defines the type of financial company. Credit Institution (1), Insurance/Reinsurance (2), Asset Manager (3), Investment Firm (4)",

  fiscalYear: "Fiscal Year (Deviation/ No Deviation)",
  fiscalYearEnd: "The date the fiscal year ends",

  annualReport: "Link to Annual Report",
  groupLevelAnnualReport: "Is Annual Report on a Group level?",
  annualReportDate: "The date for which the information presented in the report is valid through",
  annualReportCurrency: "The 3-letter alpha code that represents the currency used in the report",

  activityLevelReporting: "Activity Level disclosure",
  fiscalYearDeviation: "Is fiscal year deviating from calender year?",
  numberOfEmployees: "Total number of employees (including temporary workers)",
  scopeOfEntities:
    "Does a list of legal entities covered by Sust./Annual/Integrated/ESEF report match with a list of legal entities covered by Audited Consolidated Financial Statement ",
  currency: "The 3-letter alpha code that represents the currency used in the report",

  sustainabilityReport: "Link to Sustainability Report",
  groupLevelSustainabilityReport: "Is Sustainability Report on a Group level?",
  sustainabilityReportDate: "The date for which the information presented in the report is valid through",
  sustainabilityReportCurrency: "The 3-letter alpha code that represents the currency used in the report",
  integratedReport: "Link to Integrated Report",
  groupLevelIntegratedReport: "Is Integrated Report on a Group level?",
  integratedReportDate: "The date for which the information presented in the report is valid through",
  integratedReportCurrency: "The 3-letter alpha code that represents the currency used in the report",
  esefReport: "Link to ESEF Report",
  groupLevelEsefReport: "Is ESEF Report on a Group level?",
  esefReportDate: "The date for which the information presented in the report is valid through",
  esefReportCurrency: "The 3-letter alpha code that represents the currency used in the report",
  nfrdMandatory: "The reporting obligation for companies whose number of employees is greater or equal to 500",

  quality: "The level of confidence associated to the value",
  page: "The page number of the document from where the information was sourced",
  report: "The report from where the information was sourced",
  tagName: "The name of the tag where the information is in the pdf",
  comment: "Free optional text",

  assurance: "Level of Assurance of the EU Taxonomy disclosure (Reasonable Assurance, Limited Assurance, None)",
  provider: "Name of the Audit company which provide assurance to EU Taxonomy data points ",
  AssurancePage:
    "The page number from where the text which proof that EU Taxonomy disclosures included in Assurance statement was sourced",
  AssuranceReport: "Link to Assurance Report",
  eligibleRevenue:
    "Percentage of the Revenue where the economic activity meets taxonomy criteria for substantial contribution to climate change mitigation and does no serious harm to the other environmental objectives (DNSH criteria)",
  alignedRevenue:
    "Percentage of the Revenue that is taxonomy-aligned, i.e., generated by an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  totalRevenue:
    "Total Revenue in MM (Millions) for the financial year. i.e., Income arising in the course of an entity's ordinary activities., the amounts derived from the sale of products and the provision of services after deducting sales rebates and value added tax and other taxes directly linked to turnover. Overall turnover is equivalent to a firm's total revenues over some period of time",
  eligibleCapEx:
    "Percentage of the CapEx that is part of a plan to meet taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
  alignedCapEx:
    "Percentage of the CapEx that is either already taxonomy-aligned or is part of a credible plan to extend or reach taxonomy alignment. i.e., an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  totalCapEx:
    "Total CapEx in MM (Millions) for the financial year. A capital expenditure (CapEx) is a payment for goods or services recorded, or capitalized, on the balance sheet instead of expensed on the income statement",

  eligibleOpEx:
    "Percentage of the OpEx that is part of a plan to meet taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
  alignedOpEx:
    "Percentage of the OpEx that is associated with taxonomy-aligned activities. i.e., for an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  totalOpEx:
    "Total OpEx in MM (Millions) for the financial year. Operating expenses (OpEx) are shorter term expenses required to meet the ongoing operational costs of running a business",
};
