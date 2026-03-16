import {
  IdentifierType,
  type CompanyInformation,
  type DataMetaInformation,
  DataTypeEnum,
  QaStatus,
  type BasicCompanyInformation,
} from '@clients/backend';

/**
 * Method to prepare an array consisting of one simple dataset to be displayed in the data table of the "AVAILABLE
 * DATASETS" page
 * @param no_iterations no of iterations, how many mock dataset to generate
 * @returns The dummy BasicCompanyInformation instance as an array with one entry
 */
export function prepareSimpleDataSearchStoredCompanyArray(no_iterations = 1): BasicCompanyInformation[] {
  const mockCompanyInformation: CompanyInformation = {
    companyName: 'testCompany',
    headquarters: 'testHeadquarters',
    sector: 'testSector',
    identifiers: {
      [IdentifierType.Lei]: ['testLei'],
    },
    countryCode: 'DE',
  };

  const mockDataMetaInformation = {
    dataId: 'testDataId',
    companyId: 'testCompanyId',
    dataType: DataTypeEnum.Lksg,
    uploadTime: 1672527600000, // 1.1.2023 00:00:00:0000
    reportingPeriod: '2023',
    currentlyActive: true,
    qaStatus: QaStatus.Accepted,
  } as DataMetaInformation;
  const oneMockCompany: BasicCompanyInformation = {
    companyId: mockDataMetaInformation.companyId,
    companyName: mockCompanyInformation.companyName,
    lei: mockCompanyInformation.identifiers[IdentifierType.Lei][0],
    sector: mockCompanyInformation.sector,
    headquarters: mockCompanyInformation.headquarters,
    countryCode: mockCompanyInformation.countryCode,
  };
  const result = [];
  for (let i = 0; i < no_iterations; i++) {
    result.push(oneMockCompany);
  }

  return result;
}
