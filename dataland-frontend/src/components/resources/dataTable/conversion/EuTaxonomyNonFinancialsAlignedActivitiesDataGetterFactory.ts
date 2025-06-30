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

  const tableKeyMap = {
    revenue: 'alignedActivities',
    capex: 'capexAlignedActivities',
    opex: 'opexAlignedActivities',
  };

  const tableKey = tableKeyMap[kpiType];

  const adjustedHeaders = {
    ...euTaxonomyNonFinancialsModalColumnHeaders.alignedActivities,
    revenue: typeLabels[kpiType],
    revenuePercent: `${typeLabels[kpiType]} (%)`,
  };

  const customColumnHeaders = {
    ...euTaxonomyNonFinancialsModalColumnHeaders,
    [tableKey]: adjustedHeaders,
  };

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
    displayValue: {
      label: `Show ${input.value?.length} activit${(input.value?.length ?? 0) > 1 ? 'ies' : 'y'}`,
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
