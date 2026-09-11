import { DataTypeEnum, type EutaxonomyFinancialsData } from '@clients/backend';
import { TEST_PDF_FILE_NAME } from '@sharedUtils/ConstantsForPdfs';
import { type FixtureData } from '@sharedUtils/Fixtures';

/**
 * Extracts the first eutaxonomy-financials dataset from the fake fixtures
 * @returns the first eutaxonomy-financials dataset from the fake fixtures
 */
export function getFirstEuTaxonomyFinancialsFixtureDataFromFixtures(): Cypress.Chainable<
  FixtureData<EutaxonomyFinancialsData>
> {
  return cy.fixture('CompanyInformationWithEutaxonomyFinancialsData').then(function (jsonContent) {
    const companiesWithEuTaxonomyFinancialsData = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
    return companiesWithEuTaxonomyFinancialsData[0];
  });
}

/**
 * This method verifies that uploaded reports are downloadable
 * @param companyId the ID of the company whose data to view
 */
export function checkIfLinkedReportsAreDownloadable(companyId: string): void {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);
  cy.intercept('**/documents/*').as('documentDownload');
  const downloadLinkSelector = `[data-test="download-link-${TEST_PDF_FILE_NAME}"]`;
  cy.get(`[data-test="report-link-${TEST_PDF_FILE_NAME}"]`).click();
  cy.get(downloadLinkSelector).click();
  cy.wait('@documentDownload');
  cy.get(`a[data-test="report-${TEST_PDF_FILE_NAME}-link`);
}
