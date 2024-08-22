import { type CompanyInformation, IdentifierType, type BasicCompanyInformation } from '@clients/backend';
import { generateCompanyInformation } from '@e2e/fixtures/CompanyFixtures';
import { DataMetaInformationGenerator } from '@e2e/fixtures/data_meta_information/DataMetaInformationFixtures';

/**
 * Generates a single search result for the framework data search
 * @returns the object representing the search result
 */
function generateBasicCompanyInformation(): BasicCompanyInformation {
  const mockCompanyInformation: CompanyInformation = generateCompanyInformation();
  const mockDataMetaInformation = new DataMetaInformationGenerator().generateDataMetaInformation();
  return {
    companyName: mockCompanyInformation.companyName,
    companyId: mockDataMetaInformation.companyId,
    lei: mockCompanyInformation.identifiers[IdentifierType.Lei][0],
    headquarters: mockCompanyInformation.headquarters,
    sector: mockCompanyInformation.sector,
    countryCode: mockCompanyInformation.countryCode,
  };
}

/**
 * Prepares an array consisting of search results to be displayed in the framework data search
 * @returns the list of search result objects
 */
export function generateListOfDataSearchStoredCompany(): BasicCompanyInformation[] {
  return Array.from({ length: 100 }, () => ({ ...generateBasicCompanyInformation() }));
}
