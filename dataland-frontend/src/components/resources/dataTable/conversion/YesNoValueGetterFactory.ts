import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type YesNoNa } from "@clients/backend";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";
import { HumanizedYesNoNa } from "@/utils/YesNoNa";

/**
 * Returns a value factory that returns the value of the Yes / No form field
 * @param path the path to the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function yesNoValueGetterFactory(path: string): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const value = getFieldValueFromFrameworkDataset(path, dataset) as YesNoNa | undefined;
    const displayValue = value ? HumanizedYesNoNa[value] : "";
    return {
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: displayValue,
    };
  };
}
