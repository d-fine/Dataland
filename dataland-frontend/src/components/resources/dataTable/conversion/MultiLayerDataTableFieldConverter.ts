import { type Field, type FrameworkData } from '@/utils/GenericFrameworkTypes';
import { type MLDTCellConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import { type AvailableMLDTDisplayObjectTypes } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { plainStringValueGetterFactory } from '@/components/resources/dataTable/conversion/PlainStringValueGetterFactory';
import { yesNoValueGetterFactory } from '@/components/resources/dataTable/conversion/YesNoValueGetterFactory';
import { yesNoDataPointValueGetterFactory } from '@/components/resources/dataTable/conversion/YesNoDataPointValueGetterFactory';
import { currencyDataPointValueGetterFactory } from '@/components/resources/dataTable/conversion/CurrencyDataPointValueGetterFactory';
import { numberDataPointValueGetterFactory } from '@/components/resources/dataTable/conversion/NumberDataPointValueGetterFactory';
import { singleSelectValueGetterFactory } from '@/components/resources/dataTable/conversion/SingleSelectValueGetterFactory';
import { naceCodeValueGetterFactory } from '@/components/resources/dataTable/conversion/NaceCodeValueGetterFactory';
import { numberValueGetterFactory } from '@/components/resources/dataTable/conversion/NumberValueGetterFactory';
import { percentageValueGetterFactory } from '@/components/resources/dataTable/conversion/PercentageValueGetterFactory';
import { multiSelectValueGetterFactory } from '@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory';
import { getModalGetterFactory } from '@/components/resources/dataTable/conversion/GenericModalValueGetterFactory';
import { lksgModalColumnHeaders } from '@/components/resources/frameworkDataSearch/lksg/LksgModalColumnHeaders';
import { p2pDriveMixValueGetterFactory } from '@/components/resources/dataTable/conversion/p2p/P2pDriveMixValueGetterFactory';
import { vsmeModalColumnHeaders } from '@/components/resources/dataTable/conversion/vsme/VsmeDisplayValueGetters';

// The effort of making this file type-safe greatly outweighs the benefit.
/* eslint @typescript-eslint/no-explicit-any: 0 */

type ValueGetterFactory = (path: string, field: Field) => (dataset: any) => AvailableMLDTDisplayObjectTypes;

const formFieldValueGetterFactoryMap: { [key: string]: ValueGetterFactory } = {
  AddressFormField: plainStringValueGetterFactory,
  DateFormField: plainStringValueGetterFactory,
  InputTextFormField: plainStringValueGetterFactory,
  RadioButtonsFormField: singleSelectValueGetterFactory,
  SingleSelectFormField: singleSelectValueGetterFactory,
  NaceCodeFormField: naceCodeValueGetterFactory,
  NumberFormField: numberValueGetterFactory,
  PercentageFormField: percentageValueGetterFactory,
  MultiSelectFormField: multiSelectValueGetterFactory,
  ProductionSitesFormField: getModalGetterFactory('listOfProductionSites', lksgModalColumnHeaders),
  MostImportantProductsFormField: getModalGetterFactory('mostImportantProducts', lksgModalColumnHeaders),
  DriveMixFormField: p2pDriveMixValueGetterFactory,
  YesNoFormField: yesNoValueGetterFactory,
  YesNoBaseDataPointFormField: yesNoDataPointValueGetterFactory,
  YesNoExtendedDataPointFormField: yesNoDataPointValueGetterFactory,
  YesNoNaFormField: yesNoValueGetterFactory,
  YesNoNaBaseDataPointFormField: yesNoDataPointValueGetterFactory,
  IntegerExtendedDataPointFormField: numberDataPointValueGetterFactory,
  BigDecimalExtendedDataPointFormField: numberDataPointValueGetterFactory,
  CurrencyDataPointFormField: currencyDataPointValueGetterFactory,
  SubsidiaryFormField: getModalGetterFactory('listOfSubsidiary', vsmeModalColumnHeaders),
  PollutionEmissionFormField: getModalGetterFactory('pollutionEmission', vsmeModalColumnHeaders),
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
    const valueGetter = valueGetterFactory(path, field);
    return {
      type: 'cell',
      label: field.label,
      explanation: field.description,
      shouldDisplay: (dataset: FrameworkData) => field.showIf(dataset),
      valueGetter: valueGetter,
    };
  } else if (field.component == 'UploadReports') {
    return undefined;
  } else {
    console.log(`!WARNING! - Could not translate component of type ${field.component}`);
    return undefined;
  }
}
