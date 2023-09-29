import { type Field } from "@/utils/GenericFrameworkTypes";
import { type MLDTCellConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type AvailableDisplayValues } from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { plainStringValueGetterFactory } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import { yesNoValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { singleSelectValueGetterFactory } from "@/components/resources/dataTable/conversion/SingleSelectValueGetterFactory";
import { dataPointValueGetterFactory } from "@/components/resources/dataTable/conversion/DataPointValueGetterFactory";
import {naceCodeValueGetterFactory} from "@/components/resources/dataTable/conversion/NaceCodeValueGetterFactory";
import {numberValueGetterFactory} from "@/components/resources/dataTable/conversion/NumberValueGetterFactory";
import {percentageValueGetterFactory} from "@/components/resources/dataTable/conversion/PercentageValueGetterFactory";

// The effort of making this file type-safe greatly outweighs the benefit.
/* eslint @typescript-eslint/no-explicit-any: 0 */

type ValueGetterFactory = (path: string, field: Field) => (dataset: any) => AvailableDisplayValues;

const formFieldValueGetterFactoryMap: { [key: string]: ValueGetterFactory } = {
  AddressFormField: plainStringValueGetterFactory,
  DateFormField: plainStringValueGetterFactory,
  InputTextFormField: plainStringValueGetterFactory,
  YesNoFormField: yesNoValueGetterFactory,
  YesNoNaFormField: yesNoValueGetterFactory,
  RadioButtonsFormField: singleSelectValueGetterFactory,
  SingleSelectFormField: singleSelectValueGetterFactory,
  DataPointFormField: dataPointValueGetterFactory,
  NaceCodeFormField: naceCodeValueGetterFactory,
  NumberFormField: numberValueGetterFactory,
  PercentageFormField: percentageValueGetterFactory,
};

/**
 * Translates a Data-Model-Ts field to a Multi-Layer-Data-Table Cell configuration respecting
 * standardized formatting
 * @param path the path to the field
 * @param field the field
 * @returns the translated configuration
 */
export function getDataModelFieldDisplayConfiguration(path: string, field: Field): MLDTCellConfig<any> | undefined {
  if (field.component in formFieldValueGetterFactoryMap) {
    const valueGetterFactory = formFieldValueGetterFactoryMap[field.component];
    return {
      type: "cell",
      label: field.label,
      explanation: field.description,
      shouldDisplay: field.showIf,
      valueGetter: valueGetterFactory(path, field),
    };
  } else if (field.component == "UploadReports") {
    return undefined;
  } else {
    console.log(`!WARNING! - Could not translate component of type ${field.component}`);
    return undefined;
  }
}
