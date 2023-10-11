import {
  type AvailableDisplayValues,
  EmptyDisplayValue,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import { type Field } from "@/utils/GenericFrameworkTypes";

/**
 * Returns a value factory that returns the value of the field as a string using the display mapping in the options field
 * If the value is non-truthy, an empty string is returned
 * @param path the path to the field
 * @param field the single select field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function singleSelectValueGetterFactory(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    const selectedElement = getFieldValueFromDataModel(path, dataset) as string | undefined;
    if (!selectedElement) {
      return EmptyDisplayValue;
    }

    const matchingOption = field.options?.find((it) => it.value == selectedElement);
    if (matchingOption) {
      return {
        displayComponent: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: matchingOption.label,
      };
    } else {
      return {
        displayComponent: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: selectedElement,
      };
    }
  };
}
