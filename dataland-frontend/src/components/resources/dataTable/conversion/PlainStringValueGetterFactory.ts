import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";

/**
 * Returns a value factory that returns the value of the field as a string.
 * If the value is non-truthy, an empty string is returned
 * @param path the path to the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function plainStringValueGetterFactory(path: string): (dataset: any) => AvailableDisplayValues {
  return (dataset) => ({
    displayComponent: MLDTDisplayComponents.StringDisplayComponent,
    displayValue: (getFieldValueFromDataModel(path, dataset) || "") as string,
  });
}
