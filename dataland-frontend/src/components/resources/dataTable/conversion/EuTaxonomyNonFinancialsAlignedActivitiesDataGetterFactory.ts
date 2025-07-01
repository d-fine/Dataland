import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';

import { type EuTaxonomyAlignedActivity } from '@clients/backend';
import AlignedActivitiesDataTable from '@/components/general/AlignedActivitiesDataTable.vue';
import { euTaxonomyNonFinancialsModalColumnHeaders } from '@/components/resources/dataTable/conversion/EutaxonomyNonAlignedActivitiesValueGetterFactory';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { createModalDisplayObject } from '@/utils/CreateModalDisplayObject.ts';

/**
 * Formats a EuTaxonomyAlignedActivities component for display in the table using a modal
 * @param alignedActivities the input to display
 * @param fieldLabel the label of the containing field
 * @param kpiType the type of KPI (revenue, capex, opex) to determine the appropriate column headers
 * @returns the display-value for the table
 */
export function formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable(
  alignedActivities: ExtendedDataPoint<EuTaxonomyAlignedActivity[]> | null | undefined,
  fieldLabel: string,
  kpiType: 'revenue' | 'capex' | 'opex' = 'revenue'
): AvailableMLDTDisplayObjectTypes {
  if (!alignedActivities) {
    return MLDTDisplayObjectForEmptyString;
  }

  const tableKeyMap = {
    revenue: 'alignedActivities',
    capex: 'capexAlignedActivities',
    opex: 'opexAlignedActivities',
  };

  const typeLabels = {
    revenue: 'Revenue',
    capex: 'CapEx',
    opex: 'OpEx',
  };

  const tableKey = tableKeyMap[kpiType];

  const adjustedHeaders = {
    ...euTaxonomyNonFinancialsModalColumnHeaders.alignedActivities,
    kpi: typeLabels[kpiType],
    kpiPercent: `${typeLabels[kpiType]} (%)`,
  };

  const customColumnHeaders = {
    ...euTaxonomyNonFinancialsModalColumnHeaders,
    [tableKey]: adjustedHeaders,
  };

  return createModalDisplayObject({
    activities: alignedActivities,
    fieldLabel,
    kpiType,
    tableKey,
    columnHeaders: customColumnHeaders,
    modalComponent: AlignedActivitiesDataTable,
  });
}
