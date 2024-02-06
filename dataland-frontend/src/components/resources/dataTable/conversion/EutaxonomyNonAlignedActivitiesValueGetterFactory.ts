import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type EuTaxonomyActivity } from "@clients/backend";
import NonAlignedActivitiesDataTable from "@/components/general/NonAlignedActivitiesDataTable.vue";

export const euTaxonomyNonFinancialsModalColumnHeaders = {
  alignedActivities: {
    activityName: "Activity",
    naceCodes: "NACE Code(s)",
    share: "Share",
    revenue: "Revenue",
    revenuePercent: "Revenue (%)",
    substantialContributionToClimateChangeMitigationInPercent: "Climate Change Mitigation",
    substantialContributionToClimateChangeAdaptionInPercent: "Climate Change Adaptation",
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
      "Water and Marine Resources",
    substantialContributionToTransitionToACircularEconomyInPercent: "Circular Economy",
    substantialContributionToPollutionPreventionAndControlInPercent: "Pollution Prevention",
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
      "Biodiversity and Ecosystems",
    dnshToClimateChangeMitigation: "Climate Change Mitigation",
    dnshToClimateChangeAdaption: "Climate Change Adaptation",
    dnshToSustainableUseAndProtectionOfWaterAndMarineResources: "Water and Marine Resources",
    dnshToTransitionToACircularEconomy: "Circular Economy",
    dnshToPollutionPreventionAndControl: "Pollution Prevention",
    dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: "Biodiversity and Ecosystems",
    minimumSafeguards: "Minimum safeguards",
    substantialContributionCriteria: "Substantial Contribution Criteria",
    dnshCriteria: "DNSH Criteria",
  },
  nonAlignedActivities: {
    activityName: "Activity",
    naceCodes: "NACE Code(s)",
    share: "Share",
    revenue: "Revenue",
    revenuePercent: "Revenue (%)",
  },
};

/**
 * Formats the provided assurance datapoint for the datatable TODO rewrite
 * @param nonAlignedActivities the assurance object to display
 * @param fieldLabel the label of the assurance datapoint
 * @returns the value formatted for display
 */
export function formatNonAlignedActivitiesForDataTable(
  nonAlignedActivities: Array<EuTaxonomyActivity> | undefined | null,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  if (!nonAlignedActivities) {
    return MLDTDisplayObjectForEmptyString;
  }

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
    displayValue: {
      label: `Show ${nonAlignedActivities.length} activit${nonAlignedActivities.length > 1 ? "ies" : "y"}`,
      modalComponent: NonAlignedActivitiesDataTable,
      modalOptions: {
        props: {
          header: fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: nonAlignedActivities,
          kpiKeyOfTable: "TESTTESTODO", // TODO what do pick? Ideas later
          columnHeaders: euTaxonomyNonFinancialsModalColumnHeaders,
        },
      },
    },
  };
}
