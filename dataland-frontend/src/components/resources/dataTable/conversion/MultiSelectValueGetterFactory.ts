import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";

/**
 * Returns a value factory that returns the value of the MultiSelect form field.
 * If multiple values are selected, it returns a cell with a modal link. The modal displays all selected values.
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function multiSelectValueGetterFactory(path: string, field: Field): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  const nameMap = new Map<string, string>();
  for (const option of field.options ?? []) {
    nameMap.set(option.value, option.label);
  }

  return (dataset) => {
    const selectionValue = getFieldValueFromFrameworkDataset(path, dataset) as Array<string>;
    if (!selectionValue || selectionValue.length == 0) {
      return {
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: "",
      };
    } else {
      return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
        displayValue: {
          label: `Show ${selectionValue.length} value${selectionValue.length > 1 ? "s" : ""}`,
          modalComponent: MultiSelectModal,
          modalOptions: {
            props: {
              header: field.label,
              modal: true,
              dismissableMask: true,
            },
            data: {
              label: field.label,
              values: selectionValue.map((it) => nameMap.get(it)),
            },
          },
        },
      };
    }
  };
}
