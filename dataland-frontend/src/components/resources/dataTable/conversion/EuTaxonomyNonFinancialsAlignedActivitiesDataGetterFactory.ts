import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';

import { type EuTaxonomyAlignedActivity } from '@clients/backend';
import AlignedActivitiesDataTable from '@/components/general/AlignedActivitiesDataTable.vue';
import { euTaxonomyNonFinancialsModalColumnHeaders } from '@/components/resources/dataTable/conversion/EutaxonomyNonAlignedActivitiesValueGetterFactory';
import {type ExtendedDataPoint} from "@/utils/DataPoint";

/**
 * Formats a EuTaxonomyAlignedActivities component for display in the table using a modal
 * @param input the input to display
 * @param fieldLabel the label of the containing field
 * @returns the display-value for the table
 */
export function formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable(
  input: ExtendedDataPoint<EuTaxonomyAlignedActivity[]> | null | undefined,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
      displayValue: {
        label: `Show ${input.value?.length} activit${input.value?.length > 1 ? 'ies' : 'y'}`,
        modalComponent: AlignedActivitiesDataTable,
        modalOptions: {
          props: {
            header: fieldLabel,
            modal: true,
            dismissableMask: true,
          },
          data: {
            listOfRowContents: input.value,
            kpiKeyOfTable: 'alignedActivities',
            columnHeaders: euTaxonomyNonFinancialsModalColumnHeaders,

          },
          source: {
            dataSource: input.dataSource,
            comment: input.comment,
            quality: input.quality,
          }
        },
      },
    };
  }
}
