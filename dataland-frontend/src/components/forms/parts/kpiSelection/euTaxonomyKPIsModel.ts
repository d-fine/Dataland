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
  euTaxonomyDetailsPerCashFlowType: ["totalAmount", "alignedPercentage", "eligiblePercentage"],
};

export const euTaxonomyKpiNameMappings = {
  financialServicesTypes: "Financial Services Types",
  investmentNonNfrd: "Investment Non NFRD",
  banksAndIssuers: "Banks and Issuers",
  derivatives: "Exposures To Derivatives",
  taxonomyNonEligibleActivity: "Taxonomy non Eligible Activity",
  taxonomyEligibleActivity: "Taxonomy Eligible Activity",
  reportDate: "Report Date",
  reportingPeriod: "Reporting Period",
  reportingObligation: "NFRD Mandatory",
  totalAmount: "Total Amount",
  alignedPercentage: "Aligned Percentage",
  eligiblePercentage: "Eligible Percentage",

  isfs: "ISFS",
  fsCompanyType: "FS  Company Type",
  fiscalYear: "Fiscal Year",
  fiscalYearEnd: "Fiscal Year End",
  annualReport: "Annual Report",
  groupLevelAnnualReport: "Group Level Annual Report",
  annualReportDate: "Annual Report Date",
  annualReportCurrency: "Annual Report Currency",
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
  scopeOfEntities: "Scope Of Entities",
  euTaxonomyActivityLevelReporting: "EU Taxonomy Activity Level Reporting",
  numberOfEmployees: "Number Of Employees",
  nfrdMandatory: "NFRD Mandatory",
  assurance: "Assurance",
  assuranceProvider: "Assurance Provider",
  assurancePage: "Assurance Page",
  assuranceReport: "Assurance Report",
  totalRevenue: "Total Revenue",
  totalRevenueQuality: "Total Revenue Quality",
  totalRevenuePage: "Total Revenue Page",
  totalRevenueReport: "Total Revenue Report",
  totalRevenueTagName: "Total Revenue Tag Name",
  totalRevenueComment: "Total Revenue Comment",
  totalCapex: "Total CapEx",
  totalCapexQuality: "Total CapEx Quality",
  totalCapexPage: "Total CapEx Page",
  totalCapexReport: "Total CapEx Report",
  totalCapexTagName: "Total CapEx Tag Name",
  totalCapexComment: "Total CapEx Comment",
  totalOpex: "Total OpEx",
  totalOpexQuality: "Total OpEx Quality",
  totalOpexPage: "Total OpEx Page",
  totalOpexReport: "Total OpEx Report",
  totalOpexTagName: "Total OpEx Tag Name",
  totalOpexComment: "Total OpEx Comment",
  eligibleRevenue: "Eligible Revenue",
  eligibleRevenueQuality: "Eligible Revenue Quality",
  eligibleRevenuePage: "Eligible Revenue Page",
  eligibleRevenueReport: "Eligible Revenue Report",
  eligibleRevenueTagName: "Eligible Revenue Tag Name",
  eligibleRevenueComment: "Eligible Revenue Comment",
  eligibleCapex: "Eligible CapEx",
  eligibleCapexQuality: "Eligible CapEx Quality",
  eligibleCapexPage: "Eligible CapEx Page",
  eligibleCapexReport: "Eligible CapEx Report",
  eligibleCapexTagName: "Eligible CapEx Tag Name",
  eligibleCapexComment: "Eligible CapEx Comment",
  eligibleOpex: "Eligible OpEx",
  eligibleOpexQuality: "Eligible OpEx Quality",
  eligibleOpexPage: "Eligible OpEx Page",
  eligibleOpexReport: "Eligible OpEx Report",
  eligibleOpexTagName: "Eligible OpEx Tag Name",
  eligibleOpexComment: "Eligible OpEx Comment",
  alignedRevenue: "Aligned Revenue",
  alignedRevenueQuality: "Aligned Revenue Quality",
  alignedRevenuePage: "Aligned Revenue Page",
  alignedRevenueReport: "Aligned Revenue Report",
  alignedRevenueTagName: "Aligned Revenue Tag Name",
  alignedRevenueComment: "Aligned Revenue Comment",
  alignedCapex: "Aligned CapEx",
  alignedCapexQuality: "Aligned CapEx Quality",
  alignedCapexPage: "Aligned CapEx Page",
  alignedCapexReport: "Aligned CapEx Report",
  alignedCapexTagName: "Aligned CapEx Tag Name",
  alignedCapexComment: "Aligned CapEx Comment",
  alignedOpex: "Aligned OpEx",
  alignedOpexQuality: "Aligned OpEx Quality",
  alignedOpexPage: "Aligned OpEx Page",
  alignedOpexReport: "Aligned OpEx Report",
  alignedOpexTagName: "Aligned OpEx Tag Name",
  alignedOpexComment: "Aligned OpEx Comment",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitution:
    "Exposures To Taxonomyeligible Economic Activities Credit Institution",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionQuality:
    "Exposures To Taxonomyeligible Economic Activities Credit Institution Quality",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionPage:
    "Exposures To Taxonomyeligible Economic Activities Credit Institution Page",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionReport:
    "Exposures To Taxonomyeligible Economic Activities Credit Institution Report",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionTagName:
    "Exposures To Taxonomyeligible Economic Activities Credit Institution Tag Name",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionComment:
    "Exposures To Taxonomyeligible Economic Activities Credit Institution Comment",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitution:
    "Exposures To Taxonomy Noneligible Economic Activities Credit Institution",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionQuality:
    "Exposures To Taxonomy Noneligible Economic Activities Credit Institution Quality",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionPage:
    "Exposures To Taxonomy Noneligible Economic Activities Credit Institution Page",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionReport:
    "Exposures To Taxonomy Noneligible Economic Activities Credit Institution Report",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionTagName:
    "Exposures To Taxonomy Noneligible Economic Activities Credit Institution Tag Name",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionComment:
    "Exposures To Taxonomy Noneligible Economic Activities Credit Institution Comment",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitution:
    "Exposures To Central Governments Central Banks Supranational Issuers Credit Institution",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionQuality:
    "Exposures To Central Governments Central Banks Supranational Issuers Credit Institution Quality",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionPage:
    "Exposures To Central Governments Central Banks Supranational Issuers Credit Institution Page",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionReport:
    "Exposures To Central Governments Central Banks Supranational Issuers Credit Institution Report",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionTagName:
    "Exposures To Central Governments Central Banks Supranational Issuers Credit Institution Tag Name",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionComment:
    "Exposures To Central Governments Central Banks Supranational Issuers Credit Institution Comment",
  exposuresToDerivativesCreditInstitution: "Exposures To Derivatives Credit Institution",
  exposuresToDerivativesCreditInstitutionQuality: "Exposures To Derivatives Credit Institution Quality",
  exposuresToDerivativesCreditInstitutionPage: "Exposures To Derivatives Credit Institution Page",
  exposuresToDerivativesCreditInstitutionReport: "Exposures To Derivatives Credit Institution Report",
  exposuresToDerivativesCreditInstitutionTagName: "Exposures To Derivatives Credit Institution Tag Name",
  exposuresToDerivativesCreditInstitutionComment: "Exposures To Derivatives Credit Institution Comment",
  exposuresToNonnfrdEntitiesCreditInstitution: "Exposures To NonNFRD Entities Credit Institution",
  exposuresToNonnfrdEntitiesCreditInstitutionQuality: "Exposures To NonNFRD Entities Credit Institution Quality",
  exposuresToNonnfrdEntitiesCreditInstitutionPage: "Exposures To NonNFRD Entities Credit Institution Page",
  exposuresToNonnfrdEntitiesCreditInstitutionReport: "Exposures To NonNFRD Entities Credit Institution Report",
  exposuresToNonnfrdEntitiesCreditInstitutionTagName: "Exposures To NonNFRD Entities Credit Institution Tag Name",
  exposuresToNonnfrdEntitiesCreditInstitutionComment: "Exposures To NonNFRD Entities Credit Institution Comment",
  tradingPortfolio: "Trading Portfolio",
  tradingPortfolioQuality: "Trading Portfolio Quality",
  tradingPortfolioPage: "Trading Portfolio Page",
  tradingPortfolioReport: "Trading Portfolio Report",
  tradingPortfolioTagName: "Trading Portfolio Tag Name",
  tradingPortfolioComment: "Trading Portfolio Comment",
  ondemandInterbankLoans: "Ondemand Interbank Loans",
  ondemandInterbankLoansQuality: "Ondemand Interbank Loans Quality",
  ondemandInterbankLoansPage: "Ondemand Interbank Loans Page",
  ondemandInterbankLoansReport: "Ondemand Interbank Loans Report",
  ondemandInterbankLoansTagName: "Ondemand Interbank Loans Tag Name",
  ondemandInterbankLoansComment: "Ondemand Interbank Loans Comment",
  tradingPortfolioOndemandInterbankLoans: "Trading Portfolio  Ondemand Interbank Loans",
  tradingPortfolioOndemandInterbankLoansQuality: "Trading Portfolio  Ondemand Interbank Loans Quality",
  tradingPortfolioOndemandInterbankLoansPage: "Trading Portfolio  Ondemand Interbank Loans Page",
  tradingPortfolioOndemandInterbankLoansReport: "Trading Portfolio  Ondemand Interbank Loans Report",
  tradingPortfolioOndemandInterbankLoansTagName: "Trading Portfolio  Ondemand Interbank Loans Tag Name",
  tradingPortfolioOndemandInterbankLoansComment: "Trading Portfolio  Ondemand Interbank Loans Comment",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsurance:
    "Exposures To Taxonomyeligible Economic Activities InsuranceReinsurance",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsuranceQuality:
    "Exposures To Taxonomyeligible Economic Activities InsuranceReinsurance Quality",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsurancePage:
    "Exposures To Taxonomyeligible Economic Activities InsuranceReinsurance Page",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsuranceReport:
    "Exposures To Taxonomyeligible Economic Activities InsuranceReinsurance Report",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsuranceTagName:
    "Exposures To Taxonomyeligible Economic Activities InsuranceReinsurance Tag Name",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsuranceComment:
    "Exposures To Taxonomyeligible Economic Activities InsuranceReinsurance Comment",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsurance:
    "Exposures To Taxonomy Noneligible Economic Activities InsuranceReinsurance",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsuranceQuality:
    "Exposures To Taxonomy Noneligible Economic Activities InsuranceReinsurance Quality",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsurancePage:
    "Exposures To Taxonomy Noneligible Economic Activities InsuranceReinsurance Page",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsuranceReport:
    "Exposures To Taxonomy Noneligible Economic Activities InsuranceReinsurance Report",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsuranceTagName:
    "Exposures To Taxonomy Noneligible Economic Activities InsuranceReinsurance Tag Name",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsuranceComment:
    "Exposures To Taxonomy Noneligible Economic Activities InsuranceReinsurance Comment",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsurance:
    "Exposures To Central Governments Central Banks Supranational Issuers InsuranceReinsurance",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsuranceQuality:
    "Exposures To Central Governments Central Banks Supranational Issuers InsuranceReinsurance Quality",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsurancePage:
    "Exposures To Central Governments Central Banks Supranational Issuers InsuranceReinsurance Page",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsuranceReport:
    "Exposures To Central Governments Central Banks Supranational Issuers InsuranceReinsurance Report",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsuranceTagName:
    "Exposures To Central Governments Central Banks Supranational Issuers InsuranceReinsurance Tag Name",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsuranceComment:
    "Exposures To Central Governments Central Banks Supranational Issuers InsuranceReinsurance Comment",
  exposuresToDerivativesInsurancereinsurance: "Exposures To Derivatives InsuranceReinsurance",
  exposuresToDerivativesInsurancereinsuranceQuality: "Exposures To Derivatives InsuranceReinsurance Quality",
  exposuresToDerivativesInsurancereinsurancePage: "Exposures To Derivatives InsuranceReinsurance Page",
  exposuresToDerivativesInsurancereinsuranceReport: "Exposures To Derivatives InsuranceReinsurance Report",
  exposuresToDerivativesInsurancereinsuranceTagName: "Exposures To Derivatives InsuranceReinsurance Tag Name",
  exposuresToDerivativesInsurancereinsuranceComment: "Exposures To Derivatives InsuranceReinsurance Comment",
  exposuresToNonnfrdEntitiesInsurancereinsurance: "Exposures To NonNFRD Entities InsuranceReinsurance",
  exposuresToNonnfrdEntitiesInsurancereinsuranceQuality: "Exposures To NonNFRD Entities InsuranceReinsurance Quality",
  exposuresToNonnfrdEntitiesInsurancereinsurancePage: "Exposures To NonNFRD Entities InsuranceReinsurance Page",
  exposuresToNonnfrdEntitiesInsurancereinsuranceReport: "Exposures To NonNFRD Entities InsuranceReinsurance Report",
  exposuresToNonnfrdEntitiesInsurancereinsuranceTagName: "Exposures To NonNFRD Entities InsuranceReinsurance Tag Name",
  exposuresToNonnfrdEntitiesInsurancereinsuranceComment: "Exposures To NonNFRD Entities InsuranceReinsurance Comment",
  taxonomyeligibleNonlifeInsuranceEconomicActivities: "Taxonomyeligible Nonlife Insurance Economic Activities",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesQuality:
    "Taxonomyeligible Nonlife Insurance Economic Activities Quality",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesPage: "Taxonomyeligible Nonlife Insurance Economic Activities Page",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesReport:
    "Taxonomyeligible Nonlife Insurance Economic Activities Report",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesTagName:
    "Taxonomyeligible Nonlife Insurance Economic Activities Tag Name",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesComment:
    "Taxonomyeligible Nonlife Insurance Economic Activities Comment",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompany:
    "Exposures To Taxonomyeligible Economic Activities Asset Management Company",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyQuality:
    "Exposures To Taxonomyeligible Economic Activities Asset Management Company Quality",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyPage:
    "Exposures To Taxonomyeligible Economic Activities Asset Management Company Page",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyReport:
    "Exposures To Taxonomyeligible Economic Activities Asset Management Company Report",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyTagName:
    "Exposures To Taxonomyeligible Economic Activities Asset Management Company Tag Name",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyComment:
    "Exposures To Taxonomyeligible Economic Activities Asset Management Company Comment",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompany:
    "Exposures To Taxonomy Noneligible Economic Activities Asset Management Company",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyQuality:
    "Exposures To Taxonomy Noneligible Economic Activities Asset Management Company Quality",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyPage:
    "Exposures To Taxonomy Noneligible Economic Activities Asset Management Company Page",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyReport:
    "Exposures To Taxonomy Noneligible Economic Activities Asset Management Company Report",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyTagName:
    "Exposures To Taxonomy Noneligible Economic Activities Asset Management Company Tag Name",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyComment:
    "Exposures To Taxonomy Noneligible Economic Activities Asset Management Company Comment",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompany:
    "Exposures To Central Governments Central Banks Supranational Issuers Asset Management Company",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyQuality:
    "Exposures To Central Governments Central Banks Supranational Issuers Asset Management Company Quality",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyPage:
    "Exposures To Central Governments Central Banks Supranational Issuers Asset Management Company Page",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyReport:
    "Exposures To Central Governments Central Banks Supranational Issuers Asset Management Company Report",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyTagName:
    "Exposures To Central Governments Central Banks Supranational Issuers Asset Management Company Tag Name",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyComment:
    "Exposures To Central Governments Central Banks Supranational Issuers Asset Management Company Comment",
  exposuresToDerivativesAssetManagementCompany: "Exposures To Derivatives Asset Management Company",
  exposuresToDerivativesAssetManagementCompanyQuality: "Exposures To Derivatives Asset Management Company Quality",
  exposuresToDerivativesAssetManagementCompanyPage: "Exposures To Derivatives Asset Management Company Page",
  exposuresToDerivativesAssetManagementCompanyReport: "Exposures To Derivatives Asset Management Company Report",
  exposuresToDerivativesAssetManagementCompanyTagName: "Exposures To Derivatives Asset Management Company Tag Name",
  exposuresToDerivativesAssetManagementCompanyComment: "Exposures To Derivatives Asset Management Company Comment",
  exposuresToNonnfrdEntitiesAssetManagementCompany: "Exposures To NonNFRD Entities Asset Management Company",
  exposuresToNonnfrdEntitiesAssetManagementCompanyQuality:
    "Exposures To NonNFRD Entities Asset Management Company Quality",
  exposuresToNonnfrdEntitiesAssetManagementCompanyPage: "Exposures To NonNFRD Entities Asset Management Company Page",
  exposuresToNonnfrdEntitiesAssetManagementCompanyReport:
    "Exposures To NonNFRD Entities Asset Management Company Report",
  exposuresToNonnfrdEntitiesAssetManagementCompanyTagName:
    "Exposures To NonNFRD Entities Asset Management Company Tag Name",
  exposuresToNonnfrdEntitiesAssetManagementCompanyComment:
    "Exposures To NonNFRD Entities Asset Management Company Comment",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirm:
    "Exposures To Taxonomyeligible Economic Activities Investment Firm",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmQuality:
    "Exposures To Taxonomyeligible Economic Activities Investment Firm Quality",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmPage:
    "Exposures To Taxonomyeligible Economic Activities Investment Firm Page",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmReport:
    "Exposures To Taxonomyeligible Economic Activities Investment Firm Report",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmTagName:
    "Exposures To Taxonomyeligible Economic Activities Investment Firm Tag Name",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmComment:
    "Exposures To Taxonomyeligible Economic Activities Investment Firm Comment",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirm:
    "Exposures To Taxonomy Noneligible Economic Activities Investment Firm",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmQuality:
    "Exposures To Taxonomy Noneligible Economic Activities Investment Firm Quality",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmPage:
    "Exposures To Taxonomy Noneligible Economic Activities Investment Firm Page",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmReport:
    "Exposures To Taxonomy Noneligible Economic Activities Investment Firm Report",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmTagName:
    "Exposures To Taxonomy Noneligible Economic Activities Investment Firm Tag Name",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmComment:
    "Exposures To Taxonomy Noneligible Economic Activities Investment Firm Comment",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirm:
    "Exposures To Central Governments Central Banks Supranational Issuers Investment Firm",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmQuality:
    "Exposures To Central Governments Central Banks Supranational Issuers Investment Firm Quality",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmPage:
    "Exposures To Central Governments Central Banks Supranational Issuers Investment Firm Page",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmReport:
    "Exposures To Central Governments Central Banks Supranational Issuers Investment Firm Report",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmTagName:
    "Exposures To Central Governments Central Banks Supranational Issuers Investment Firm Tag Name",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmComment:
    "Exposures To Central Governments Central Banks Supranational Issuers Investment Firm Comment",
  exposuresToDerivativesInvestmentFirm: "Exposures To Derivatives Investment Firm",
  exposuresToDerivativesInvestmentFirmQuality: "Exposures To Derivatives Investment Firm Quality",
  exposuresToDerivativesInvestmentFirmPage: "Exposures To Derivatives Investment Firm Page",
  exposuresToDerivativesInvestmentFirmReport: "Exposures To Derivatives Investment Firm Report",
  exposuresToDerivativesInvestmentFirmTagName: "Exposures To Derivatives Investment Firm Tag Name",
  exposuresToDerivativesInvestmentFirmComment: "Exposures To Derivatives Investment Firm Comment",
  exposuresToNonnfrdEntitiesInvestmentFirm: "Exposures To NonNFRD Entities Investment Firm",
  exposuresToNonnfrdEntitiesInvestmentFirmQuality: "Exposures To NonNFRD Entities Investment Firm Quality",
  exposuresToNonnfrdEntitiesInvestmentFirmPage: "Exposures To NonNFRD Entities Investment Firm Page",
  exposuresToNonnfrdEntitiesInvestmentFirmReport: "Exposures To NonNFRD Entities Investment Firm Report",
  exposuresToNonnfrdEntitiesInvestmentFirmTagName: "Exposures To NonNFRD Entities Investment Firm Tag Name",
  exposuresToNonnfrdEntitiesInvestmentFirmComment: "Exposures To NonNFRD Entities Investment Firm Comment",
  greenAssetRatioCreditInstitution: "Green Asset Ratio Credit Institution",
  greenAssetRatioQualityCreditInstitution: "Green Asset Ratio Quality Credit Institution",
  greenAssetRatioPageCreditInstitution: "Green Asset Ratio Page Credit Institution",
  greenAssetRatioReportCreditInstitution: "Green Asset Ratio Report Credit Institution",
  greenAssetRatioCreditInstitutionTagName: "Green Asset Ratio Credit Institution Tag Name",
  greenAssetRatioCommentCreditInstitution: "Green Asset Ratio Comment Credit Institution",
  greenAssetRatioInvestmentFirm: "Green Asset Ratio Investment Firm",
  greenAssetRatioQualityInvestmentFirm: "Green Asset Ratio Quality Investment Firm",
  greenAssetRatioPageInvestmentFirm: "Green Asset Ratio Page Investment Firm",
  greenAssetRatioReportInvestmentFirm: "Green Asset Ratio Report Investment Firm",
  greenAssetRatioInvestmentFirmTagName: "Green Asset Ratio Investment Firm Tag Name",
  greenAssetRatioCommentInvestmentFirm: "Green Asset Ratio Comment Investment Firm",

  activityLevelReporting: "EU Taxonomy Activity Level Reporting",
  fiscalYearDeviation: "Fiscal Year is deviating",
  currency: "Currency used in the report",

  quality: "Quality",
  page: "Page",
  report: "Report",
  tagName: "Tag Name",
  comment: "Comment",

  provider: "Assurance Provider",
  AssurancePage: "Assurance Page",
  AssuranceReport: "Assurance Report",
  eligibleCapEx: "Eligible CapEx",
  alignedCapEx: "Aligned CapEx",
  totalCapEx: "Total CapEx",
  eligibleOpEx: "Eligible OpEx",
  alignedOpEx: "Aligned OpEx",
  totalOpEx: "Total OpEx",

  ExposuresToTaxonomyEligibleEconomicActivitiesCreditInstitution:
    "Exposures To Taxonomy-eligible Economic Activities Credit Institution",
  ExposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitution:
    "Exposures To Taxonomy Non-eligible Economic Activities Credit Institution",
  ExposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitution:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Credit Institution",
  ExposuresToDerivativesCreditInstitution: "Exposures To Derivatives Credit Institution",
  ExposuresToNonNfdrEntitiesCreditInstitution: "Exposures To Non-NFRD Entities Credit Institution",
  interbankLoans: "Interbank Loans",
  tradingPortfolioAndInterbankLoans: "Trading Portfolio & Interbank Loans",
  ExposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsurance:
    "Exposures To Taxonomy-eligible Economic Activities Insurance/Reinsurance",
  ExposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsurance:
    "Exposures To Taxonomy Non-eligible Economic Activities Insurance/Reinsurance",
  ExposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsurance:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Insurance/Reinsurance",
  ExposuresToDerivativesInsuranceReinsurance: "Exposures To Derivatives Insurance/Reinsurance",
  ExposuresToNonNfrdEntitiesInsuranceReinsurance: "Exposures To Non-NFRD Entities Insurance/Reinsurance",
  taxonomyEligibleNonLifeInsuranceActivities: "Taxonomy-eligible Non-life Insurance Economic Activities",
  ExposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompany:
    "Exposures To Taxonomy-eligible Economic Activities Asset Management Company",
  ExposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompany:
    "Exposures To Taxonomy Non-eligible Economic Activities Asset Management Company",
  ExposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompany:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Asset Management Company",
  ExposuresToDerivativesAssetManagementCompany: "Exposures To Derivatives Asset Management Company",
  ExposuresToNonNfdrEntitiesAssetManagementCompany: "Exposures To Non-NFRD Entities Asset Management Company",
  ExposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirm:
    "Exposures To Taxonomy-eligible Economic Activities Investment Firm",
  ExposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirm:
    "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm",
  ExposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirm:
    "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm",
  ExposuresToDerivativesInvestmentFirm: "Exposures To Derivatives Investment Firm",
  ExposuresToNonNfrdEntitiesInvestmentFirm: "Exposures To Non-NFRD Entities Investment Firm",
  greenAssetRatio: "Green Asset Ratio",
};

export const euTaxonomyKpiInfoMappings = {
  financialServicesTypes: "Financial Services Types",
  reportingObligation: "NFRD Mandatory",
  investmentNonNfrd: "Investment Non NFRD",
  banksAndIssuers: "Banks and Issuers",
  derivatives: "For financial companies (FS), the percentage of total assets exposed to derivatives",
  taxonomyNonEligibleActivity:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  taxonomyEligibleActivity:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  reportDate: "Report Date",
  reportingPeriod: "Reporting Period",
  totalAmount: "totalAmount",
  alignedPercentage: "Aligned Percentage",
  eligiblePercentage: "Eligible Percentage",

  isfs: "Distinguishes between non-financial companies (IS) and financial companies (FS). There are two possible values expected: 1- IS, 2- FS",
  fsCompanyType:
    "Defines the type of financial company. Credit Institution (1), Insurance/Reinsurance (2), Asset Manager (3), Investment Firm (4)",
  fiscalYear: "Fiscal Year (Deviation/ No Deviation)",
  fiscalYearEnd: "The date the fiscal year ends",
  annualReport: "Link to Annual Report",
  groupLevelAnnualReport: "Is Annual Report on a Group level",
  annualReportDate: "The date for which the information presented in the report is valid through",
  annualReportCurrency: "The 3-letter alpha code that represents the currency used in the report",
  sustainabilityReport: "Link to Sustainability Report",
  groupLevelSustainabilityReport: "Is Sustainability Report on a Group level",
  sustainabilityReportDate: "The date for which the information presented in the report is valid through",
  sustainabilityReportCurrency: "The 3-letter alpha code that represents the currency used in the report",
  integratedReport: "Link to Integrated Report",
  groupLevelIntegratedReport: "Is Integrated Report on a Group level",
  integratedReportDate: "The date for which the information presented in the report is valid through",
  integratedReportCurrency: "The 3-letter alpha code that represents the currency used in the report",
  esefReport: "Link to ESEF Report",
  groupLevelEsefReport: "Is ESEF Report on a Group level",
  esefReportDate: "The date for which the information presented in the report is valid through",
  esefReportCurrency: "The 3-letter alpha code that represents the currency used in the report",
  scopeOfEntities:
    "Does a list of legal entities covered by Sust./Annual/Integrated/ESEF report match with a list of legal entities covered by Audited Consolidated Financial Statement ",
  euTaxonomyActivityLevelReporting: "Activity Level disclosure",
  numberOfEmployees: "Total number of employees (including temporary workers)",
  nfrdMandatory: "The reporting obligation for companies whose number of employees is greater or equal to 500",
  assurance: "Level of Assurance of the EU Taxonomy disclosure (Reasonable Assurance, Limited Assurance, None)",
  assuranceProvider: "Name of the Audit company which provide assurance to EU Taxonomy data points ",
  assurancePage:
    "The page number from where the text which proof that EU Taxonomy disclosures included in Assurance statement was sourced",
  assuranceReport: "Link to Assurance Report",
  totalRevenue:
    "Total Revenue in MM (Millions) for the financial year. i.e., Income arising in the course of an entity's ordinary activities., the amounts derived from the sale of products and the provision of services after deducting sales rebates and value added tax and other taxes directly linked to turnover. Overall turnover is equivalent to a firm's total revenues over some period of time",
  totalRevenueQuality: "The level of confidence associated to the value",
  totalRevenuePage: "The page number of the document from where the information was sourced",
  totalRevenueReport: "The report from where the information was sourced",
  totalRevenueTagName: "The name of the tag where the information is in the pdf",
  totalRevenueComment: "Free optional text",
  totalCapex:
    "Total CapEx in MM (Millions) for the financial year. A capital expenditure (CapEx) is a payment for goods or services recorded, or capitalized, on the balance sheet instead of expensed on the income statement",
  totalCapexQuality: "The level of confidence associated to the value",
  totalCapexPage: "The page number of the document from where the information was sourced",
  totalCapexReport: "The report from where the information was sourced",
  totalCapexTagName: "The name of the tag where the information is in the pdf",
  totalCapexComment: "Free optional text",
  totalOpex:
    "Total OpEx in MM (Millions) for the financial year. Operating expenses (OpEx) are shorter term expenses required to meet the ongoing operational costs of running a business",
  totalOpexQuality: "The level of confidence associated to the value",
  totalOpexPage: "The page number of the document from where the information was sourced",
  totalOpexReport: "The report from where the information was sourced",
  totalOpexTagName: "The name of the tag where the information is in the pdf",
  totalOpexComment: "Free optional text",
  eligibleRevenue:
    "Percentage of the Revenue where the economic activity meets taxonomy criteria for substantial contribution to climate change mitigation and does no serious harm to the other environmental objectives (DNSH criteria)",
  eligibleRevenueQuality: "The level of confidence associated to the value",
  eligibleRevenuePage: "The page number of the document from where the information was sourced",
  eligibleRevenueReport: "The report from where the information was sourced",
  eligibleRevenueTagName: "The name of the tag where the information is in the pdf",
  eligibleRevenueComment: "Free optional text",
  eligibleCapex:
    "Percentage of the CapEx that is part of a plan to meet taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
  eligibleCapexQuality: "The level of confidence associated to the value",
  eligibleCapexPage: "The page number of the document from where the information was sourced",
  eligibleCapexReport: "The report from where the information was sourced",
  eligibleCapexTagName: "The name of the tag where the information is in the pdf",
  eligibleCapexComment: "Free optional text",
  eligibleOpex:
    "Percentage of the OpEx that is part of a plan to meet taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
  eligibleOpexQuality: "The level of confidence associated to the value",
  eligibleOpexPage: "The page number of the document from where the information was sourced",
  eligibleOpexReport: "The report from where the information was sourced",
  eligibleOpexTagName: "The name of the tag where the information is in the pdf",
  eligibleOpexComment: "Free optional text",
  alignedRevenue:
    "Percentage of the Revenue that is taxonomy-aligned, i.e., generated by an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  alignedRevenueQuality: "The level of confidence associated to the value",
  alignedRevenuePage: "The page number of the document from where the information was sourced",
  alignedRevenueReport: "The report from where the information was sourced",
  alignedRevenueTagName: "The name of the tag where the information is in the pdf",
  alignedRevenueComment: "Free optional text",
  alignedCapex:
    "Percentage of the CapEx that is either already taxonomy-aligned or is part of a credible plan to extend or reach taxonomy alignment. i.e., an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  alignedCapexQuality: "The level of confidence associated to the value",
  alignedCapexPage: "The page number of the document from where the information was sourced",
  alignedCapexReport: "The report from where the information was sourced",
  alignedCapexTagName: "The name of the tag where the information is in the pdf",
  alignedCapexComment: "Free optional text",
  alignedOpex:
    "Percentage of the OpEx that is associated with taxonomy-aligned activities. i.e., for an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
  alignedOpexQuality: "The level of confidence associated to the value",
  alignedOpexPage: "The page number of the document from where the information was sourced",
  alignedOpexReport: "The report from where the information was sourced",
  alignedOpexTagName: "The name of the tag where the information is in the pdf",
  alignedOpexComment: "Free optional text",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyeligibleEconomicActivitiesCreditInstitutionComment: "Free optional text",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyNoneligibleEconomicActivitiesCreditInstitutionComment: "Free optional text",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionQuality:
    "The level of confidence associated to the value",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionPage:
    "The page number of the document from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionReport:
    "The report from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionComment: "Free optional text",
  exposuresToDerivativesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  exposuresToDerivativesCreditInstitutionQuality: "The level of confidence associated to the value",
  exposuresToDerivativesCreditInstitutionPage: "The page number of the document from where the information was sourced",
  exposuresToDerivativesCreditInstitutionReport: "The report from where the information was sourced",
  exposuresToDerivativesCreditInstitutionTagName: "The name of the tag where the information is in the pdf",
  exposuresToDerivativesCreditInstitutionComment: "Free optional text",
  exposuresToNonnfrdEntitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  exposuresToNonnfrdEntitiesCreditInstitutionQuality: "The level of confidence associated to the value",
  exposuresToNonnfrdEntitiesCreditInstitutionPage:
    "The page number of the document from where the information was sourced",
  exposuresToNonnfrdEntitiesCreditInstitutionReport: "The report from where the information was sourced",
  exposuresToNonnfrdEntitiesCreditInstitutionTagName: "The name of the tag where the information is in the pdf",
  exposuresToNonnfrdEntitiesCreditInstitutionComment: "Free optional text",
  tradingPortfolio: "For Credit Institutions, the trading portfolio as a percentage of total assets",
  tradingPortfolioQuality: "The level of confidence associated to the value",
  tradingPortfolioPage: "The page number of the document from where the information was sourced",
  tradingPortfolioReport: "The report from where the information was sourced",
  tradingPortfolioTagName: "The name of the tag where the information is in the pdf",
  tradingPortfolioComment: "Free optional text",
  ondemandInterbankLoans: "For Credit Institutions, the on demand interbank loans as a percentage of total assets",
  ondemandInterbankLoansQuality: "The level of confidence associated to the value",
  ondemandInterbankLoansPage: "The page number of the document from where the information was sourced",
  ondemandInterbankLoansReport: "The report from where the information was sourced",
  ondemandInterbankLoansTagName: "The name of the tag where the information is in the pdf",
  ondemandInterbankLoansComment: "Free optional text",
  tradingPortfolioOndemandInterbankLoans:
    "For Credit Institutions, the trading portfolio and the on demand interbank loans as a percentage of total assets",
  tradingPortfolioOndemandInterbankLoansQuality: "The level of confidence associated to the value",
  tradingPortfolioOndemandInterbankLoansPage: "The page number of the document from where the information was sourced",
  tradingPortfolioOndemandInterbankLoansReport: "The report from where the information was sourced",
  tradingPortfolioOndemandInterbankLoansTagName: "The name of the tag where the information is in the pdf",
  tradingPortfolioOndemandInterbankLoansComment: "Free optional text",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsurance:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsuranceQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsuranceReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsuranceTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyeligibleEconomicActivitiesInsurancereinsuranceComment: "Free optional text",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsurance:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsuranceQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsuranceReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsuranceTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInsurancereinsuranceComment: "Free optional text",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsurance:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsuranceQuality:
    "The level of confidence associated to the value",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsuranceReport:
    "The report from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsuranceTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsurancereinsuranceComment: "Free optional text",
  exposuresToDerivativesInsurancereinsurance:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  exposuresToDerivativesInsurancereinsuranceQuality: "The level of confidence associated to the value",
  exposuresToDerivativesInsurancereinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToDerivativesInsurancereinsuranceReport: "The report from where the information was sourced",
  exposuresToDerivativesInsurancereinsuranceTagName: "The name of the tag where the information is in the pdf",
  exposuresToDerivativesInsurancereinsuranceComment: "Free optional text",
  exposuresToNonnfrdEntitiesInsurancereinsurance:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  exposuresToNonnfrdEntitiesInsurancereinsuranceQuality: "The level of confidence associated to the value",
  exposuresToNonnfrdEntitiesInsurancereinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToNonnfrdEntitiesInsurancereinsuranceReport: "The report from where the information was sourced",
  exposuresToNonnfrdEntitiesInsurancereinsuranceTagName: "The name of the tag where the information is in the pdf",
  exposuresToNonnfrdEntitiesInsurancereinsuranceComment: "Free optional text",
  taxonomyeligibleNonlifeInsuranceEconomicActivities:
    "For Insurance/Reinsurance companies, the percentage of Taxonomy-eligible non-life insurance economics activities. Insurance and reinsurance undertakings other than life insurance undertakings shall calculate the KPI related to underwriting activities and present the ‘gross premiums written’ non-life insurance revenue or, as applicable, reinsurance revenue corresponding to Taxonomy-aligned insurance or reinsurance activities",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesQuality: "The level of confidence associated to the value",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesPage:
    "The page number of the document from where the information was sourced",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesReport: "The report from where the information was sourced",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesTagName: "The name of the tag where the information is in the pdf",
  taxonomyeligibleNonlifeInsuranceEconomicActivitiesComment: "Free optional text",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyeligibleEconomicActivitiesAssetManagementCompanyComment: "Free optional text",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyNoneligibleEconomicActivitiesAssetManagementCompanyComment: "Free optional text",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyQuality:
    "The level of confidence associated to the value",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyPage:
    "The page number of the document from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyReport:
    "The report from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyComment: "Free optional text",
  exposuresToDerivativesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  exposuresToDerivativesAssetManagementCompanyQuality: "The level of confidence associated to the value",
  exposuresToDerivativesAssetManagementCompanyPage:
    "The page number of the document from where the information was sourced",
  exposuresToDerivativesAssetManagementCompanyReport: "The report from where the information was sourced",
  exposuresToDerivativesAssetManagementCompanyTagName: "The name of the tag where the information is in the pdf",
  exposuresToDerivativesAssetManagementCompanyComment: "Free optional text",
  exposuresToNonnfrdEntitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  exposuresToNonnfrdEntitiesAssetManagementCompanyQuality: "The level of confidence associated to the value",
  exposuresToNonnfrdEntitiesAssetManagementCompanyPage:
    "The page number of the document from where the information was sourced",
  exposuresToNonnfrdEntitiesAssetManagementCompanyReport: "The report from where the information was sourced",
  exposuresToNonnfrdEntitiesAssetManagementCompanyTagName: "The name of the tag where the information is in the pdf",
  exposuresToNonnfrdEntitiesAssetManagementCompanyComment: "Free optional text",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmQuality: "The level of confidence associated to the value",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyeligibleEconomicActivitiesInvestmentFirmComment: "Free optional text",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyNoneligibleEconomicActivitiesInvestmentFirmComment: "Free optional text",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmQuality:
    "The level of confidence associated to the value",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmPage:
    "The page number of the document from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmReport:
    "The report from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmComment: "Free optional text",
  exposuresToDerivativesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  exposuresToDerivativesInvestmentFirmQuality: "The level of confidence associated to the value",
  exposuresToDerivativesInvestmentFirmPage: "The page number of the document from where the information was sourced",
  exposuresToDerivativesInvestmentFirmReport: "The report from where the information was sourced",
  exposuresToDerivativesInvestmentFirmTagName: "The name of the tag where the information is in the pdf",
  exposuresToDerivativesInvestmentFirmComment: "Free optional text",
  exposuresToNonnfrdEntitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  exposuresToNonnfrdEntitiesInvestmentFirmQuality: "The level of confidence associated to the value",
  exposuresToNonnfrdEntitiesInvestmentFirmPage:
    "The page number of the document from where the information was sourced",
  exposuresToNonnfrdEntitiesInvestmentFirmReport: "The report from where the information was sourced",
  exposuresToNonnfrdEntitiesInvestmentFirmTagName: "The name of the tag where the information is in the pdf",
  exposuresToNonnfrdEntitiesInvestmentFirmComment: "Free optional text",
  greenAssetRatioCreditInstitution:
    "The proportion of the of credit institution’s assets financing and invested in taxonomy-aligned economic activities as a proportion of total covered assets",
  greenAssetRatioQualityCreditInstitution: "The level of confidence associated to the value",
  greenAssetRatioPageCreditInstitution: "The page number of the document from where the information was sourced",
  greenAssetRatioReportCreditInstitution: "The report from where the information was sourced",
  greenAssetRatioCreditInstitutionTagName: "The name of the tag where the information is in the pdf",
  greenAssetRatioCommentCreditInstitution: "Free optional text",
  greenAssetRatioInvestmentFirm:
    "The proportion of the of credit institution’s assets financing and invested in taxonomy-aligned economic activities as a proportion of total covered assets",
  greenAssetRatioQualityInvestmentFirm: "The level of confidence associated to the value",
  greenAssetRatioPageInvestmentFirm: "The page number of the document from where the information was sourced",
  greenAssetRatioReportInvestmentFirm: "The report from where the information was sourced",
  greenAssetRatioInvestmentFirmTagName: "The name of the tag where the information is in the pdf",
  greenAssetRatioCommentInvestmentFirm: "Free optional text",
  currency: "The 3-letter alpha code that represents the currency used in the report",

  activityLevelReporting: "EU Taxonomy Activity Level Reporting",
  fiscalYearDeviation: "Is Fiscal Year deviating?",

  quality: "The level of confidence associated to the value",
  page: "The page number of the document from where the information was sourced",
  report: "The report from where the information was sourced",
  tagName: "The name of the tag where the information is in the pdf",
  comment: "Free optional text",

  provider: "Name of the Audit company which provide assurance to EU Taxonomy data points ",
  AssurancePage:
    "The page number from where the text which proof that EU Taxonomy disclosures included in Assurance statement was sourced",
  AssuranceReport: "Link to Assurance Report",
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

  ExposuresToTaxonomyEligibleEconomicActivitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  ExposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  ExposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  ExposuresToDerivativesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  ExposuresToNonNfdrEntitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  interbankLoans: "For Credit Institutions, the on demand interbank loans as a percentage of total assets",
  tradingPortfolioAndInterbankLoans:
    "For Credit Institutions, the trading portfolio and the on demand interbank loans as a percentage of total assets",

  ExposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  ExposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  ExposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  ExposuresToDerivativesInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  ExposuresToNonNfrdEntitiesInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  taxonomyEligibleNonLifeInsuranceActivities:
    "For Insurance/Reinsurance companies, the percentage of Taxonomy-eligible non-life insurance economics activities. Insurance and reinsurance undertakings other than life insurance undertakings shall calculate the KPI related to underwriting activities and present the ‘gross premiums written’ non-life insurance revenue or, as applicable, reinsurance revenue corresponding to Taxonomy-aligned insurance or reinsurance activities",
  ExposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  ExposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  ExposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  ExposuresToDerivativesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  ExposuresToNonNfdrEntitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",

  ExposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  ExposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  ExposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  ExposuresToDerivativesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  ExposuresToNonNfrdEntitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  greenAssetRatio:
    "The proportion of the of credit institution’s assets financing and invested in taxonomy-aligned economic activities as a proportion of total covered assets",
};
