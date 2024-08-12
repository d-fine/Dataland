import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type EuTaxonomyActivity } from '@clients/backend';
import NonAlignedActivitiesDataTable from '@/components/general/NonAlignedActivitiesDataTable.vue';
import { type ExtendedDataPoint } from '@/utils/DataPoint';

export const euTaxonomyNonFinancialsModalColumnHeaders = {
  alignedActivities: {
    activityName: 'Activity',
    naceCodes: 'NACE Code(s)',
    share: 'Share',
    revenue: 'Revenue',
    revenuePercent: 'Revenue (%)',
    substantialContributionToClimateChangeMitigationInPercent: 'Climate Change Mitigation',
    substantialContributionToClimateChangeAdaptationInPercent: 'Climate Change Adaptation',
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
      'Water and Marine Resources',
    substantialContributionToTransitionToACircularEconomyInPercent: 'Circular Economy',
    substantialContributionToPollutionPreventionAndControlInPercent: 'Pollution Prevention',
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
      'Biodiversity and Ecosystems',
    dnshToClimateChangeMitigation: 'Climate Change Mitigation',
    dnshToClimateChangeAdaptation: 'Climate Change Adaptation',
    dnshToSustainableUseAndProtectionOfWaterAndMarineResources: 'Water and Marine Resources',
    dnshToTransitionToACircularEconomy: 'Circular Economy',
    dnshToPollutionPreventionAndControl: 'Pollution Prevention',
    dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: 'Biodiversity and Ecosystems',
    minimumSafeguards: 'Minimum safeguards',
    substantialContributionCriteria: 'Substantial Contribution Criteria',
    dnshCriteria: 'DNSH Criteria',
  },
  nonAlignedActivities: {
    activityName: 'Activity',
    naceCodes: 'NACE Code(s)',
    share: 'Share',
    revenue: 'Revenue',
    revenuePercent: 'Revenue (%)',
  },
};

/**
 * Formats an array of EuTaxonomyActivity for the display in the multi-layer-data-table.
 * This list is behind the field "nonAlignedActivities" in the eutaxonomy-non-financials framework.
 * @param nonAlignedActivities the list of EuTaxonomyActivity objects
 * @param fieldLabel the label of the respective field in the framework
 * @returns the display object for the multi-layer-data-table to render a modal to display the non-aligned activities
 */
export function formatNonAlignedActivitiesForDataTable(
  nonAlignedActivities: ExtendedDataPoint<EuTaxonomyActivity[]> | undefined | null,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  if (!nonAlignedActivities) {
    return MLDTDisplayObjectForEmptyString;
  }

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
    displayValue: {
      label: `Show ${nonAlignedActivities.value!.length} activit${nonAlignedActivities.value!.length > 1 ? 'ies' : 'y'}`,
      modalComponent: NonAlignedActivitiesDataTable,
      modalOptions: {
        props: {
          header: fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: nonAlignedActivities.value,
          kpiKeyOfTable: 'nonAlignedActivities',
          columnHeaders: euTaxonomyNonFinancialsModalColumnHeaders,
          dataSource: nonAlignedActivities.dataSource,
          comment: nonAlignedActivities.comment,
          quality: nonAlignedActivities.quality,
        },
      },
    },
  };
}
