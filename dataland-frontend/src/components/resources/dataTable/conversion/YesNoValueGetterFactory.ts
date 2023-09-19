import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { type BaseDataPointYesNo } from "@clients/backend";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";

/**
 * Returns a value factory that returns the value of the Yes / No form field
 * If the form field requires certification, a link to the certificate is returned if available.
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function yesNoValueGetterFactory(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    if (field?.certificateRequiredIfYes == true) {
      const elementValue = getFieldValueFromDataModel(path, dataset) as BaseDataPointYesNo;
      if (elementValue.dataSource) {
        const dataSource = elementValue.dataSource;
        return {
          displayComponent: MLDTDisplayComponents.DocumentLinkDisplayComponent,
          displayValue: {
            label: elementValue.value + " (Certified)",
            reference: dataSource,
          },
        };
      }
    }

    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: getFieldValueFromDataModel(path, dataset) as string,
    };
  };
}
