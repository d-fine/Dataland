import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import EligibleOrAlignedActivitiesDataTable from '@/components/general/EligibleOrAlignedActivitiesDataTable.vue';
import { euTaxonomyNonFinancialsModalColumnHeaders } from '@/components/resources/dataTable/conversion/EutaxonomyNonAlignedActivitiesValueGetterFactory';
import { createModalDisplayObject } from '@/utils/CreateModalDisplayObject.ts';
import { type ExtendedDataPoint } from '@/utils/DataPoint';

type EligibleOrAlignedActivity = {
  activityName?: string;
  naceCodes?: string[] | null;
  share?: {
    relativeShareInPercent?: number | null;
    absoluteShare?: unknown;
  } | null;
  relativeEligibleShareInPercent?: number | null;
  substantialContributionToClimateChangeMitigationInPercent?: number | null;
  substantialContributionToClimateChangeAdaptationInPercent?: number | null;
  substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent?: number | null;
  substantialContributionToTransitionToACircularEconomyInPercent?: number | null;
  substantialContributionToPollutionPreventionAndControlInPercent?: number | null;
  substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent?: number | null;
  enablingActivity?: string | null;
  transitionalActivity?: string | null;
};

/**
 * Formats the eligible-or-aligned activities component for the display in the multi-layer-data-table.
 * Displays three KPI columns per activity: the aligned absolute amount, the aligned share in percent,
 * and the eligible share in percent.
 *
 * @param eligibleOrAlignedActivities the list of activity objects wrapped in an extended datapoint
 * @param fieldLabel the label of the respective field in the framework
 * @param kpiType the type of KPI (revenue, capex, opex) to determine the appropriate column headers
 * @returns the display object for the multi-layer-data-table modal
 */
export function formatEuTaxonomyNonFinancialsEligibleOrAlignedActivitiesDataForTable(
  eligibleOrAlignedActivities: ExtendedDataPoint<EligibleOrAlignedActivity[]> | null | undefined,
  fieldLabel: string,
  kpiType: 'revenue' | 'capex' | 'opex' = 'revenue'
): AvailableMLDTDisplayObjectTypes {
  if (!eligibleOrAlignedActivities) {
    return MLDTDisplayObjectForEmptyString;
  }

  const typeLabels = {
    revenue: 'Revenue',
    capex: 'CapEx',
    opex: 'OpEx',
  };

  const tableKey = kpiType;

  const adjustedHeaders = {
    ...euTaxonomyNonFinancialsModalColumnHeaders.alignedActivities,
    [kpiType]: `Aligned ${typeLabels[kpiType]}`,
    [`${kpiType}Percent`]: `Aligned ${typeLabels[kpiType]} (%)`,
    [`${kpiType}EligiblePercent`]: `Eligible ${typeLabels[kpiType]} (%)`,
  };

  const customColumnHeaders = {
    ...euTaxonomyNonFinancialsModalColumnHeaders,
    [tableKey]: adjustedHeaders,
  };

  return createModalDisplayObject({
    activities: eligibleOrAlignedActivities,
    fieldLabel,
    kpiType,
    tableKey,
    columnHeaders: customColumnHeaders,
    modalComponent: EligibleOrAlignedActivitiesDataTable,
  });
}
