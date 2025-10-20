import {
  reader_name,
  reader_pw,
  uploader_name,
  uploader_pw,
  reviewer_name,
  reviewer_pw,
  admin_name,
  admin_pw,
} from '@e2e/utils/Cypress';
import { DataTypeEnum } from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility';
import { uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateCompanyInformation } from '@e2e/fixtures/CompanyFixtures';

describeIf(
  'Check if each page is visitable if and only if the corresponding role is given',
  {
    executionEnvironments: ['developmentLocal', 'ci'],
  },
  () => {
    let readerAndUploaderPages = [] as string[];
    let uploaderOnlyPages = [] as string[];
    let reviewerOnlyPages = [] as string[];
    let companyId: string;
    const noPermissionMessage = "h1:contains('You do not have permission')";

    before(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const storedCompany = await uploadCompanyViaApi(token, generateCompanyInformation());
        companyId = storedCompany.companyId;
        readerAndUploaderPages = [
          '',
          'companies',
          'datasets',
          'requests',
          '/api-key',
          '/dataprivacy',
          '/imprint',
          '/nocontent',
          `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.Lksg}`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.Sfdr}`,
        ];
        uploaderOnlyPages = [
          '/companies/choose',
          `/companies/${companyId}/frameworks/upload`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`,
          `/companies/${companyId}/frameworks/${DataTypeEnum.Lksg}/upload`,
        ];
        reviewerOnlyPages = ['/qualityassurance'];
      });
    });

    it('Check if a non uploader user can access only the corresponding pages', () => {
      cy.ensureLoggedIn(reader_name, reader_pw);
      for (const page of readerAndUploaderPages) {
        it(`Non uploader should be able to access ${page}`, () => {
          cy.visit(page);
          cy.get(noPermissionMessage, { timeout: Cypress.env('long_timeout_in_ms') as number }).should('not.exist');
        });
      }
      for (const page of uploaderOnlyPages) {
        cy.visit(page);
        cy.get(noPermissionMessage, { timeout: Cypress.env('long_timeout_in_ms') as number }).should('exist');
      }
      for (const page of reviewerOnlyPages) {
        cy.visit(page);
        cy.get(noPermissionMessage, { timeout: Cypress.env('long_timeout_in_ms') as number }).should('exist');
      }
    });

    it('Check if an uploader user can access the corresponding pages', () => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      for (const page of readerAndUploaderPages) {
        cy.visit(page);
        cy.get(noPermissionMessage, { timeout: Cypress.env('long_timeout_in_ms') as number }).should('not.exist');
      }
      for (const page of uploaderOnlyPages) {
        cy.visit(page);
        cy.get(noPermissionMessage, { timeout: Cypress.env('long_timeout_in_ms') as number }).should('not.exist');
      }
    });
    it('Check if an reviewer user can access the corresponding page', () => {
      cy.ensureLoggedIn(reviewer_name, reviewer_pw);
      for (const page of reviewerOnlyPages) {
        cy.visit(page);
        cy.get(noPermissionMessage, { timeout: Cypress.env('long_timeout_in_ms') as number }).should('not.exist');
      }
    });
  }
);
