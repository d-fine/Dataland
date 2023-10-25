import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type YesNoNa } from "@clients/backend";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";

const humanReadableYesNoMap: { [key in YesNoNa]: string } = {
  Yes: "Yes",
  No: "No",
  NA: "N/A",
};

/**
 * Returns a value factory that returns the value of the Yes / No form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function yesNoValueGetterFactory(path: string, field: Field): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const value = getFieldValueFromFrameworkDataset(path, dataset) as YesNoNa | undefined;
    const displayValue = value ? humanReadableYesNoMap[value] : "";
    return {
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: displayValue,
    };
  };
}
