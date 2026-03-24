import {
  type DataMetaInformation,
  type EutaxonomyFinancialsData,
  type SfdrData,
  type StoredCompany,
} from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility.ts';
import { getKeycloakToken, login } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import {
  admin_name,
  admin_pw,
  getBaseUrl,
  reviewer_name,
  reviewer_pw,
  uploader_name,
  uploader_pw,
} from '@e2e/utils/Cypress';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { visitQaOverviewAndGoToLastPage } from '@e2e/utils/QualityAssuranceUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition';

const apiBaseUrl = getBaseUrl();

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
        )
          .then((dataMetaInfo: DataMetaInformation) => {
            const dataPointTypesWithCorrectedValues = new Map<string, string>();
            dataPointTypesWithCorrectedValues.set('extendedDateFiscalYearEnd', '{"value":"2026-03-23"}');
            dataPointTypesWithCorrectedValues.set(
              'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount',
              '{"value":"74568964325", "currency":"EUR"}'
            );
            uploadQaReportsForDataset(dataMetaInfo, dataPointTypesWithCorrectedValues);
          })
          .then(() => {
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

/**
 * Uploads QA reports for selected data point types in the given dataset.
 *
 * Uses the admin token to resolve data point IDs via the metadata endpoint and
 * then posts QA reports for each provided data point type.
 *
 * @param dataMetaInfo Metadata of the dataset whose data points are queried.
 * @param dataPointTypesWithCorrectedValues Map of data point type -> corrected value (JSON string).
 */
function uploadQaReportsForDataset(
  dataMetaInfo: DataMetaInformation,
  dataPointTypesWithCorrectedValues: Map<string, string>
): void {
  getReviewerAndAdminTokens().then(({ reviewerToken, adminToken }) => {
    cy.request({
      method: 'GET',
      url: `${apiBaseUrl}/api/metadata/${dataMetaInfo.dataId}/data-points`,
      headers: { Authorization: `Bearer ${adminToken}` },
    }).then((response) => {
      const containedDataPoints = response.body?.data ?? response.body;
      Array.from(dataPointTypesWithCorrectedValues.entries()).forEach(([dataPointType, correctedValue]) => {
        uploadQaReportForDataPoint(containedDataPoints[dataPointType], reviewerToken, correctedValue);
        uploadQaReportForDataPoint(containedDataPoints[dataPointType], adminToken, correctedValue);
      });
    });
  });
}

/**
 * Retrieves reviewer and admin Keycloak tokens for subsequent API requests.
 *
 * @returns A chainable containing both reviewer and admin bearer tokens.
 */
function getReviewerAndAdminTokens(): Cypress.Chainable<{ reviewerToken: string; adminToken: string }> {
  return getKeycloakToken(reviewer_name, reviewer_pw).then((reviewerToken: string) => {
    return getKeycloakToken(admin_name, admin_pw).then((adminToken: string) => {
      return { reviewerToken, adminToken };
    });
  });
}

/**
 * Uploads a single QA report for the given data point.
 *
 * @param dataPointId The data point id the QA report is attached to.
 * @param token Bearer token used to authorize the QA report upload.
 * @param correctedValue JSON string representing the corrected value payload.
 */
function uploadQaReportForDataPoint(dataPointId: string, token: string, correctedValue: string): void {
  cy.request({
    method: 'POST',
    url: `${apiBaseUrl}/qa/data-points/${dataPointId}/reports`,
    headers: { Authorization: `Bearer ${token}` },
    body: {
      comment: 'The data point is not correct and hence rejected.',
      verdict: 'QaRejected',
      correctedData: correctedValue,
    },
  });
}
