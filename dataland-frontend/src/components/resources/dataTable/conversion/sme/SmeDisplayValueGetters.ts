import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import { type SmeSubsidiaries } from "@clients/backend";

export const smeModalColumnHeaders = {
  listOfSubsidiaries: {
    nameOfSubsidiary: "Name",
    addressOfSubsidiary: "Address",
  },
};

/**
 * Generates a display modal component for subsidiaries
 * @param input list of sme subsidiaries for display
 * @param fieldLabel field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatSmeSubsidiariesForDisplay(
  input: SmeSubsidiaries[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  }

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
    displayValue: {
      label: `Show ${fieldLabel}`,
      modalComponent: DetailsCompanyDataTable,
      modalOptions: {
        props: {
          header: fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: input,
          kpiKeyOfTable: "listOfSubsidiaries",
          columnHeaders: smeModalColumnHeaders,
        },
      },
    },
  };
}
