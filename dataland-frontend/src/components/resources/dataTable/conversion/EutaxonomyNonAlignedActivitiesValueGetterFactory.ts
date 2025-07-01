import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type EuTaxonomyActivity } from '@clients/backend';
import NonAlignedActivitiesDataTable from '@/components/general/NonAlignedActivitiesDataTable.vue';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { createModalDisplayObject } from '@/utils/CreateModalDisplayObject.ts';

const revenueColumnHeaders = {
  activityName: 'Activity',
  naceCodes: 'NACE Code(s)',
  share: 'Share',
  kpi: 'Revenue',
  kpiPercent: 'Revenue (%)',
};

const capexColumnHeaders = {
  activityName: 'Activity',
  naceCodes: 'NACE Code(s)',
  share: 'Share',
  kpi: 'CapEx',
  kpiPercent: 'CapEx (%)',
};

const opexColumnHeaders = {
  activityName: 'Activity',
  naceCodes: 'NACE Code(s)',
  share: 'Share',
  kpi: 'OpEx',
  kpiPercent: 'OpEx (%)',
};

export const euTaxonomyNonFinancialsModalColumnHeaders = {
  alignedActivities: {
    activityName: 'Activity',
    naceCodes: 'NACE Code(s)',
    share: 'Share',
    kpi: 'Revenue',
    kpiPercent: 'Revenue (%)',
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
    minimumSafeguards: 'Minimum Safeguards',
    enablingActivity: 'Enabling Activity',
    transitionalActivity: 'Transitional Activity',
    substantialContributionCriteria: 'Substantial Contribution Criteria',
    dnshCriteria: 'DNSH Criteria',
  },
  nonAlignedActivities: revenueColumnHeaders,
  capexNonAlignedActivities: capexColumnHeaders,
  opexNonAlignedActivities: opexColumnHeaders,
};

/**
 * Formats an array of EuTaxonomyActivity for the display in the multi-layer-data-table.
 * This list is behind the field "nonAlignedActivities" in the eutaxonomy-non-financials framework.
 * @param nonAlignedActivities the list of EuTaxonomyActivity objects
 * @param fieldLabel the label of the respective field in the framework
 * @param kpiType the type of KPI (revenue, capex, opex) to determine the appropriate column headers
 * @returns the display object for the multi-layer-data-table to render a modal to display the non-aligned activities
 */
export function formatNonAlignedActivitiesForDataTable(
  nonAlignedActivities: ExtendedDataPoint<EuTaxonomyActivity[]> | undefined | null,
  fieldLabel: string,
  kpiType: 'revenue' | 'capex' | 'opex' = 'revenue'
): AvailableMLDTDisplayObjectTypes {
  if (!nonAlignedActivities) {
    return MLDTDisplayObjectForEmptyString;
  }

  const tableKeyMap = {
    revenue: 'nonAlignedActivities',
    capex: 'capexNonAlignedActivities',
    opex: 'opexNonAlignedActivities',
  };

  const tableKey = tableKeyMap[kpiType];

  return createModalDisplayObject({
    activities: nonAlignedActivities,
    fieldLabel,
    kpiType,
    tableKey,
    columnHeaders: euTaxonomyNonFinancialsModalColumnHeaders,
    modalComponent: NonAlignedActivitiesDataTable,
  });
}
