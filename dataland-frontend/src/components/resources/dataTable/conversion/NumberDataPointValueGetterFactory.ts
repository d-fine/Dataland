import { type Field } from "@/utils/GenericFrameworkTypes";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { getDataPointGetterFactory } from "@/components/resources/dataTable/conversion/Utils";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { type GenericDataPoint } from "@/utils/DataPoint";

/**
 * Returns a value factory that returns the value of the number data point form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
export function numberDataPointValueGetterFactory(
  path: string,
  field: Field,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return getDataPointGetterFactory(path, field, (dataPoint: GenericDataPoint<number>) => {
    const datapointValue = formatNumberToReadableFormat(dataPoint.value);
    const datapointUnitSuffix = field.unit ?? "";
    return datapointValue ? `${datapointValue} ${datapointUnitSuffix}`.trim() : "";
  });
}
