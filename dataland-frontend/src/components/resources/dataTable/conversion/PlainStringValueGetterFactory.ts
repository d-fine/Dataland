import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';

/**
 * Returns a value factory that returns the value of the field as a string.
 * If the value is non-truthy, an empty string is returned
 * @param path the path to the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function plainStringValueGetterFactory(path: string): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => formatStringForDatatable(getFieldValueFromFrameworkDataset(path, dataset) as string | undefined);
}

/**
 * Formats the provided string as a raw string cell for the datatable
 * @param input the input to display
 * @returns the value formatted for display
 */
export function formatStringForDatatable(input: string | null | undefined): AvailableMLDTDisplayObjectTypes {
  return {
    displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
    displayValue: input ?? '',
  };
}
