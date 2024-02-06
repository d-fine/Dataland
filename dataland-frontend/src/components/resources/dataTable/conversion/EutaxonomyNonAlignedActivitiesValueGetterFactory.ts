import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type EuTaxonomyActivity } from "@clients/backend";
import NonAlignedActivitiesDataTable from "@/components/general/NonAlignedActivitiesDataTable.vue";
import { euTaxonomyForNonFinancialsModalColumnHeaders } from "@/frameworks/eutaxonomy-non-financials/EuTaxonomyForNonFinancialsModalColumnHeaders";

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
          columnHeaders: euTaxonomyForNonFinancialsModalColumnHeaders,
        },
      },
    },
  };
}
