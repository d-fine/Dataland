import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';

import { type EuTaxonomyAlignedActivity } from '@clients/backend';
import AlignedActivitiesDataTable from '@/components/general/AlignedActivitiesDataTable.vue';
import { euTaxonomyNonFinancialsModalColumnHeaders } from '@/components/resources/dataTable/conversion/EutaxonomyNonAlignedActivitiesValueGetterFactory';
import { type ExtendedDataPoint } from '@/utils/DataPoint';

/**
 * Formats a EuTaxonomyAlignedActivities component for display in the table using a modal
 * @param input the input to display
 * @param fieldLabel the label of the containing field
 * @param kpiType the type of KPI (revenue, capex, opex) to determine the appropriate column headers
 * @returns the display-value for the table
 */
export function formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable(
  input: ExtendedDataPoint<EuTaxonomyAlignedActivity[]> | null | undefined,
  fieldLabel: string,
  kpiType: 'revenue' | 'capex' | 'opex' = 'revenue'
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  }

  const typeLabels = {
    revenue: 'Revenue',
    capex: 'CapEx',
    opex: 'OpEx',
  };

  const columnHeadersBase = {
    activityName: 'Activity',
    naceCodes: 'NACE Code(s)',
    share: 'Share',
    revenue: typeLabels[kpiType],
    revenuePercent: `${typeLabels[kpiType]} (%)`,
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
  };

  type ColumnHeaders = typeof euTaxonomyNonFinancialsModalColumnHeaders & {
    [key: string]: typeof columnHeadersBase;
  };

  const tableKeyMap = {
    revenue: 'alignedActivities',
    capex: 'capexAlignedActivities',
    opex: 'opexAlignedActivities',
  };

  const tableKey = tableKeyMap[kpiType];

  const customColumnHeaders = {
    ...euTaxonomyNonFinancialsModalColumnHeaders,
  } as ColumnHeaders;

  customColumnHeaders[tableKey] = columnHeadersBase;

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
    displayValue: {
      label: `Show ${input.value?.length} activit${(input.value?.length ?? 0 > 1) ? 'ies' : 'y'}`,
      modalComponent: AlignedActivitiesDataTable,
      modalOptions: {
        props: {
          header: `${fieldLabel} (${typeLabels[kpiType]})`,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: input.value,
          kpiKeyOfTable: tableKey,
          columnHeaders: customColumnHeaders,
          dataPointDisplay: {
            dataSource: input.dataSource,
            comment: input.comment,
            quality: input.quality,
          },
          kpiType: kpiType,
        },
      },
    },
  };
}
