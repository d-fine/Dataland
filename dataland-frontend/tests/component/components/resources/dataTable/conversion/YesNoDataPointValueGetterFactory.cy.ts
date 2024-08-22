import { type Field } from '@/utils/GenericFrameworkTypes';
import {
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { YesNoNa } from '@clients/backend';
import type { BaseDataPointYesNoNa } from '@clients/backend';
import { yesNoDataPointValueGetterFactory } from '@/components/resources/dataTable/conversion/YesNoDataPointValueGetterFactory';
import { NO_DATA_PROVIDED } from '@/utils/Constants';

describe('Unit test for the YesNoDataPointValueGetterFactory', () => {
  describe('Tests when the data provides a simple data source', () => {
    const baseFieldCertificate: Field = {
      name: 'environmentalManagementSystemNationalCertification',
      label: 'Environmental Management System National Certification',
      description: 'Is the environmental management system nationally recognized and certified?',
      unit: '',
      component: 'YesNoNaFormField',
      required: false,
      showIf: (): boolean => true,
    };

    const baseFieldNoCertificate: Field = {
      name: 'normal-field',
      label: 'Just a normal field',
      description: 'No certificate here',
      unit: '',
      component: 'YesNoNaFormField',
      required: false,
      showIf: (): boolean => true,
    };

    it('An empty string should be displayed if the data point is undefined', () => {
      const dataset = { data: undefined };
      const value = yesNoDataPointValueGetterFactory('data', baseFieldCertificate)(dataset);
      expect(value).to.deep.equal(MLDTDisplayObjectForEmptyString);
    });

    it("An empty string should be displayed if the data point's value is undefined", () => {
      const datapoint = {};
      const dataset = { data: datapoint };
      const value = yesNoDataPointValueGetterFactory('data', baseFieldCertificate)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: NO_DATA_PROVIDED,
      });
    });

    it("'N/A' should be displayed if the value is N/A", () => {
      const datapoint: BaseDataPointYesNoNa = {
        value: YesNoNa.Na,
      };
      const dataset = { data: datapoint };
      const value = yesNoDataPointValueGetterFactory('data', baseFieldCertificate)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: 'N/A',
      });
    });

    describe("Tests cases for when 'certificate' is in the field name", () => {
      it("'Certified' with a link to the document should be displayed if a certificate is provided and the value is Yes", () => {
        const datapoint: BaseDataPointYesNoNa = {
          value: YesNoNa.Yes,
          dataSource: {
            fileName: 'Hello',
            fileReference: 'TestReference',
          },
        };
        const dataset = { data: datapoint };
        const value = yesNoDataPointValueGetterFactory('data', baseFieldCertificate)(dataset);
        expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.DocumentLinkDisplayComponent>>{
          displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
          displayValue: {
            label: 'Certified',
            dataSource: {
              fileReference: 'TestReference',
              fileName: 'Hello',
            },
          },
        });
      });
      it("'Uncertified' should be displayed if no certificate is provided and the value is No", () => {
        const datapoint: BaseDataPointYesNoNa = {
          value: YesNoNa.No,
        };
        const dataset = { data: datapoint };
        const value = yesNoDataPointValueGetterFactory('data', baseFieldCertificate)(dataset);
        expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent as string,
          displayValue: 'Uncertified',
        });
      });
    });

    describe("Test cases for when 'certificate' is not in the field name", () => {
      it("'Yes' should be displayed if no certificate is provided", () => {
        const datapoint: BaseDataPointYesNoNa = {
          value: YesNoNa.Yes,
        };
        const dataset = { data: datapoint };
        const value = yesNoDataPointValueGetterFactory('data', baseFieldNoCertificate)(dataset);
        expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
          displayComponentName: MLDTDisplayComponentName.StringDisplayComponent as string,
          displayValue: 'Yes',
        });
      });
    });
  });

  describe('Tests when the field provides a datasource property set', () => {
    const field: Field = {
      name: 'environmentalManagementSystemNationalCertification',
      label: 'Environmental Management System National Certification',
      description: 'Is the environmental management system nationally recognized and certified?',
      unit: '',
      component: 'YesNoNaFormField',
      required: false,
      showIf: (): boolean => true,
    };

    it("'Certified' should be displayed if the value is Yes", () => {
      const datapoint: BaseDataPointYesNoNa = {
        value: YesNoNa.Yes,
        dataSource: {
          fileName: 'Hello',
          fileReference: 'TestReference',
        },
      };
      const dataset = { data: datapoint };
      const value = yesNoDataPointValueGetterFactory('data', field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.DocumentLinkDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
        displayValue: {
          label: 'Certified',
          dataSource: datapoint.dataSource,
        },
      });
    });

    it("'Uncertified' should be displayed if the value is No", () => {
      const datapoint: BaseDataPointYesNoNa = {
        value: YesNoNa.No,
        dataSource: {
          fileName: 'Hello',
          fileReference: 'TestReference',
        },
      };
      const dataset = { data: datapoint };
      const value = yesNoDataPointValueGetterFactory('data', field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.DocumentLinkDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
        displayValue: {
          label: 'Uncertified',
          dataSource: datapoint.dataSource,
        },
      });
    });

    it("'N/A' should be displayed if the value is NA", () => {
      const datapoint = {
        value: YesNoNa.Na,
        dataSource: {
          fileName: 'Hello',
          fileReference: 'TestReference',
        },
      };
      const dataset = { data: datapoint };
      const value = yesNoDataPointValueGetterFactory('data', field)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.DocumentLinkDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
        displayValue: {
          label: 'N/A',
          dataSource: datapoint.dataSource,
        },
      });
    });
  });
});
