import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { type Field } from "@/utils/GenericFrameworkTypes";

/**
 * Returns a value factory that returns the value of the field as a nicely formatted number.
 * @param path the path to the field
 * @param field the field from the data model
 * @returns the created getter
 */
export function numberValueGetterFactory(
  path: string,
  field: Field,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const formattedFieldValue = formatNumberToReadableFormat(
      getFieldValueFromFrameworkDataset(path, dataset) as number,
    );
    const displayedUnit = field.unit ?? "";
    return {
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: `${formattedFieldValue} ${displayedUnit}`.trim(),
    };
  };
}
