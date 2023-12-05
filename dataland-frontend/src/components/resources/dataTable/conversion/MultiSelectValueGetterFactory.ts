import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";

/**
 * Returns a value factory that returns the value of the MultiSelect form field.
 * If multiple values are selected, it returns a cell with a modal link. The modal displays all selected values.
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
export function multiSelectValueGetterFactory(
  path: string,
  field: Field,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const selectionValue = getFieldValueFromFrameworkDataset(path, dataset) as Array<string>;
    return formatListOfStringsForDatatable(selectionValue, field.label);
  };
}

/**
 * Formats the provided string as a raw string cell for the datatable TODO
 * @param input the input to display
 * @param fieldLabel TODO
 * @returns the value formatted for display
 */
export function formatListOfStringsForDatatable(
  input: string[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  if (!input || input.length == 0) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: `Show ${input.length} value${input.length > 1 ? "s" : ""}`,
        modalComponent: MultiSelectModal, // TODO Emanuel: This leads to an import error when you run prepopulation!
        modalOptions: {
          props: {
            header: fieldLabel,
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: fieldLabel,
            values: input,
          },
        },
      },
    };
  }
}
