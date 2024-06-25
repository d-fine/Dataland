import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';

/**
 * Formats the provided string as a FreeText cell for the datatable
 * @param input the input to display
 * @returns the value formatted for display
 */
export function formatFreeTextForDatatable(input: string | null | undefined): AvailableMLDTDisplayObjectTypes {
  const isBlank = !input || input.trim().length == 0;
  if (isBlank) {
    return MLDTDisplayObjectForEmptyString;
  }
  return {
    displayComponentName: MLDTDisplayComponentName.FreeTextDisplayComponent,
    displayValue: input,
  };
}
