import { type Field } from '@/utils/GenericFrameworkTypes';
import { type AvailableMLDTDisplayObjectTypes } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { formatNumberToReadableFormat } from '@/utils/Formatter';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { getDataPointGetterFactory } from '@/components/resources/dataTable/conversion/DataPoints';

/**
 * Returns a value factory that returns the value of the number data point form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
export function numberDataPointValueGetterFactory(
  path: string,
  field: Field
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return getDataPointGetterFactory<number, ExtendedDataPoint<number>>(
    path,
    field,
    (dataPoint?: ExtendedDataPoint<number>): string => {
      const datapointValue = formatNumberToReadableFormat(dataPoint?.value);
      const datapointUnitSuffix = field.unit ?? '';
      return datapointValue ? `${datapointValue} ${datapointUnitSuffix}`.trim() : '';
    }
  );
}
