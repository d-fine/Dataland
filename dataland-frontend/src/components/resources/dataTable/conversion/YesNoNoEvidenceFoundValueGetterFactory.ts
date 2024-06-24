import { type YesNoNoEvidenceFound } from '@clients/backend';
import { HumanizedYesNoNoEvidenceFound } from '@/utils/YesNoNa';
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';

/**
 * Formats the provided Yes/No/NoEvidenceFound value for the data-table
 * @param value the value to display
 * @returns the value formatted for display
 */
export function formatYesNoNoEvidenceFoundValueForDatatable(
  value: YesNoNoEvidenceFound | undefined | null
): AvailableMLDTDisplayObjectTypes {
  const displayValue = value ? HumanizedYesNoNoEvidenceFound[value] : '';
  return {
    displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
    displayValue: displayValue,
  };
}

/**
 * Returns a value factory that returns the value of the Yes/No/NoEvidenceFound form field
 * @param path the path to the field
 * @returns the created getter
 */
export function yesNoNoEvidenceFoundValueGetterFactory(
  path: string
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    return formatYesNoNoEvidenceFoundValueForDatatable(
      getFieldValueFromFrameworkDataset(path, dataset) as YesNoNoEvidenceFound | null | undefined
    );
  };
}
