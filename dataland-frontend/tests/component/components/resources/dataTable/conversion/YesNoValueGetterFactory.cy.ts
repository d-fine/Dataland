import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { YesNoNa } from '@clients/backend';
import { yesNoValueGetterFactory } from '@/components/resources/dataTable/conversion/YesNoValueGetterFactory';

describe('Unit test for the YesNoValueGetterFactory', () => {
  describe('Tests for the simple field', () => {
    it('An empty string should be displayed if the data point is undefined', () => {
      const dataset = { data: undefined };
      const value = yesNoValueGetterFactory('data')(dataset);
      expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
    });

    it("'Yes' should be displayed if the value is Yes", () => {
      const dataset = { data: YesNoNa.Yes };
      const value = yesNoValueGetterFactory('data')(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: 'Yes',
      });
    });

    it("'No' should be displayed if the value is No", () => {
      const dataset = { data: YesNoNa.No };
      const value = yesNoValueGetterFactory('data')(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: 'No',
      });
    });

    it("'N/A' should be displayed if the value is NA", () => {
      const dataset = { data: YesNoNa.Na };
      const value = yesNoValueGetterFactory('data')(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: 'N/A',
      });
    });
  });
});
