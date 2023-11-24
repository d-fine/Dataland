import { type CompanyInformation, IdentifierType } from "@clients/backend";
import { type DataSearchStoredCompany } from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";
import { generateCompanyInformation } from "@e2e/fixtures/CompanyFixtures";
import { DataMetaInformationGenerator } from "@e2e/fixtures/data_meta_information/DataMetaInformationFixtures";

/**
 * Generates a single search result for the framework data search
 * @returns the object representing the search result
 */
function generateDataSearchStoredCompany(): DataSearchStoredCompany {
  const mockCompanyInformation: CompanyInformation = generateCompanyInformation();
  const mockDataMetaInformation = new DataMetaInformationGenerator().generateDataMetaInformation();
  return {
    companyName: mockCompanyInformation.companyName,
    companyInformation: mockCompanyInformation,
    companyId: mockDataMetaInformation.companyId,
    permId: mockCompanyInformation.identifiers[IdentifierType.PermId][0],
    dataRegisteredByDataland: [mockDataMetaInformation],
  };
}

/**
 * Prepares an array consisting of search results to be displayed in the framework data search
 * @returns the list of search result objects
 */
export function generateListOfDataSearchStoredCompany(): DataSearchStoredCompany[] {
  return Array.from({ length: 100 }, () => ({ ...generateDataSearchStoredCompany() }));
}
