import { getKeycloakToken } from '@e2e/utils/Auth';
import { searchBasicCompanyInformationForDataType } from '@e2e/utils/GeneralApiUtils';
import { type CompanyIdAndName, DataTypeEnum } from '@clients/backend';
import { reader_name, reader_pw } from '@e2e/utils/Cypress';

/**
 * Fetches two test companies for use in e2e tests.
 * Returns a Cypress.Chainable resolving to a tuple of two CompanyIdAndName objects.
 */
export function fetchTestCompanies(): Cypress.Chainable<[CompanyIdAndName, CompanyIdAndName]> {
  return getKeycloakToken(reader_name, reader_pw)
    .then((token: string) => {
      return searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyNonFinancials);
    })
    .then((basicCompanyInfos) => {
      if (!basicCompanyInfos || basicCompanyInfos.length < 2) {
        throw new Error('Not enough companies found for test setup.');
      }
      const alphaCompanyIdAndName: CompanyIdAndName = {
        companyId: basicCompanyInfos[0].companyId,
        companyName: basicCompanyInfos[0].companyName,
      };
      const betaCompanyIdAndName: CompanyIdAndName = {
        companyId: basicCompanyInfos[1].companyId,
        companyName: basicCompanyInfos[1].companyName,
      };
      return [alphaCompanyIdAndName, betaCompanyIdAndName] as [CompanyIdAndName, CompanyIdAndName];
    });
}

/**
 * Sets up common network intercepts for company cockpit E2E tests.
 * Call this in a beforeEach block.
 */
export function setupCommonInterceptions(): void {
  cy.intercept('https://youtube.com/**', []);
  cy.intercept('https://jnn-pa.googleapis.com/**', []);
  cy.intercept('https://play.google.com/**', []);
  cy.intercept('https://googleads.g.doubleclick.net/**', []);
}
