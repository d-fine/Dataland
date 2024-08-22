import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { numberValueGetterFactory } from '@/components/resources/dataTable/conversion/NumberValueGetterFactory';
import { type Field } from '@/utils/GenericFrameworkTypes';

describe('Unit test for the NumberValueGetterFactory', () => {
  const sampleNumberFormFieldWithUnit: Field = {
    name: 'numberFormFieldTest',
    label: 'Some Label For Number Field',
    component: 'NumberFormField',
    showIf: () => true,
    description: 'This is a dummy field for the test',
    unit: 'someUnit',
  };

  const sampleNumberFormFieldWithoutUnit: Field = { ...sampleNumberFormFieldWithUnit };
  sampleNumberFormFieldWithoutUnit.unit = undefined;

  it('An empty string should be displayed if the data point is undefined', () => {
    const dataset = { data: undefined };
    const value = numberValueGetterFactory('data', sampleNumberFormFieldWithUnit)(dataset);
    expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it('The value of the input should be displayed together with its unit if it exists', () => {
    const dataset = { data: 10 };
    const value = numberValueGetterFactory('data', sampleNumberFormFieldWithUnit)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: '10 someUnit',
    });
  });

  it('The value of the input should be displayed rounded to two decimal places', () => {
    const dataset = { data: 10.223 };
    const value = numberValueGetterFactory('data', sampleNumberFormFieldWithUnit)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: '10.22 someUnit',
    });
  });

  it('The value of the input should contain thousands separators', () => {
    const dataset = { data: 1023 };
    const value = numberValueGetterFactory('data', sampleNumberFormFieldWithUnit)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: '1,023 someUnit',
    });
  });

  it('The value of the input should be displayed without a unit if it does not have one', () => {
    const dataset = { data: 1234023.333 };
    const value = numberValueGetterFactory('data', sampleNumberFormFieldWithoutUnit)(dataset);
    expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: '1,234,023.33',
    });
  });
});
