import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { getFieldValueFromFrameworkDataset } from '@/components/resources/dataTable/conversion/Utils';
import { type Field } from '@/utils/GenericFrameworkTypes';

/**
 * Returns a value factory that returns the value of the field as a string using the display mapping in the options field
 * If the value is non-truthy, an empty string is returned
 * @param path the path to the field
 * @param field the single select field
 * @returns the created getter
 */
export function singleSelectValueGetterFactory(
  path: string,
  field: Field
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const selectedElement = getFieldValueFromFrameworkDataset(path, dataset) as string | undefined;
    if (!selectedElement) {
      return MLDTDisplayObjectForEmptyString;
    }

    const matchingOption = field.options?.find((it) => it.value == selectedElement);
    if (matchingOption) {
      return {
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: matchingOption.label,
      };
    } else {
      return {
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: selectedElement,
      };
    }
  };
}
