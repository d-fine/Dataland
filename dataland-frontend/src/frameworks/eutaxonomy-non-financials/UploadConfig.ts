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
        name: "scopeOfEntities",
        label: "Scope Of Entities",
        fields: [],
      },
      {
        name: "nfrdMandatory",
        label: "NFRD Mandatory",
        fields: [],
      },
      {
        name: "euTaxonomyActivityLevelReporting",
        label: "EU Taxonomy Activity Level Reporting",
        fields: [],
      },
      {
        name: "assurance",
        label: "Assurance",
        fields: [],
      },
      {
        name: "numberOfEmployees",
        label: "Number Of Employees",
        fields: [],
      },
      {
        name: "referencedReports",
        label: "Referenced Reports",
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
        name: "totalAmount",
        label: "Total Amount",
        fields: [],
      },
      {
        name: "nonEligibleShare",
        label: "Non-Eligible Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "eligibleShare",
        label: "Eligible Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "nonAlignedShare",
        label: "Non-Aligned Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "alignedShare",
        label: "Aligned Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "substantialContributionToClimateChangeMitigationInPercent",
        label: "Substantial Contribution to Climate Change Mitigation In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeAdaptationInPercent",
        label: "Substantial Contribution to Climate Change Adaptation In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
        label: "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToTransitionToACircularEconomyInPercent",
        label: "Substantial Contribution to Transition to a Circular Economy In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToPollutionPreventionAndControlInPercent",
        label: "Substantial Contribution to Pollution Prevention and Control In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent",
        label: "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems In Percent",
        fields: [],
      },
      {
        name: "enablingShareInPercent",
        label: "Enabling Share In Percent",
        fields: [],
      },
      {
        name: "transitionalShareInPercent",
        label: "Transitional Share In Percent",
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
        name: "totalAmount",
        label: "Total Amount",
        fields: [],
      },
      {
        name: "nonEligibleShare",
        label: "Non-Eligible Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "eligibleShare",
        label: "Eligible Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "nonAlignedShare",
        label: "Non-Aligned Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "alignedShare",
        label: "Aligned Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "substantialContributionToClimateChangeMitigationInPercent",
        label: "Substantial Contribution to Climate Change Mitigation In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeAdaptationInPercent",
        label: "Substantial Contribution to Climate Change Adaptation In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
        label: "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToTransitionToACircularEconomyInPercent",
        label: "Substantial Contribution to Transition to a Circular Economy In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToPollutionPreventionAndControlInPercent",
        label: "Substantial Contribution to Pollution Prevention and Control In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent",
        label: "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems In Percent",
        fields: [],
      },
      {
        name: "enablingShareInPercent",
        label: "Enabling Share In Percent",
        fields: [],
      },
      {
        name: "transitionalShareInPercent",
        label: "Transitional Share In Percent",
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
        name: "totalAmount",
        label: "Total Amount",
        fields: [],
      },
      {
        name: "nonEligibleShare",
        label: "Non-Eligible Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "eligibleShare",
        label: "Eligible Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "nonAlignedShare",
        label: "Non-Aligned Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "alignedShare",
        label: "Aligned Share",
        fields: [
          {
            name: "relativeShareInPercent",
            label: "Relative Share in Percent",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "between:0,100",
          },
        ],
      },
      {
        name: "substantialContributionToClimateChangeMitigationInPercent",
        label: "Substantial Contribution to Climate Change Mitigation In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToClimateChangeAdaptationInPercent",
        label: "Substantial Contribution to Climate Change Adaptation In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
        label: "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToTransitionToACircularEconomyInPercent",
        label: "Substantial Contribution to Transition to a Circular Economy In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToPollutionPreventionAndControlInPercent",
        label: "Substantial Contribution to Pollution Prevention and Control In Percent",
        fields: [],
      },
      {
        name: "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent",
        label: "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems In Percent",
        fields: [],
      },
      {
        name: "enablingShareInPercent",
        label: "Enabling Share In Percent",
        fields: [],
      },
      {
        name: "transitionalShareInPercent",
        label: "Transitional Share In Percent",
        fields: [],
      },
    ],
  },
] as Category[];
