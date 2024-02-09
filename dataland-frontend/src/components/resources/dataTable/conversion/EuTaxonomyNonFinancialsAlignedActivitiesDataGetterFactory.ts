import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";

import { type EuTaxonomyAlignedActivity } from "@clients/backend";
import AlignedActivitiesDataTable from "@/components/general/AlignedActivitiesDataTable.vue";
import { euTaxonomyNonFinancialsModalColumnHeaders } from "@/components/resources/dataTable/conversion/EutaxonomyNonAlignedActivitiesValueGetterFactory";

/**
 * Formats a EuTaxonomyAlignedActivities component for display in the table using a modal
 * @param input the input to display
 * @param fieldLabel the label of the containing field
 * @returns the display-value for the table
 */
export function formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable(
  input: EuTaxonomyAlignedActivity[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: `Show data`,
        modalComponent: AlignedActivitiesDataTable,
        modalOptions: {
          props: {
            header: fieldLabel,
            modal: true,
            dismissableMask: true,
          },
          data: {
            listOfRowContents: input,
            kpiKeyOfTable: "alignedActivities",
            columnHeaders: euTaxonomyNonFinancialsModalColumnHeaders,
          },
        },
      },
    };
  }
}
