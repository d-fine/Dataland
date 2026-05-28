import { getReaderToken } from '@e2e/utils/Auth';
import { searchBasicCompanyInformationForDataType } from '@e2e/utils/GeneralApiUtils';
import { type CompanyIdAndName, DataTypeEnum } from '@clients/backend';

/**
 * Fetches two test companies for use in e2e tests.
 * Alpha is the first result. Beta is the first company whose name appears exactly once
 * in the result set, ensuring searching for beta's name in the autocomplete returns only
 * one result and the correct company is always clicked.
 * Returns a Cypress.Chainable resolving to a tuple of two CompanyIdAndName objects.
 */
export function fetchTestCompanies(): Cypress.Chainable<[CompanyIdAndName, CompanyIdAndName]> {
  return getReaderToken()
    .then((token: string) => {
      return searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyNonFinancials);
    })
    .then((basicCompanyInfos) => {
      const alphaCompanyIdAndName: CompanyIdAndName = {
        companyId: basicCompanyInfos[0].companyId,
        companyName: basicCompanyInfos[0].companyName,
      };
      const nameCounts = new Map<string, number>();
      for (const info of basicCompanyInfos) {
        nameCounts.set(info.companyName, (nameCounts.get(info.companyName) ?? 0) + 1);
      }
      const betaInfo = basicCompanyInfos.find(
        (info) => info.companyId !== alphaCompanyIdAndName.companyId && nameCounts.get(info.companyName) === 1
      );
      if (!betaInfo) {
        throw new Error(
          'fetchTestCompanies: could not find a second company with a unique name. ' +
            'All returned companies share names, which makes the navigation test unreliable.'
        );
      }
      const betaCompanyIdAndName: CompanyIdAndName = {
        companyId: betaInfo.companyId,
        companyName: betaInfo.companyName,
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
