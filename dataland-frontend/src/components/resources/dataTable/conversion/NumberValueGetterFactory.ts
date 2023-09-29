import {
    type AvailableDisplayValues,
    MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import { formatNumberToReadableFormat } from "@/utils/Formatter";

/**
 * Returns a value factory that returns the value of the field as a nicely formatted number.
 * @param path the path to the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function numberValueGetterFactory(path: string): (dataset: any) => AvailableDisplayValues {
    return (dataset) => ({
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: formatNumberToReadableFormat(getFieldValueFromDataModel(path, dataset) as number),
    });
}