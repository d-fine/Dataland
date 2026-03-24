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
const containedDataPoints: Record<string, string> = {};

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

      getReviewerAndAdminTokens().then(({ reviewerToken, adminToken, uploaderToken }) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          EuTaxonomyFinancialsBaseFrameworkDefinition,
          uploaderToken,
          storedCompany.companyId,
          '2024',
          euTaxonomyData.t,
          false
        ).then((dataMetaInfo: DataMetaInformation) => {
          uploadQaReportsForDataset(dataMetaInfo, { reviewerToken, adminToken });
          startJudgement(storedCompany).then((datasetJudgementId: string) => {
            cy.contains('button', 'REVIEW PAGE').should('be.visible').click();
            cy.contains('span', 'Custom Data Point').should('be.visible');
            judgeDatapoints(datasetJudgementId, adminToken);
          });
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
 * Navigates to the review page from the judgement start view.
 * ToDo Zu viele PATCH anfragen aufeinmal.
 *
 */
function judgeDatapoints(datasetJudgementId: string, adminToken: string): void {
  Object.keys(containedDataPoints).forEach((dataPointType) => {
    cy.request({
      method: 'PATCH',
      url: `${apiBaseUrl}/qa/dataset-judgements/${datasetJudgementId}/data-points/${dataPointType}`,
      headers: { Authorization: `Bearer ${adminToken}` },
      body: {
        acceptedSource: 'Original',
      },
    });
    cy.get(`tr[data-point-type-id="${dataPointType}"]`)
      .should('exist')
      .within(() => {
        cy.get('td').eq(1).find('.accepted-check').should('be.visible');
      });
  });
}

/**
 * Starts a judgement by navigating to QA and verifying the uploaded dataset is listed for the company.
 *
 * @param storedCompany The company owning the dataset to be judged.
 * @returns The dataset judgement id returned by the start judgement request.
 */
function startJudgement(storedCompany: StoredCompany): Cypress.Chainable<string> {
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

  return cy
    .wait('@startJudgementRequest')
    .then((interception) => {
      expect(interception.response?.statusCode).to.eq(201);
      return interception.response?.body?.datasetJudgementId as string;
    })
    .should('exist');
}

/**
 * Uploads QA reports for selected data point types in the given dataset.
 *
 * Uses the admin token to resolve data point IDs via the metadata endpoint and
 * then posts QA reports using the reviewer/admin tokens based on the configured scenarios.
 *
 * @param dataMetaInfo Metadata of the dataset whose data points are queried.
 * @param tokens Reviewer/admin bearer tokens used for QA report uploads.
 */
function uploadQaReportsForDataset(
  dataMetaInfo: DataMetaInformation,
  tokens: { reviewerToken: string; adminToken: string }
): void {
  const dataPointTypesWithCorrectedValues = getCorrectedDataPointValues();
  const qaReportScenarios = buildQaReportScenarios(tokens.reviewerToken, tokens.adminToken);

  cy.request({
    method: 'GET',
    url: `${apiBaseUrl}/api/metadata/${dataMetaInfo.dataId}/data-points`,
    headers: { Authorization: `Bearer ${tokens.adminToken}` },
  }).then((response) => {
    cy.log(`GET /metadata/${dataMetaInfo.dataId}/data-points response: ${JSON.stringify(response.body)}`);
    Object.assign(containedDataPoints, response.body?.data ?? response.body ?? {});
    cy.log(`containedDataPoints: ${JSON.stringify(containedDataPoints)}`);
    Array.from(dataPointTypesWithCorrectedValues.entries()).forEach(([dataPointType, correctedValue], index) => {
      const dataPointId = containedDataPoints[dataPointType];
      qaReportScenarios[index](dataPointId, correctedValue);
    });
  });
}

/**
 * Retrieves reviewer, admin, and uploader Keycloak tokens for subsequent API requests.
 *
 * @returns A chainable containing reviewer, admin, and uploader bearer tokens.
 */
function getReviewerAndAdminTokens(): Cypress.Chainable<{
  reviewerToken: string;
  adminToken: string;
  uploaderToken: string;
}> {
  return getKeycloakToken(reviewer_name, reviewer_pw).then((reviewerToken: string) => {
    return getKeycloakToken(admin_name, admin_pw).then((adminToken: string) => {
      return getKeycloakToken(uploader_name, uploader_pw).then((uploaderToken: string) => {
        return { reviewerToken, adminToken, uploaderToken };
      });
    });
  });
}

/**
 * Uploads a single QA report for the given data point with the verdict QaRejected.
 *
 * @param dataPointId The data point id the QA report is attached to.
 * @param token Bearer token used to authorize the QA report upload.
 * @param correctedValue JSON string representing the corrected value payload.
 */
function uploadRejectedQaReportForDataPoint(dataPointId: string, token: string, correctedValue: string): void {
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

/**
 * Uploads a single QA report for the given data point with the verdict QaAccepted.
 *
 * @param dataPointId The data point id the QA report is attached to.
 * @param token Bearer token used to authorize the QA report upload.
 */
function uploadAcceptedQaReportForDataPoint(dataPointId: string, token: string): void {
  cy.request({
    method: 'POST',
    url: `${apiBaseUrl}/qa/data-points/${dataPointId}/reports`,
    headers: { Authorization: `Bearer ${token}` },
    body: {
      comment: 'The data point is correct and hence accepted.',
      verdict: 'QaAccepted',
    },
  });
}

/**
 * Builds the ordered QA report scenarios applied to successive data points.
 *
 * @param reviewerToken Bearer token for the reviewer user.
 * @param adminToken Bearer token for the admin user.
 * @returns List of scenario functions executed by index order.
 */
function buildQaReportScenarios(
  reviewerToken: string,
  adminToken: string
): Array<(dataPointId: string, correctedValue: string) => void> {
  return [
    (dataPointId: string): void => {
      uploadAcceptedQaReportForDataPoint(dataPointId, reviewerToken);
      uploadAcceptedQaReportForDataPoint(dataPointId, adminToken);
    },
    (dataPointId: string, correctedValue: string): void => {
      uploadRejectedQaReportForDataPoint(dataPointId, reviewerToken, correctedValue);
      uploadRejectedQaReportForDataPoint(dataPointId, adminToken, correctedValue);
    },
    (dataPointId: string, correctedValue: string): void => {
      uploadRejectedQaReportForDataPoint(dataPointId, reviewerToken, correctedValue);
      uploadAcceptedQaReportForDataPoint(dataPointId, adminToken);
    },
    (dataPointId: string, correctedValue: string): void => {
      uploadAcceptedQaReportForDataPoint(dataPointId, reviewerToken);
      uploadRejectedQaReportForDataPoint(dataPointId, adminToken, correctedValue);
    },
  ];
}

/**
 * Builds the corrected values map for data point types used in QA report uploads.
 *
 * @returns Map of data point type -> corrected value (JSON string).
 */
function getCorrectedDataPointValues(): Map<string, string> {
  const dataPointTypesWithCorrectedValues = new Map<string, string>();
  dataPointTypesWithCorrectedValues.set('extendedDateFiscalYearEnd', '{"value":"2026-03-23"}');
  dataPointTypesWithCorrectedValues.set(
    'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount',
    '{"value":"74568964325", "currency":"EUR"}'
  );
  dataPointTypesWithCorrectedValues.set('extendedEnumYesNoIsNfrdMandatory', '{"value":"No"}');
  dataPointTypesWithCorrectedValues.set('extendedDecimalNumberOfEmployees', '{"value":"2409600.75"}');
  return dataPointTypesWithCorrectedValues;
}
