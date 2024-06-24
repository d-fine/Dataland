import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type Field } from '@/utils/GenericFrameworkTypes';
import { singleSelectValueGetterFactory } from '@/components/resources/dataTable/conversion/SingleSelectValueGetterFactory';
describe('Unit test for the SingleSelectValueGetterFactory', () => {
  const field: Field = {
    name: 'fiscalYearDeviation',
    label: 'Fiscal Year Deviation',
    description: 'Does the fiscal year deviate from the calendar year?',
    unit: '',
    component: 'RadioButtonsFormField',
    options: [
      {
        label: 'Deviation',
        value: 'Deviation',
      },
      {
        label: 'No Deviation',
        value: 'NoDeviation',
      },
    ],
    required: true,
    showIf: (): boolean => true,
    validation: 'required',
  };

  it('An empty string should be displayed if the data point is undefined', () => {
    const dataset = { data: undefined };
    const value = singleSelectValueGetterFactory('data', field)(dataset);
    expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it('The human-readable name of the field should be displayed otherwise', () => {
    const dataset = { data: 'NoDeviation' };
    const value = singleSelectValueGetterFactory('data', field)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: 'No Deviation',
    });
  });
  it('The raw value of the input should be displayed as a string if the option is unknown', () => {
    const dataset = { data: 'Hello there' };
    const value = singleSelectValueGetterFactory('data', field)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: 'Hello there',
    });
  });
});
