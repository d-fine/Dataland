import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
/**
 * Returns a value factory that returns the value of the DataPointFormField
 * @param path the path to the field
 * @param field the field from the data model
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function dataPointValueGetterFactory(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    const datapoint = getFieldValueFromDataModel(path, dataset);

    if (!datapoint?.value) {
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "",
      };
    }

    const datapointValue = formatNumberToReadableFormat(datapoint.value);
    let datapointUnitSuffix;

    if (field.options) {
      const datapointUnitRaw = field.unit ?? "";
      const matchingEntry = field.options.find((it) => it.value == datapointUnitRaw);
      datapointUnitSuffix = matchingEntry?.label ?? datapointUnitRaw;
    } else {
      datapointUnitSuffix = field.unit ?? "";
    }

    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: `${datapointValue} ${datapointUnitSuffix}`.trim(),
    };
  };
}
