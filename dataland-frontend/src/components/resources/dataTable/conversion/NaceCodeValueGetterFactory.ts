import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";
import { convertSingleNaceCode } from "@/utils/NaceCodeConverter";

/**
 * Returns a value factory that returns the value of the NACE code form field.
 * If multiple values are selected, it returns a cell with a modal link. The modal displays all selected values.
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function naceCodeValueGetterFactory(
  path: string,
  field: Field,
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const selectionValue = getFieldValueFromFrameworkDataset(path, dataset) as Array<string>;
    if (!selectionValue || selectionValue.length == 0) {
      return MLDTDisplayObjectForEmptyString;
    } else {
      return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
        displayValue: {
          label: `Show ${selectionValue.length} NACE code${selectionValue.length > 1 ? "s" : ""}`,
          modalComponent: MultiSelectModal,
          modalOptions: {
            props: {
              header: field.label,
              modal: true,
              dismissableMask: true,
            },
            data: {
              label: field.label,
              values: selectionValue.map(convertSingleNaceCode),
            },
          },
        },
      };
    }
  };
}
