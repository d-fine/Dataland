import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type CurrencyDataPoint } from "@clients/backend";
import {
  getFieldValueFromFrameworkDataset,
  hasDataPointValidReference,
} from "@/components/resources/dataTable/conversion/Utils";
import { formatAmountWithCurrency } from "@/utils/Formatter";

/**
 * Returns a value factory that returns the value of the currency data point form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function currencyDataPointValueGetterFactory(
  path: string,
  field: Field,
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const datapoint = getFieldValueFromFrameworkDataset(path, dataset) as CurrencyDataPoint | undefined;
    if (!datapoint) {
      return MLDTDisplayObjectForEmptyString;
    }
    const datapointValue = formatAmountWithCurrency({ amount: datapoint.value });
    const formattedValue: string = datapointValue ? `${datapointValue} ${datapoint.currency}`.trim() : "";
    if (datapoint.quality || datapoint.comment?.length || datapoint.dataSource?.page != null) {
      return {
        displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
        displayValue: {
          fieldLabel: field.label,
          value: formattedValue,
          dataSource: datapoint.dataSource,
          quality: datapoint.quality,
          comment: datapoint.comment,
        },
      };
    } else if (hasDataPointValidReference(datapoint)) {
      return {
        displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
        displayValue: {
          label: formattedValue,
          dataSource: datapoint.dataSource,
        },
      } as MLDTDisplayObject<MLDTDisplayComponentName.DocumentLinkDisplayComponent>;
    } else {
      return {
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: formattedValue,
      } as MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>;
    }
  };
}
