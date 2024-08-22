import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';
import { formatNumberToReadableFormat } from '@/utils/Formatter';
import { type Field } from '@/utils/GenericFrameworkTypes';

/**
 * Returns a value factory that returns the value of the field as a nicely formatted number.
 * @param path the path to the field
 * @param field the field from the data model
 * @returns the created getter
 */
export function numberValueGetterFactory(
  path: string,
  field: Field
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const fieldValue = getFieldValueFromFrameworkDataset(path, dataset) as number;
    const displayedUnit = field.unit ?? '';

    return formatNumberForDatatable(fieldValue, displayedUnit);
  };
}

/**
 * Formats the provided string as a number in a human-readable format with the unit appended to it
 * @param numberContent the number to display
 * @param unit the unit of the number
 * @returns the value formatted for display
 */
export function formatNumberForDatatable(
  numberContent: number | undefined | null,
  unit: string
): AvailableMLDTDisplayObjectTypes {
  const numberForDisplay = formatNumberToReadableFormat(numberContent);
  if (numberForDisplay === '') {
    return MLDTDisplayObjectForEmptyString;
  }
  return {
    displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
    displayValue: `${numberForDisplay} ${unit}`.trim(),
  };
}
