import { type EutaxonomyFinancialsData, type SfdrData, type StoredCompany } from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility.ts';
import { getKeycloakToken, login } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { admin_name, admin_pw, uploader_name, uploader_pw } from '@e2e/utils/Cypress';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { visitQaOverviewAndGoToLastPage } from '@e2e/utils/QualityAssuranceUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition';

describeIf(
  'As a user, I expect to be able to log in',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let storedCompany: StoredCompany;
    let preparedEuTaxonomyFixtures: Array<FixtureData<EutaxonomyFinancialsData>>;
    let preparedSfdrFixtures: Array<FixtureData<SfdrData>>;

    before(function () {
      cy.fixture('CompanyInformationWithEutaxonomyFinancialsPreparedFixtures').then(function (jsonContent) {
        preparedEuTaxonomyFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
      });

      cy.fixture('CompanyInformationWithSfdrPreparedFixtures').then(function (jsonContent) {
        preparedSfdrFixtures = jsonContent as Array<FixtureData<SfdrData>>;
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const testCompany = generateDummyCompanyInformation(`company-for-testing-judgement-${Date.now()}`);
        return uploadCompanyViaApi(token, testCompany).then((newCompany) => (storedCompany = newCompany));
      });
    });

    it.only('Start Judgement', () => {
      const euTaxonomyData = getPreparedFixture('lightweight-eu-taxo-financials-dataset', preparedEuTaxonomyFixtures);

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          EuTaxonomyFinancialsBaseFrameworkDefinition,
          token,
          storedCompany.companyId,
          '2024',
          euTaxonomyData.t,
          false
        ).then(() => {
          startJudgement(storedCompany);
        });
      });
    });

    it('Start and finish a Judgement', () => {
      const sfdrData = getPreparedFixture('Sfdr-dataset-with-no-null-fields', preparedSfdrFixtures);

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          SfdrBaseFrameworkDefinition,
          token,
          storedCompany.companyId,
          '2021',
          sfdrData.t,
          false
        );
      });
    });
  }
);

/**
 * Starts a judgement by navigating to QA and verifying the uploaded dataset is listed for the company.
 *
 * @param storedCompany The company owning the dataset to be judged.
 */
function startJudgement(storedCompany: StoredCompany): void {
  cy.intercept('POST', '**/qa/**').as('startJudgementRequest');
  const companyName = storedCompany.companyInformation.companyName;
  login(admin_name, admin_pw);
  visitQaOverviewAndGoToLastPage();
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .last()
    .should('exist')
    .within(() => {
      cy.contains('[data-test="qa-review-company-name"]', companyName)
        .should('have.text', companyName)
        .closest('tr')
        .within(() => {
          cy.get('[data-test="qa-review-company-name"]').should('have.text', companyName);
          cy.contains('td', 'Start Review').should('exist').click();
        });
    });

  cy.get('.p-dialog')
    .should('be.visible')
    .within(() => {
      cy.contains('button', 'CONFIRM').should('exist').click();
    });

  cy.wait('@startJudgementRequest').its('response.statusCode').should('eq', 201);
}
