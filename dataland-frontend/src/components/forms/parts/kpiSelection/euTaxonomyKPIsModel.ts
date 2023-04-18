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
  // These are not in the data dictionary
  financialServicesTypes: "Financial Services Types",
  investmentNonNfrd: "Investment Non NFRD",
  banksAndIssuers: "Banks and Issuers",
  derivatives: "Exposures To Derivatives",
  taxonomyNonEligibleActivity: "Taxonomy non Eligible Activity",
  taxonomyEligibleActivity: "Taxonomy Eligible Activity",
  reportDate: "Report Date",
  reportingPeriod: "Reporting Period",
  reportingObligation: "NFRD Mandatory",
  provider: "Assurance Provider",
  greenAssetRatio: "Green Asset Ratio",
  totalAmount: "Total Amount",
  alignedPercentage: "Aligned Percentage",
  eligiblePercentage: "Eligible Percentage",
  activityLevelReporting: "EU Taxonomy Activity Level Reporting",
  fiscalYearDeviation: "Fiscal Year is deviating",
  currency: "Currency used in the report",
  quality: "Quality",
  page: "Page",
  report: "Report",
  tagName: "Tag Name",
  comment: "Comment",

  //These are generated from the data dictionary
  isFs: "IS/FS",
  fsCompanyType: "FS - Company Type",
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
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitution:
    "Exposures To Taxonomy-eligible Economic Activities Credit Institution",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionQuality:
    "Exposures To Taxonomy-eligible Economic Activities Credit Institution Quality",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionPage:
    "Exposures To Taxonomy-eligible Economic Activities Credit Institution Page",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionReport:
    "Exposures To Taxonomy-eligible Economic Activities Credit Institution Report",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionTagName:
    "Exposures To Taxonomy-eligible Economic Activities Credit Institution Tag Name",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionComment:
    "Exposures To Taxonomy-eligible Economic Activities Credit Institution Comment",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitution:
    "Exposures To Taxonomy Non-eligible Economic Activities Credit Institution",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionQuality:
    "Exposures To Taxonomy Non-eligible Economic Activities Credit Institution Quality",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionPage:
    "Exposures To Taxonomy Non-eligible Economic Activities Credit Institution Page",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionReport:
    "Exposures To Taxonomy Non-eligible Economic Activities Credit Institution Report",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionTagName:
    "Exposures To Taxonomy Non-eligible Economic Activities Credit Institution Tag Name",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionComment:
    "Exposures To Taxonomy Non-eligible Economic Activities Credit Institution Comment",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitution:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Credit Institution",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionQuality:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Credit Institution Quality",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionPage:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Credit Institution Page",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionReport:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Credit Institution Report",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionTagName:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Credit Institution Tag Name",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitutionComment:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Credit Institution Comment",
  exposuresToDerivativesCreditInstitution: "Exposures To Derivatives Credit Institution",
  exposuresToDerivativesCreditInstitutionQuality: "Exposures To Derivatives Credit Institution Quality",
  exposuresToDerivativesCreditInstitutionPage: "Exposures To Derivatives Credit Institution Page",
  exposuresToDerivativesCreditInstitutionReport: "Exposures To Derivatives Credit Institution Report",
  exposuresToDerivativesCreditInstitutionTagName: "Exposures To Derivatives Credit Institution Tag Name",
  exposuresToDerivativesCreditInstitutionComment: "Exposures To Derivatives Credit Institution Comment",
  exposuresToNonNfrdEntitiesCreditInstitution: "Exposures To Non-NFRD Entities Credit Institution",
  exposuresToNonNfrdEntitiesCreditInstitutionQuality: "Exposures To Non-NFRD Entities Credit Institution Quality",
  exposuresToNonNfrdEntitiesCreditInstitutionPage: "Exposures To Non-NFRD Entities Credit Institution Page",
  exposuresToNonNfrdEntitiesCreditInstitutionReport: "Exposures To Non-NFRD Entities Credit Institution Report",
  exposuresToNonNfrdEntitiesCreditInstitutionTagName: "Exposures To Non-NFRD Entities Credit Institution Tag Name",
  exposuresToNonNfrdEntitiesCreditInstitutionComment: "Exposures To Non-NFRD Entities Credit Institution Comment",
  tradingPortfolio: "Trading Portfolio",
  tradingPortfolioQuality: "Trading Portfolio Quality",
  tradingPortfolioPage: "Trading Portfolio Page",
  tradingPortfolioReport: "Trading Portfolio Report",
  tradingPortfolioTagName: "Trading Portfolio Tag Name",
  tradingPortfolioComment: "Trading Portfolio Comment",
  onDemandInterbankLoans: "On-demand Interbank Loans",
  onDemandInterbankLoansQuality: "On-demand Interbank Loans Quality",
  onDemandInterbankLoansPage: "On-demand Interbank Loans Page",
  onDemandInterbankLoansReport: "On-demand Interbank Loans Report",
  onDemandInterbankLoansTagName: "On-demand Interbank Loans Tag Name",
  onDemandInterbankLoansComment: "On-demand Interbank Loans Comment",
  tradingPortfolioOnDemandInterbankLoans: "Trading Portfolio & On-demand Interbank Loans",
  tradingPortfolioOnDemandInterbankLoansQuality: "Trading Portfolio & On-demand Interbank Loans Quality",
  tradingPortfolioOnDemandInterbankLoansPage: "Trading Portfolio & On-demand Interbank Loans Page",
  tradingPortfolioOnDemandInterbankLoansReport: "Trading Portfolio & On-demand Interbank Loans Report",
  tradingPortfolioOnDemandInterbankLoansTagName: "Trading Portfolio & On-demand Interbank Loans Tag Name",
  tradingPortfolioOnDemandInterbankLoansComment: "Trading Portfolio & On-demand Interbank Loans Comment",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsurance:
    "Exposures To Taxonomy-eligible Economic Activities Insurance/Reinsurance",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsuranceQuality:
    "Exposures To Taxonomy-eligible Economic Activities Insurance/Reinsurance Quality",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsurancePage:
    "Exposures To Taxonomy-eligible Economic Activities Insurance/Reinsurance Page",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsuranceReport:
    "Exposures To Taxonomy-eligible Economic Activities Insurance/Reinsurance Report",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsuranceTagName:
    "Exposures To Taxonomy-eligible Economic Activities Insurance/Reinsurance Tag Name",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsuranceComment:
    "Exposures To Taxonomy-eligible Economic Activities Insurance/Reinsurance Comment",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsurance:
    "Exposures To Taxonomy Non-eligible Economic Activities Insurance/Reinsurance",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsuranceQuality:
    "Exposures To Taxonomy Non-eligible Economic Activities Insurance/Reinsurance Quality",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsurancePage:
    "Exposures To Taxonomy Non-eligible Economic Activities Insurance/Reinsurance Page",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsuranceReport:
    "Exposures To Taxonomy Non-eligible Economic Activities Insurance/Reinsurance Report",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsuranceTagName:
    "Exposures To Taxonomy Non-eligible Economic Activities Insurance/Reinsurance Tag Name",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsuranceComment:
    "Exposures To Taxonomy Non-eligible Economic Activities Insurance/Reinsurance Comment",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsurance:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Insurance/Reinsurance",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsuranceQuality:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Insurance/Reinsurance Quality",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsurancePage:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Insurance/Reinsurance Page",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsuranceReport:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Insurance/Reinsurance Report",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsuranceTagName:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Insurance/Reinsurance Tag Name",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsuranceComment:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Insurance/Reinsurance Comment",
  exposuresToDerivativesInsuranceReinsurance: "Exposures To Derivatives Insurance/Reinsurance",
  exposuresToDerivativesInsuranceReinsuranceQuality: "Exposures To Derivatives Insurance/Reinsurance Quality",
  exposuresToDerivativesInsuranceReinsurancePage: "Exposures To Derivatives Insurance/Reinsurance Page",
  exposuresToDerivativesInsuranceReinsuranceReport: "Exposures To Derivatives Insurance/Reinsurance Report",
  exposuresToDerivativesInsuranceReinsuranceTagName: "Exposures To Derivatives Insurance/Reinsurance Tag Name",
  exposuresToDerivativesInsuranceReinsuranceComment: "Exposures To Derivatives Insurance/Reinsurance Comment",
  exposuresToNonNfrdEntitiesInsuranceReinsurance: "Exposures To Non-NFRD Entities Insurance/Reinsurance",
  exposuresToNonNfrdEntitiesInsuranceReinsuranceQuality: "Exposures To Non-NFRD Entities Insurance/Reinsurance Quality",
  exposuresToNonNfrdEntitiesInsuranceReinsurancePage: "Exposures To Non-NFRD Entities Insurance/Reinsurance Page",
  exposuresToNonNfrdEntitiesInsuranceReinsuranceReport: "Exposures To Non-NFRD Entities Insurance/Reinsurance Report",
  exposuresToNonNfrdEntitiesInsuranceReinsuranceTagName:
    "Exposures To Non-NFRD Entities Insurance/Reinsurance Tag Name",
  exposuresToNonNfrdEntitiesInsuranceReinsuranceComment: "Exposures To Non-NFRD Entities Insurance/Reinsurance Comment",
  taxonomyEligibleNonLifeInsuranceEconomicActivities: "Taxonomy-eligible Non-life Insurance Economic Activities",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesQuality:
    "Taxonomy-eligible Non-life Insurance Economic Activities Quality",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesPage:
    "Taxonomy-eligible Non-life Insurance Economic Activities Page",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesReport:
    "Taxonomy-eligible Non-life Insurance Economic Activities Report",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesTagName:
    "Taxonomy-eligible Non-life Insurance Economic Activities Tag Name",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesComment:
    "Taxonomy-eligible Non-life Insurance Economic Activities Comment",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompany:
    "Exposures To Taxonomy-eligible Economic Activities Asset Management Company",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyQuality:
    "Exposures To Taxonomy-eligible Economic Activities Asset Management Company Quality",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyPage:
    "Exposures To Taxonomy-eligible Economic Activities Asset Management Company Page",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyReport:
    "Exposures To Taxonomy-eligible Economic Activities Asset Management Company Report",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyTagName:
    "Exposures To Taxonomy-eligible Economic Activities Asset Management Company Tag Name",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyComment:
    "Exposures To Taxonomy-eligible Economic Activities Asset Management Company Comment",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompany:
    "Exposures To Taxonomy Non-eligible Economic Activities Asset Management Company",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyQuality:
    "Exposures To Taxonomy Non-eligible Economic Activities Asset Management Company Quality",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyPage:
    "Exposures To Taxonomy Non-eligible Economic Activities Asset Management Company Page",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyReport:
    "Exposures To Taxonomy Non-eligible Economic Activities Asset Management Company Report",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyTagName:
    "Exposures To Taxonomy Non-eligible Economic Activities Asset Management Company Tag Name",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyComment:
    "Exposures To Taxonomy Non-eligible Economic Activities Asset Management Company Comment",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompany:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Asset Management Company",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyQuality:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Asset Management Company Quality",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyPage:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Asset Management Company Page",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyReport:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Asset Management Company Report",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyTagName:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Asset Management Company Tag Name",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompanyComment:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Asset Management Company Comment",
  exposuresToDerivativesAssetManagementCompany: "Exposures To Derivatives Asset Management Company",
  exposuresToDerivativesAssetManagementCompanyQuality: "Exposures To Derivatives Asset Management Company Quality",
  exposuresToDerivativesAssetManagementCompanyPage: "Exposures To Derivatives Asset Management Company Page",
  exposuresToDerivativesAssetManagementCompanyReport: "Exposures To Derivatives Asset Management Company Report",
  exposuresToDerivativesAssetManagementCompanyTagName: "Exposures To Derivatives Asset Management Company Tag Name",
  exposuresToDerivativesAssetManagementCompanyComment: "Exposures To Derivatives Asset Management Company Comment",
  exposuresToNonNfrdEntitiesAssetManagementCompany: "Exposures To Non-NFRD Entities Asset Management Company",
  exposuresToNonNfrdEntitiesAssetManagementCompanyQuality:
    "Exposures To Non-NFRD Entities Asset Management Company Quality",
  exposuresToNonNfrdEntitiesAssetManagementCompanyPage: "Exposures To Non-NFRD Entities Asset Management Company Page",
  exposuresToNonNfrdEntitiesAssetManagementCompanyReport:
    "Exposures To Non-NFRD Entities Asset Management Company Report",
  exposuresToNonNfrdEntitiesAssetManagementCompanyTagName:
    "Exposures To Non-NFRD Entities Asset Management Company Tag Name",
  exposuresToNonNfrdEntitiesAssetManagementCompanyComment:
    "Exposures To Non-NFRD Entities Asset Management Company Comment",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirm:
    "Exposures To Taxonomy-eligible Economic Activities Investment Firm",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmQuality:
    "Exposures To Taxonomy-eligible Economic Activities Investment Firm Quality",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmPage:
    "Exposures To Taxonomy-eligible Economic Activities Investment Firm Page",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmReport:
    "Exposures To Taxonomy-eligible Economic Activities Investment Firm Report",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmTagName:
    "Exposures To Taxonomy-eligible Economic Activities Investment Firm Tag Name",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmComment:
    "Exposures To Taxonomy-eligible Economic Activities Investment Firm Comment",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirm:
    "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmQuality:
    "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm Quality",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmPage:
    "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm Page",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmReport:
    "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm Report",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmTagName:
    "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm Tag Name",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmComment:
    "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm Comment",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirm:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Investment Firm",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmQuality:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Investment Firm Quality",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmPage:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Investment Firm Page",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmReport:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Investment Firm Report",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmTagName:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Investment Firm Tag Name",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirmComment:
    "Exposures To Central Governments, Central Banks, Supranational Issuers Investment Firm Comment",
  exposuresToDerivativesInvestmentFirm: "Exposures To Derivatives Investment Firm",
  exposuresToDerivativesInvestmentFirmQuality: "Exposures To Derivatives Investment Firm Quality",
  exposuresToDerivativesInvestmentFirmPage: "Exposures To Derivatives Investment Firm Page",
  exposuresToDerivativesInvestmentFirmReport: "Exposures To Derivatives Investment Firm Report",
  exposuresToDerivativesInvestmentFirmTagName: "Exposures To Derivatives Investment Firm Tag Name",
  exposuresToDerivativesInvestmentFirmComment: "Exposures To Derivatives Investment Firm Comment",
  exposuresToNonNfrdEntitiesInvestmentFirm: "Exposures To Non-NFRD Entities Investment Firm",
  exposuresToNonNfrdEntitiesInvestmentFirmQuality: "Exposures To Non-NFRD Entities Investment Firm Quality",
  exposuresToNonNfrdEntitiesInvestmentFirmPage: "Exposures To Non-NFRD Entities Investment Firm Page",
  exposuresToNonNfrdEntitiesInvestmentFirmReport: "Exposures To Non-NFRD Entities Investment Firm Report",
  exposuresToNonNfrdEntitiesInvestmentFirmTagName: "Exposures To Non-NFRD Entities Investment Firm Tag Name",
  exposuresToNonNfrdEntitiesInvestmentFirmComment: "Exposures To Non-NFRD Entities Investment Firm Comment",
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
};

export const euTaxonomyKpiInfoMappings = {
  // These are not in the data dictionary
  financialServicesTypes: "Financial Services Types",
  reportingObligation: "NFRD Mandatory",
  investmentNonNfrd: "Investment Non NFRD",
  banksAndIssuers: "Banks and Issuers",
  derivatives: "For financial companies (FS), the percentage of total assets exposed to derivatives",
  taxonomyNonEligibleActivity:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  taxonomyEligibleActivity:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  provider: "Name of the Audit company which provide assurance to EU Taxonomy data points ",
  greenAssetRatio:
    "The proportion of the of credit institution’s assets financing and invested in taxonomy-aligned economic activities as a proportion of total covered assets",
  reportDate: "Report Date",
  reportingPeriod: "Reporting Period",
  totalAmount: "totalAmount",
  alignedPercentage: "Aligned Percentage",
  eligiblePercentage: "Eligible Percentage",
  currency: "The 3-letter alpha code that represents the currency used in the report",
  activityLevelReporting: "EU Taxonomy Activity Level Reporting",
  fiscalYearDeviation: "Is Fiscal Year deviating?",
  quality: "The level of confidence associated to the value",
  page: "The page number of the document from where the information was sourced",
  report: "The report from where the information was sourced",
  tagName: "The name of the tag where the information is in the pdf",
  comment: "Free optional text",

  //These are generated from the data dictionary
  isFs: "Distinguishes between non-financial companies (IS) and financial companies (FS). There are two possible values expected: 1- IS, 2- FS",
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
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitutionComment: "Free optional text",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitutionComment: "Free optional text",
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
  exposuresToNonNfrdEntitiesCreditInstitution:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  exposuresToNonNfrdEntitiesCreditInstitutionQuality: "The level of confidence associated to the value",
  exposuresToNonNfrdEntitiesCreditInstitutionPage:
    "The page number of the document from where the information was sourced",
  exposuresToNonNfrdEntitiesCreditInstitutionReport: "The report from where the information was sourced",
  exposuresToNonNfrdEntitiesCreditInstitutionTagName: "The name of the tag where the information is in the pdf",
  exposuresToNonNfrdEntitiesCreditInstitutionComment: "Free optional text",
  tradingPortfolio: "For Credit Institutions, the trading portfolio as a percentage of total assets",
  tradingPortfolioQuality: "The level of confidence associated to the value",
  tradingPortfolioPage: "The page number of the document from where the information was sourced",
  tradingPortfolioReport: "The report from where the information was sourced",
  tradingPortfolioTagName: "The name of the tag where the information is in the pdf",
  tradingPortfolioComment: "Free optional text",
  onDemandInterbankLoans: "For Credit Institutions, the on demand interbank loans as a percentage of total assets",
  onDemandInterbankLoansQuality: "The level of confidence associated to the value",
  onDemandInterbankLoansPage: "The page number of the document from where the information was sourced",
  onDemandInterbankLoansReport: "The report from where the information was sourced",
  onDemandInterbankLoansTagName: "The name of the tag where the information is in the pdf",
  onDemandInterbankLoansComment: "Free optional text",
  tradingPortfolioOnDemandInterbankLoans:
    "For Credit Institutions, the trading portfolio and the on demand interbank loans as a percentage of total assets",
  tradingPortfolioOnDemandInterbankLoansQuality: "The level of confidence associated to the value",
  tradingPortfolioOnDemandInterbankLoansPage: "The page number of the document from where the information was sourced",
  tradingPortfolioOnDemandInterbankLoansReport: "The report from where the information was sourced",
  tradingPortfolioOnDemandInterbankLoansTagName: "The name of the tag where the information is in the pdf",
  tradingPortfolioOnDemandInterbankLoansComment: "Free optional text",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsuranceQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsuranceReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsuranceTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsuranceComment: "Free optional text",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsuranceQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsuranceReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsuranceTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsuranceComment: "Free optional text",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsuranceQuality:
    "The level of confidence associated to the value",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsuranceReport:
    "The report from where the information was sourced",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsuranceTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsuranceComment: "Free optional text",
  exposuresToDerivativesInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to derivatives",
  exposuresToDerivativesInsuranceReinsuranceQuality: "The level of confidence associated to the value",
  exposuresToDerivativesInsuranceReinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToDerivativesInsuranceReinsuranceReport: "The report from where the information was sourced",
  exposuresToDerivativesInsuranceReinsuranceTagName: "The name of the tag where the information is in the pdf",
  exposuresToDerivativesInsuranceReinsuranceComment: "Free optional text",
  exposuresToNonNfrdEntitiesInsuranceReinsurance:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  exposuresToNonNfrdEntitiesInsuranceReinsuranceQuality: "The level of confidence associated to the value",
  exposuresToNonNfrdEntitiesInsuranceReinsurancePage:
    "The page number of the document from where the information was sourced",
  exposuresToNonNfrdEntitiesInsuranceReinsuranceReport: "The report from where the information was sourced",
  exposuresToNonNfrdEntitiesInsuranceReinsuranceTagName: "The name of the tag where the information is in the pdf",
  exposuresToNonNfrdEntitiesInsuranceReinsuranceComment: "Free optional text",
  taxonomyEligibleNonLifeInsuranceEconomicActivities:
    "For Insurance/Reinsurance companies, the percentage of Taxonomy-eligible non-life insurance economics activities. Insurance and reinsurance undertakings other than life insurance undertakings shall calculate the KPI related to underwriting activities and present the ‘gross premiums written’ non-life insurance revenue or, as applicable, reinsurance revenue corresponding to Taxonomy-aligned insurance or reinsurance activities",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesQuality: "The level of confidence associated to the value",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesPage:
    "The page number of the document from where the information was sourced",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesReport: "The report from where the information was sourced",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesTagName: "The name of the tag where the information is in the pdf",
  taxonomyEligibleNonLifeInsuranceEconomicActivitiesComment: "Free optional text",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompanyComment: "Free optional text",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompanyComment: "Free optional text",
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
  exposuresToNonNfrdEntitiesAssetManagementCompany:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  exposuresToNonNfrdEntitiesAssetManagementCompanyQuality: "The level of confidence associated to the value",
  exposuresToNonNfrdEntitiesAssetManagementCompanyPage:
    "The page number of the document from where the information was sourced",
  exposuresToNonNfrdEntitiesAssetManagementCompanyReport: "The report from where the information was sourced",
  exposuresToNonNfrdEntitiesAssetManagementCompanyTagName: "The name of the tag where the information is in the pdf",
  exposuresToNonNfrdEntitiesAssetManagementCompanyComment: "Free optional text",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmQuality: "The level of confidence associated to the value",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirmComment: "Free optional text",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. i.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmQuality:
    "The level of confidence associated to the value",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmPage:
    "The page number of the document from where the information was sourced",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmReport:
    "The report from where the information was sourced",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmTagName:
    "The name of the tag where the information is in the pdf",
  exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirmComment: "Free optional text",
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
  exposuresToNonNfrdEntitiesInvestmentFirm:
    "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
  exposuresToNonNfrdEntitiesInvestmentFirmQuality: "The level of confidence associated to the value",
  exposuresToNonNfrdEntitiesInvestmentFirmPage:
    "The page number of the document from where the information was sourced",
  exposuresToNonNfrdEntitiesInvestmentFirmReport: "The report from where the information was sourced",
  exposuresToNonNfrdEntitiesInvestmentFirmTagName: "The name of the tag where the information is in the pdf",
  exposuresToNonNfrdEntitiesInvestmentFirmComment: "Free optional text",
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
};
