import { type Category } from "@/utils/GenericFrameworkTypes";
import { EutaxonomyNonFinancialsData } from "@clients/backend";

export const eutaxonomyNonFinancialsDataModel = [
  {
    name: "general",
    label: "General",
    color: "orange",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "reportingPeriod",
        label: "Reporting period",
        fields: [],
      },
      {
        name: "fiscalYearDeviation",
        label: "Fiscal Year Deviation",
        fields: [],
      },
      {
        name: "fiscalYearEnd",
        label: "Fiscal Year End",
        fields: [],
      },
      {
        name: "referencedReports",
        label: "Referenced Reports",
        fields: [],
      },
      {
        name: "scopeOfEntities",
        label: "Scope Of Entities",
        fields: [],
      },
      {
        name: "euTaxonomyActivityLevelReporting",
        label: "EU Taxonomy Activity Level Reporting",
        fields: [],
      },
      {
        name: "numberOfEmployees",
        label: "Number Of Employees",
        fields: [],
      },
      {
        name: "nfrdMandatory",
        label: "NFRD Mandatory",
        fields: [],
      },
      {
        name: "assurance",
        label: "Assurance",
        fields: [],
      },
    ],
  },
  {
    name: "revenue",
    label: "Revenue",
    color: "green",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "totalRevenue",
        label: "Total Revenue",
        fields: [],
      },
      {
        name: "eligibleRevenue",
        label: "Eligible Revenue",
        fields: [],
      },
      {
        name: "alignedRevenue",
        label: "Aligned Revenue",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeMitigation",
        label: "Substantial Contribution to Climate Change Mitigation",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeAdaptation",
        label: "Substantial Contribution to Climate Change Adaptation",
        fields: [],
      },
      {
        name: "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources",
        label: "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources",
        fields: [],
      },
      {
        name: "substantialContributionToTransitionToACircularEconomy",
        label: "Substantial Contribution to Transition to a Circular Economy",
        fields: [],
      },
      {
        name: "substantialContributionToPollutionPreventionAndControl",
        label: "Substantial Contribution to Pollution Prevention and Control",
        fields: [],
      },
      {
        name: "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems",
        label: "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems",
        fields: [],
      },
      {
        name: "nonAlignedRevenue",
        label: "Non-Aligned Revenue",
        fields: [],
      },
      {
        name: "nonEligibleRevenue",
        label: "Non-Eligible Revenue",
        fields: [],
      },
      {
        name: "enablingRevenue",
        label: "Enabling Revenue",
        fields: [],
      },
      {
        name: "transitionalRevenue",
        label: "Transitional Revenue",
        fields: [],
      },
    ],
  },
  {
    name: "capex",
    label: "CapEx",
    color: "yellow",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "totalCapex",
        label: "Total CapEx",
        fields: [],
      },
      {
        name: "eligibleCapex",
        label: "Eligible CapEx",
        fields: [],
      },
      {
        name: "alignedCapex",
        label: "Aligned CapEx",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeMitigation",
        label: "Substantial Contribution to Climate Change Mitigation",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeAdaptation",
        label: "Substantial Contribution to Climate Change Adaptation",
        fields: [],
      },
      {
        name: "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources",
        label: "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources",
        fields: [],
      },
      {
        name: "substantialContributionToTransitionToACircularEconomy",
        label: "Substantial Contribution to Transition to a Circular Economy",
        fields: [],
      },
      {
        name: "substantialContributionToPollutionPreventionAndControl",
        label: "Substantial Contribution to Pollution Prevention and Control",
        fields: [],
      },
      {
        name: "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems",
        label: "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems",
        fields: [],
      },
      {
        name: "nonAlignedCapex",
        label: "Non-Aligned CapEx",
        fields: [],
      },
      {
        name: "nonEligibleCapex",
        label: "Non-Eligible CapEx",
        fields: [],
      },
      {
        name: "enablingCapex",
        label: "Enabling CapEx",
        fields: [],
      },
      {
        name: "transitionalCapex",
        label: "Transitional CapEx",
        fields: [],
      },
    ],
  },
  {
    name: "opex",
    label: "OpEx",
    color: "blue",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "totalOpex",
        label: "Total OpEx",
        fields: [],
      },
      {
        name: "eligibleOpex",
        label: "Eligible OpEx",
        fields: [],
      },
      {
        name: "alignedOpex",
        label: "Aligned OpEx",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeMitigation",
        label: "Substantial Contribution to Climate Change Mitigation",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeAdaptation",
        label: "Substantial Contribution to Climate Change Adaptation",
        fields: [],
      },
      {
        name: "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources",
        label: "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources",
        fields: [],
      },
      {
        name: "substantialContributionToTransitionToACircularEconomy",
        label: "Substantial Contribution to Transition to a Circular Economy",
        fields: [],
      },
      {
        name: "substantialContributionToPollutionPreventionAndControl",
        label: "Substantial Contribution to Pollution Prevention and Control",
        fields: [],
      },
      {
        name: "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems",
        label: "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems",
        fields: [],
      },
      {
        name: "nonAlignedOpex",
        label: "Non-Aligned OpEx",
        fields: [],
      },
      {
        name: "nonEligibleOpex",
        label: "Non-Eligible OpEx",
        fields: [],
      },
      {
        name: "enablingOpex",
        label: "Enabling OpEx",
        fields: [],
      },
      {
        name: "transitionalOpex",
        label: "Transitional OpEx",
        fields: [],
      },
    ],
  },
] as Category[];
