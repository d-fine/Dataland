import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { plainStringValueGetterFactory } from '@/components/resources/dataTable/conversion/PlainStringValueGetterFactory';
describe('Unit test for the PlainStringValueGetterFactory', () => {
  it('An empty string should be displayed if the data point is undefined', () => {
    const dataset = { data: undefined };
    const value = plainStringValueGetterFactory('data')(dataset);
    expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it('The value of the input should be displayed as a string if defined', () => {
    const dataset = { data: 'Hello there' };
    const value = plainStringValueGetterFactory('data')(dataset);
    expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: 'Hello there',
    });
  });
});
