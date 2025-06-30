import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { multiSelectValueGetterFactory } from '@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory';
import { type Field } from '@/utils/GenericFrameworkTypes';

describe('Unit test for the MultiSelectValueGetterFactory', () => {
  const sampleMultiSelectFormField: Field = {
    name: 'MultiSelectFormField-Test',
    label: 'MultiSelectFormField-Test',
    component: 'MultiSelectFormField',
    showIf: () => true,
    description: 'Test-Field',
    options: [
      {
        label: 'Option A Label',
        value: 'A',
      },
      {
        label: 'Option B Label',
        value: 'B',
      },
    ],
  };

  it('An empty string should be displayed if the data point is undefined', () => {
    const dataset = { data: undefined };
    const value = multiSelectValueGetterFactory('data', sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it('An empty string should be displayed if the data point has no values', () => {
    const dataset = { data: [] };
    const value = multiSelectValueGetterFactory('data', sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it('A Link to a MultiSelectModal should be displayed if there is one value to display', () => {
    const dataset = { data: ['A'] };
    const value = multiSelectValueGetterFactory('data', sampleMultiSelectFormField)(dataset);
    expect(value).to.have.property('displayComponentName', MLDTDisplayComponentName.ModalLinkDisplayComponent);
    expect(value).to.have.nested.property('displayValue.label', 'Show 1 value');
    expect(value).to.have.deep.nested.property('displayValue.modalOptions.data', {
      label: 'MultiSelectFormField-Test',
      values: ['Option A Label'],
    });
  });

  it('A Link to a MultiSelectModal should be displayed with a plural s if there is more than 1 value', () => {
    const dataset = { data: ['A', 'B'] };
    const value = multiSelectValueGetterFactory('data', sampleMultiSelectFormField)(dataset);
    expect(value).to.have.property('displayComponentName', MLDTDisplayComponentName.ModalLinkDisplayComponent);
    expect(value).to.have.nested.property('displayValue.label', 'Show 2 values');
    expect(value).to.have.deep.nested.property('displayValue.modalOptions.data', {
      label: 'MultiSelectFormField-Test',
      values: ['Option A Label', 'Option B Label'],
    });
  });
});
