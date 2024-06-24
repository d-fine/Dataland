import { assertDefined } from '@/utils/TypeScriptUtils';
import {
  type CompanyAssociatedDataEuTaxonomyDataForFinancials,
  DataTypeEnum,
  type EuTaxonomyDataForFinancials,
} from '@clients/backend';
import { TEST_PDF_FILE_NAME, TEST_PDF_FILE_PATH } from '@sharedUtils/ConstantsForPdfs';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { type CyHttpMessages } from 'cypress/types/net-stubbing';
import { goToEditFormOfMostRecentDatasetForCompanyAndFramework } from './GeneralUtils';

/**
 * Extracts the first eutaxonomy-financials dataset from the fake fixtures
 * @returns the first eutaxonomy-financials dataset from the fake fixtures
 */
export function getFirstEuTaxonomyFinancialsFixtureDataFromFixtures(): Cypress.Chainable<
  FixtureData<EuTaxonomyDataForFinancials>
> {
  return cy.fixture('CompanyInformationWithEuTaxonomyDataForFinancials').then(function (jsonContent) {
    const companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    return companiesWithEuTaxonomyDataForFinancials[0];
  });
}

/**
 * Visits the edit page for the eu taxonomy dataset for financial companies via navigation.
 * @param companyId the id of the company for which to edit a dataset
 * @param expectIncludedFile specifies if the test file is expected to be in the server response
 */
export function gotoEditForm(companyId: string, expectIncludedFile: boolean): void {
  goToEditFormOfMostRecentDatasetForCompanyAndFramework(companyId, DataTypeEnum.EutaxonomyFinancials).then(
    (interception) => {
      const referencedReports = assertDefined(
        (interception?.response?.body as CompanyAssociatedDataEuTaxonomyDataForFinancials)?.data?.referencedReports
      );
      expect(TEST_PDF_FILE_NAME in referencedReports).to.equal(expectIncludedFile);
      expect(`${TEST_PDF_FILE_NAME}2` in referencedReports).to.equal(true);
    }
  );
}

/**
 * This method verifies that uploaded reports are downloadable
 * @param companyId the ID of the company whose data to view
 */
export function checkIfLinkedReportsAreDownloadable(companyId: string): void {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);
  const expectedPathToDownloadedReport = Cypress.config('downloadsFolder') + `/${TEST_PDF_FILE_NAME}.pdf`;
  const downloadLinkSelector = `span[data-test="Report-Download-${TEST_PDF_FILE_NAME}"]`;
  cy.readFile(expectedPathToDownloadedReport).should('not.exist');
  cy.intercept('**/documents/*').as('documentDownload');
  cy.get(`[data-test="report-link-${TEST_PDF_FILE_NAME}"]`).click();
  cy.get(downloadLinkSelector).click();
  cy.wait('@documentDownload');
  cy.readFile(`../${TEST_PDF_FILE_PATH}`, 'binary', {
    timeout: Cypress.env('medium_timeout_in_ms') as number,
  }).then((expectedPdfBinary) => {
    cy.task('calculateHash', expectedPdfBinary).then((expectedPdfHash) => {
      cy.readFile(expectedPathToDownloadedReport, 'binary', {
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      }).then((receivedPdfHash) => {
        cy.task('calculateHash', receivedPdfHash).should('eq', expectedPdfHash);
      });
      cy.task('deleteFolder', Cypress.config('downloadsFolder'));
    });
  });
}

/**
 * After a Eu Taxonomy financial or non financial form has been filled in this function submits the form and checks
 * if a 200 response is returned by the backend
 * @param submissionDataIntercept function that asserts content of an intercepted request
 */
export function submitFilledInEuTaxonomyForm(
  submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void
): void {
  const postRequestAlias = 'postDataAlias';
  cy.intercept(
    {
      method: 'POST',
      url: `**/api/data/**`,
      times: 1,
    },
    submissionDataIntercept
  ).as(postRequestAlias);
  cy.get('button[data-test="submitButton"]').click();
  cy.wait(`@${postRequestAlias}`, { timeout: Cypress.env('long_timeout_in_ms') as number }).then((interception) => {
    expect(interception.response?.statusCode).to.eq(200);
  });
  cy.contains('td', 'EU Taxonomy');
}
