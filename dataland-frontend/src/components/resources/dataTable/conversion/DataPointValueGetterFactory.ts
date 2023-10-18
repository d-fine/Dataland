import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";
import { CurrencyDataPoint, type ExtendedDataPointBigDecimal } from "@clients/backend";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
/**
 * Returns a value factory that returns the value of the DataPointFormField
 * @param path the path to the field
 * @param field the field from the data model
 * @returns the created getter
 */
export function dataPointValueGetterFactory(
  path: string,
  field: Field,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const datapoint = getFieldValueFromFrameworkDataset(path, dataset) as ExtendedDataPointBigDecimal | undefined;

    if (!datapoint?.value) {
      return MLDTDisplayObjectForEmptyString;
    }

    const datapointValue = formatNumberToReadableFormat(datapoint.value);
    let datapointUnitSuffix;

    if (field.options) {
      if (field.options == getDataset(DropdownDatasetIdentifier.CurrencyCodes)) {
        datapointUnitSuffix = (datapoint as CurrencyDataPoint)?.currency ?? "";
      } else {
        // TODO why does the following check for field unit as well as options? This should never happen, right?
        const datapointUnitRaw = field.unit ?? "";
        const matchingEntry = field.options.find((it) => it.value == datapointUnitRaw);
        datapointUnitSuffix = matchingEntry?.label ?? datapointUnitRaw;
      }
    } else {
      datapointUnitSuffix = field.unit ?? "";
    }

    return {
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: `${datapointValue} ${datapointUnitSuffix}`.trim(),
    };
  };
}
