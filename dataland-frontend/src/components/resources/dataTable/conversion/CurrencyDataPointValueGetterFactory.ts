import { type Field } from "@/utils/GenericFrameworkTypes";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type CurrencyDataPoint } from "@clients/backend";
import { getDataPointGetterFactory } from "@/components/resources/dataTable/conversion/Utils";
import { formatAmountWithCurrency } from "@/utils/Formatter";

/**
 * Returns a value factory that returns the value of the currency data point form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
export function currencyDataPointValueGetterFactory(
  path: string,
  field: Field,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return getDataPointGetterFactory(path, field, (dataPoint: CurrencyDataPoint) => {
    const datapointValue = formatAmountWithCurrency({ amount: dataPoint.value });
    return datapointValue ? `${datapointValue} ${dataPoint.currency}`.trim() : "";
  });
}
