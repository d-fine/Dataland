import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import { formatPercentageNumberAsString } from "@/utils/Formatter";

/**
 * Returns a value factory that returns the value of the field as a nicely formatted percentage.
 * @param path the path to the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function percentageValueGetterFactory(path: string): (dataset: any) => AvailableDisplayValues {
  return (dataset) => ({
    displayComponent: MLDTDisplayComponents.StringDisplayComponent,
    displayValue: formatPercentageNumberAsString(getFieldValueFromDataModel(path, dataset) as number),
  });
}
