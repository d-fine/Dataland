import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import AlignedActivitiesDataTable from '@/components/general/AlignedActivitiesDataTable.vue';
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

type AlignedActivitiesTableRow = EligibleOrAlignedActivity & {
  dnshToClimateChangeMitigation?: string | null;
  dnshToClimateChangeAdaptation?: string | null;
  dnshToSustainableUseAndProtectionOfWaterAndMarineResources?: string | null;
  dnshToTransitionToACircularEconomy?: string | null;
  dnshToPollutionPreventionAndControl?: string | null;
  dnshToProtectionAndRestorationOfBiodiversityAndEcosystems?: string | null;
  minimumSafeguards?: string | null;
};

/**
 * Formats the eligible-or-aligned activities component for the display in the multi-layer-data-table.
 * The existing aligned-activities modal is reused here because the 2026/73 activity shape is closest to the
 * aligned-activities structure. The KPI percentage column is mapped to the eligible share percentage while the KPI
 * amount column still shows the aligned absolute share from the regular share object.
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
    [kpiType]: `${typeLabels[kpiType]} (aligned)`,
    [`${kpiType}Percent`]: `${typeLabels[kpiType]} eligible (%)`,
  };

  const customColumnHeaders = {
    ...euTaxonomyNonFinancialsModalColumnHeaders,
    [tableKey]: adjustedHeaders,
  };

  const mappedActivities: ExtendedDataPoint<AlignedActivitiesTableRow[]> = {
    ...eligibleOrAlignedActivities,
    value:
      eligibleOrAlignedActivities.value?.map((activity) => ({
        ...activity,
        share: {
          ...activity.share,
          relativeShareInPercent: activity.relativeEligibleShareInPercent ?? activity.share?.relativeShareInPercent,
        },
        dnshToClimateChangeMitigation: undefined,
        dnshToClimateChangeAdaptation: undefined,
        dnshToSustainableUseAndProtectionOfWaterAndMarineResources: undefined,
        dnshToTransitionToACircularEconomy: undefined,
        dnshToPollutionPreventionAndControl: undefined,
        dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: undefined,
        minimumSafeguards: undefined,
      })) ?? [],
  };

  return createModalDisplayObject({
    activities: mappedActivities,
    fieldLabel,
    kpiType,
    tableKey,
    columnHeaders: customColumnHeaders,
    modalComponent: AlignedActivitiesDataTable,
  });
}
