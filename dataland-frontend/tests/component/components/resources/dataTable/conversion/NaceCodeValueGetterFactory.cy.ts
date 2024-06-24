import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { naceCodeValueGetterFactory } from '@/components/resources/dataTable/conversion/NaceCodeValueGetterFactory';
import { type Field } from '@/utils/GenericFrameworkTypes';

describe('Unit test for the MultiSelectValueGetterFactory', () => {
  const sampleMultiSelectFormField: Field = {
    name: 'NaceCodeFormField-Test',
    label: 'NaceCodeFormField-Test',
    component: 'NaceCodeFormField',
    showIf: () => true,
    description: 'Test-Field',
  };

  it('An empty string should be displayed if the data point is undefined', () => {
    const dataset = { data: undefined };
    const value = naceCodeValueGetterFactory('data', sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it('An empty string should be displayed if the data point has no values', () => {
    const dataset = { data: [] };
    const value = naceCodeValueGetterFactory('data', sampleMultiSelectFormField)(dataset);
    expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it('A Link to a MultiSelectModal should be displayed if there is one value to display', () => {
    const dataset = { data: ['A'] };
    const value = naceCodeValueGetterFactory('data', sampleMultiSelectFormField)(dataset);
    expect(value).to.have.property('displayComponentName', MLDTDisplayComponentName.ModalLinkDisplayComponent);
    expect(value).to.have.nested.property('displayValue.label', 'Show 1 NACE code');
    expect(value).to.have.deep.nested.property('displayValue.modalOptions.data', {
      label: 'NaceCodeFormField-Test',
      values: ['A - AGRICULTURE, FORESTRY AND FISHING'],
    });
  });

  it('A Link to a MultiSelectModal should be displayed with a plural s if there is more than 1 value', () => {
    const dataset = { data: ['A', 'B'] };
    const value = naceCodeValueGetterFactory('data', sampleMultiSelectFormField)(dataset);
    expect(value).to.have.property('displayComponentName', MLDTDisplayComponentName.ModalLinkDisplayComponent);
    expect(value).to.have.nested.property('displayValue.label', 'Show 2 NACE codes');
    expect(value).to.have.deep.nested.property('displayValue.modalOptions.data', {
      label: 'NaceCodeFormField-Test',
      values: ['A - AGRICULTURE, FORESTRY AND FISHING', 'B - MINING AND QUARRYING'],
    });
  });
});
