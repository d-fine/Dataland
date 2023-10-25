import { type Field } from "@/utils/GenericFrameworkTypes";
import { type MLDTCellConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { plainStringValueGetterFactory } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import { yesNoValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { yesNoBaseDataPointValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoBaseDataPointValueGetterFactory";
import { yesNoExtendedDataPointValueGetterFactory } from "@/components/resources/dataTable/conversion/YesNoExtendedDataPointValueGetterFactory";
import { singleSelectValueGetterFactory } from "@/components/resources/dataTable/conversion/SingleSelectValueGetterFactory";
import { dataPointValueGetterFactory } from "@/components/resources/dataTable/conversion/DataPointValueGetterFactory";
import { naceCodeValueGetterFactory } from "@/components/resources/dataTable/conversion/NaceCodeValueGetterFactory";
import { numberValueGetterFactory } from "@/components/resources/dataTable/conversion/NumberValueGetterFactory";
import { percentageValueGetterFactory } from "@/components/resources/dataTable/conversion/PercentageValueGetterFactory";
import { multiSelectValueGetterFactory } from "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory";
import { getModalGetterFactory } from "@/components/resources/dataTable/conversion/GenericModalValueGetterFactory";
import { lksgModalColumnHeaders } from "@/components/resources/frameworkDataSearch/lksg/LksgModalColumnHeaders";
import { lksgProcurementCategoriesValueGetterFactory } from "@/components/resources/dataTable/conversion/lksg/LksgProcurementCategoriesValueGetterFactory";
import { p2pDriveMixValueGetterFactory } from "@/components/resources/dataTable/conversion/p2p/P2pDriveMixValueGetterFactory";
import { highImpactClimateGetterFactory } from "@/components/resources/dataTable/conversion/HighImpactClimateGetterFactory";

// The effort of making this file type-safe greatly outweighs the benefit.
/* eslint @typescript-eslint/no-explicit-any: 0 */

type ValueGetterFactory = (path: string, field: Field) => (dataset: any) => AvailableMLDTDisplayObjectTypes;

const formFieldValueGetterFactoryMap: { [key: string]: ValueGetterFactory } = {
  AddressFormField: plainStringValueGetterFactory,
  DateFormField: plainStringValueGetterFactory,
  InputTextFormField: plainStringValueGetterFactory,
  YesNoFormField: yesNoValueGetterFactory,
  YesNoBaseDataPointFormField: yesNoBaseDataPointValueGetterFactory,
  YesNoExtendedDataPointFormField: yesNoExtendedDataPointValueGetterFactory,
  YesNoNaFormField: yesNoValueGetterFactory,
  YesNoNaBaseDataPointFormField: yesNoBaseDataPointValueGetterFactory,
  RadioButtonsFormField: singleSelectValueGetterFactory,
  SingleSelectFormField: singleSelectValueGetterFactory,
  DataPointFormField: dataPointValueGetterFactory,
  NaceCodeFormField: naceCodeValueGetterFactory,
  NumberFormField: numberValueGetterFactory,
  PercentageFormField: percentageValueGetterFactory,
  MultiSelectFormField: multiSelectValueGetterFactory,
  ProductionSitesFormField: getModalGetterFactory("listOfProductionSites", lksgModalColumnHeaders),
  MostImportantProductsFormField: getModalGetterFactory("mostImportantProducts", lksgModalColumnHeaders),
  ProcurementCategoriesFormField: lksgProcurementCategoriesValueGetterFactory,
  DriveMixFormField: p2pDriveMixValueGetterFactory,
  HighImpactClimateSectorsFormField: highImpactClimateGetterFactory,
};

/**
 * Translates a Data-Model-Ts field to a Multi-Layer-Data-Table Cell configuration respecting
 * standardized formatting
 * @param path the path to the field
 * @param field the field
 * @returns the translated configuration
 */
export function getDataModelFieldCellConfig(path: string, field: Field): MLDTCellConfig<any> | undefined {
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
