import { type Field } from '@/utils/GenericFrameworkTypes';
import {
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type BaseDocumentReference, type ExtendedDocumentReference, QualityOptions } from '@clients/backend';
import { type BaseDataPoint, type ExtendedDataPoint } from '@/utils/DataPoint';
import { getDataPointGetterFactory } from '@/components/resources/dataTable/conversion/DataPoints';
import { NO_DATA_PROVIDED } from '@/utils/Constants';

/**
 * Some formatting function for testing
 * @param dataPoint the data point whose value to format
 * @returns the formatted string
 */
function defaultFormatter(dataPoint?: ExtendedDataPoint<string>): string | undefined {
  if (dataPoint?.value == null) {
    return undefined;
  }
  return `${dataPoint?.value} Formatted`;
}

const dummyBaseDocumentReference: BaseDocumentReference = {
  fileReference: 'reference',
  fileName: 'name',
};

const dummyExtendedDocumentReference: ExtendedDocumentReference = {
  fileReference: 'reference',
  fileName: 'name',
  page: '8-9',
};

const dummyField: Field = {
  name: 'Field Name',
  description: 'Field Description',
  showIf: () => true,
  label: 'Field Label',
  component: 'YesNoBaseDataPointFormField',
};

describe('Unit test for the YesNoDataPointValueGetterFactory', () => {
  describe('Tests when the data provides a simple data source', () => {
    it('A formatted string should be displayed if only the value is provided', () => {
      const dataset = {
        data: <BaseDataPoint<string>>{
          value: 'Data',
        },
      };
      const value = getDataPointGetterFactory<string>('data', dummyField, defaultFormatter)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: 'Data Formatted',
      });
    });
    it('A formatted string and the data source should be displayed if a complete base data point is provided', () => {
      const dataset = {
        data: <BaseDataPoint<string>>{
          value: 'Data',
          dataSource: dummyBaseDocumentReference,
        },
      };
      const value = getDataPointGetterFactory<string>('data', dummyField, defaultFormatter)(dataset);
      expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.DocumentLinkDisplayComponent>>{
        displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
        displayValue: {
          label: 'Data Formatted',
          dataSource: dummyBaseDocumentReference,
        },
      });
    });
  });
  describe('Tests when the data is an extended data point', () => {
    it('A placeholder string should be displayed if no data point value is provided', () => {
      assertDataPointWithDataSourceGetsTheCorrectlyFormattedDisplayObject(undefined, NO_DATA_PROVIDED);
    });
    it('Data point display information should contain the datapoints meta information', () => {
      assertDataPointWithDataSourceGetsTheCorrectlyFormattedDisplayObject('Data', 'Data Formatted');
    });
  });
});

/**
 * Asserts that a data point with a data source and quality specified gets a getter
 * that returns a display object with display component DataPointDisplayComponent and correctly formatted value
 * @param dataPointValue the value the data point should have
 * @param expectedformattedValue the expected formatted value
 */
function assertDataPointWithDataSourceGetsTheCorrectlyFormattedDisplayObject(
  dataPointValue: string | undefined,
  expectedformattedValue: string
): void {
  const dataset = {
    data: <ExtendedDataPoint<string>>{
      value: dataPointValue,
      dataSource: dummyExtendedDocumentReference,
      quality: QualityOptions.Audited,
    },
  };
  const value = getDataPointGetterFactory<string>('data', dummyField, defaultFormatter)(dataset);
  expect(value).to.deep.equal(<MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
    displayValue: {
      fieldLabel: dummyField.label,
      value: expectedformattedValue,
      dataSource: dummyExtendedDocumentReference,
      quality: dataset.data.quality,
      comment: undefined,
    },
  });
}
