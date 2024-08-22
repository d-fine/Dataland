import { type Field } from '@/utils/GenericFrameworkTypes';
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type CurrencyDataPoint } from '@clients/backend';
import { formatAmountWithCurrency } from '@/utils/Formatter';
import {
  getDataPointGetterFactory,
  wrapDisplayValueWithDatapointInformation,
} from '@/components/resources/dataTable/conversion/DataPoints';

/**
 * Formats a currency value for display in the data-table
 * @param dataPoint the datapoint to format
 * @param fieldLabel the label of the datapoint
 * @returns the cell to display
 */
export function formatCurrencyForDisplay(
  dataPoint: CurrencyDataPoint | null | undefined,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  const datapointValue = formatAmountWithCurrency({ amount: dataPoint?.value, currency: dataPoint?.currency }).trim();

  return wrapDisplayValueWithDatapointInformation(
    {
      displayValue: datapointValue,
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
    },
    fieldLabel,
    dataPoint
  );
}

/**
 * Returns a value factory that returns the value of the currency data point form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
export function currencyDataPointValueGetterFactory(
  path: string,
  field: Field
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return getDataPointGetterFactory<number, CurrencyDataPoint>(
    path,
    field,
    (dataPoint?: CurrencyDataPoint): string | undefined => {
      const datapointValue = formatAmountWithCurrency({ amount: dataPoint?.value });
      return datapointValue ? `${datapointValue} ${dataPoint?.currency ?? ''}`.trim() : '';
    }
  );
}
