import type {
  ExtendedDataPointNuclearAndGasNonEligible,
  ExtendedDocumentReference,
  NuclearAndGasNonEligible,
} from '@clients/backend';
import { formatNuclearAndGasTaxonomyShareDataForTable } from '@/components/resources/dataTable/conversion/NuclearAndGasValueGetterFactory.ts';
import {
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';

describe('formatNuclearAndGasTaxonomyShareDataForTable - Unit Tests', () => {
  const fieldLabel = 'Test Field Label';

  it('should return DataPointWrapperDisplayComponent when value is null and auxiliary data exists', () => {
    const mockDataSource: ExtendedDocumentReference = {
      fileReference: 'mock-file-ref-123.pdf',
      fileName: 'Mock Source File',
      page: '5',
      tagName: 'Section 1.2',
      publicationDate: '2023-10-01',
    };
    const mockAuxiliaryDataPoint: ExtendedDataPointNuclearAndGasNonEligible = {
      value: null,
      dataSource: mockDataSource,
      comment: 'Mock comment for auxiliary data test',
      quality: 'Audited', // adjust if this is an enum or specific type
    };

    const result = formatNuclearAndGasTaxonomyShareDataForTable(mockAuxiliaryDataPoint, fieldLabel);

    expect(result.displayComponentName).to.equal(MLDTDisplayComponentName.DataPointWrapperDisplayComponent);
  });

  it('should return MLDTDisplayObjectForEmptyString when value and auxiliary data are all null', () => {
    const mockData = {
      value: null,
      dataSource: null,
      comment: null,
      quality: null,
    };

    const result = formatNuclearAndGasTaxonomyShareDataForTable(mockData, fieldLabel);
    expect(result).to.equal(MLDTDisplayObjectForEmptyString);
  });

  it('should return ModalLinkWithDataSourceDisplayComponent when value exists', () => {
    const mockValue: NuclearAndGasNonEligible = {
      taxonomyNonEligibleShareNAndG426: 12.5,
      taxonomyNonEligibleShareNAndG427: 15.0,
      taxonomyNonEligibleShareNAndG428: null,
      taxonomyNonEligibleShareNAndG429: null,
      taxonomyNonEligibleShareNAndG430: null,
      taxonomyNonEligibleShareNAndG431: null,
      taxonomyNonEligibleShareOtherActivities: null,
      taxonomyNonEligibleShare: 27.5,
    };

    const mockDataWithValue: ExtendedDataPointNuclearAndGasNonEligible = {
      value: mockValue,
      dataSource: null,
      comment: null,
      quality: null,
    };

    const result = formatNuclearAndGasTaxonomyShareDataForTable(mockDataWithValue, fieldLabel);

    expect(result.displayComponentName).to.equal(MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent);
  });
});
