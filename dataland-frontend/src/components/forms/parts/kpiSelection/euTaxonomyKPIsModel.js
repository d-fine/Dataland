export const euTaxonomyKPIsModel = {
  industrialCompanies: {
    revenue: ["eligibleRevenue", "alignedRevenue", "totalRevenue"],
    capEx: ["eligibleCapEx", "alignedCapEx", "totalCapEx"],
    opEx: ["eligibleOpEx", "alignedOpEx", "totalOpEx"],
  },
  financialCompanies: {
    exposuresToTaxonomy: [
      "exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompany",
      "exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompany",
    ],
    exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompany: [
      "exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompany",
    ],
    exposuresToDerivativesAssetManagementCompany: ["exposuresToDerivativesAssetManagementCompany"],
    exposuresToNonNFRDEntitiesAssetManagementCompany: ["exposuresToNonNFRDEntitiesAssetManagementCompany"],
  },
  creditInstitution: {
    tradingPortfolioOnDemandInterbankLoans: ["tradingPortfolioOnDemandInterbankLoans"],
    tradingPortfolio: ["tradingPortfolio"],
    OnDemandInterbankLoans: ["OnDemandInterbankLoans"],
  },
  greenAssetRatio: {
    greenAssetRatioInvestmentFirm: ["greenAssetRatioInvestmentFirm"],
  },
  insuranceReinsurance: {
    taxonomyEligibleNonLifeInsuranceEconomicActivities: ["taxonomyEligibleNonLifeInsuranceEconomicActivities"],
  },
};
