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
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function numberDataPointValueGetterFactory(
  path: string,
  field: Field,
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return getDataPointGetterFactory(path, field, (dataPoint: GenericDataPoint<any>) => {
    const datapointValue = formatNumberToReadableFormat(dataPoint.value);
    const datapointUnitSuffix = field.unit ?? "";
    return datapointValue ? `${datapointValue} ${datapointUnitSuffix}`.trim() : "";
  });
}
