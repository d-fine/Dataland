import {
  type DataMetaInformation,
  type EutaxonomyFinancialsData,
  type SfdrData,
  type StoredCompany,
} from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility.ts';
import { getKeycloakToken, login, logout } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import {
  admin_name,
  admin_pw,
  admin_userId,
  getBaseUrl,
  judge_name,
  judge_pw,
  reviewer_name,
  reviewer_pw,
  reviewer_userId,
  uploader_name,
  uploader_pw,
} from '@e2e/utils/Cypress';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { qaOverviewNavigateToLastPage } from '@e2e/utils/QualityAssuranceUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition';

const apiBaseUrl = getBaseUrl();
const dataPointsWithQaReports: Record<string, string> = {};
const dataPointsWithoutQaReports: Record<string, string> = {};
let amountOfDataPointsToReview = 0;

interface PatchDataPointOptions {
  dataPointType: string;
  acceptedSource?: string;
  reporterUserIdOfAcceptedQaReport?: string;
  customDataPoint?: string;
}

enum IconState {
  Accepted,
  Rejected,
  None,
}

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

      getTokens().then(({ reviewerToken, adminToken, uploaderToken, judgeToken }) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          EuTaxonomyFinancialsBaseFrameworkDefinition,
          uploaderToken,
          storedCompany.companyId,
          '2024',
          euTaxonomyData.t,
          false
        ).then((dataMetaInfo: DataMetaInformation) => {
          uploadQaReportsForDataset(dataMetaInfo, { reviewerToken, adminToken });
          startJudgement(storedCompany).then((datasetJudgementId) => {
            const companyName = storedCompany.companyInformation.companyName;
            changeJudgeAssignment(companyName);
            judgeDatapointsWithoutQaReports(datasetJudgementId, judgeToken);
            judgeDatapointsWithQaReports(datasetJudgementId, judgeToken);
            finishJudgementSuccesfully(companyName);
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
 * Finishes the judgement by clicking the "Finish Judgement" button, confirming the dialog, and verifying the judgement is completed.
 *
 * @param judgeToken Bearer token for the judge user, used to verify the judgement completion via API if necessary.
 */
function finishJudgementSuccesfully(companyName: string): void {
  cy.contains('button', 'FINISH REVIEW').should('be.visible').click();
  cy.get('.p-dialog')
    .should('be.visible')
    .within(() => {
      cy.contains('button', 'CONFIRM').should('exist').click();
    });
  qaOverviewNavigateToLastPage();
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody').then(($tbody) => {
    const $rows = $tbody.find('tr');

    // Falls keine Zeile existiert: nichts prüfen
    if (!$rows.length) {
      return;
    }

    const $secondCellOfLastRow = $rows.last().find('td').eq(1);

    cy.wrap($secondCellOfLastRow).should('not.contain', companyName);
    // oder, wenn wirklich exakt der Text nicht gleich sein soll:
    // cy.wrap($secondCellOfLastRow).should('not.have.text', companyName);
  });
}

/**
 * Switches to the judge user, opens the review entry for the company, and assigns the dataset to the judge.
 *
 * @param companyName - The name of the company owning the dataset to be judged.
 */
function changeJudgeAssignment(companyName: string): void {
  cy.intercept('PATCH', '**/qa/dataset-judgements/**/judge').as('reassignJudgement');
  logout();
  login(judge_name, judge_pw);
  cy.visitAndCheckAppMount('/qualityassurance');
  qaOverviewNavigateToLastPage();
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .last()
    .should('exist')
    .within(() => {
      cy.contains('[data-test="qa-review-company-name"]', companyName)
        .should('have.text', companyName)
        .closest('tr')
        .within(() => {
          cy.get('[data-test="qa-review-company-name"]').should('have.text', companyName);
          cy.contains('td', 'Data Admin').should('exist').click();
        });
    });
  cy.contains('button', 'REVIEW PAGE').should('be.visible').click();
  cy.contains('p', 'Currently assigned to:').should('be.visible').next('p').should('have.text', 'Data Admin');
  cy.contains('button', 'ASSIGN YOURSELF').should('be.visible').click();
  cy.get('.p-dialog')
    .should('be.visible')
    .within(() => {
      cy.contains('button', 'CONFIRM').should('exist').click();
    });
  cy.wait('@reassignJudgement').its('response.statusCode').should('eq', 200);
}

/**
 * Patches a datapoint with the given accepted source, reporter user id, and custom datapoint value.
 *
 * @param datasetJudgementId The dataset for which the datapoint should be patched
 * @param token Authentication token
 * @param options Options for patching the datapoint, including dataPointType, acceptedSource,
 * reporterUserIdOfAcceptedQaReport, and customDataPoint
 */
function patchDataPoint(datasetJudgementId: string, token: string, options: PatchDataPointOptions): void {
  cy.request({
    method: 'PATCH',
    url: `${apiBaseUrl}/qa/dataset-judgements/${datasetJudgementId}/data-points/${options.dataPointType}`,
    headers: { Authorization: `Bearer ${token}` },
    body: {
      acceptedSource: options.acceptedSource,
      reporterUserIdOfAcceptedQaReport: options.reporterUserIdOfAcceptedQaReport,
      customDataPoint: options.customDataPoint,
    },
  })
    .its('status')
    .should('eq', 200);
}

/**
 * Checks the icons in the row of the given data point id and verifies that they match the expected ones.
 *
 * @param dataPointId Id of the datapoint we want to check
 * @param expectedIcons The expected icons of the given datapoint
 */
function checkRowIcons(dataPointId: string, expectedIcons: IconState[]): void {
  cy.get(`[data-test="data-point-row-${dataPointId}"]`).within(() => {
    expectedIcons.forEach((state, index) => {
      switch (expectedIcons[index]) {
        case IconState.Accepted: {
          cy.get('td')
            .eq(index + 1)
            .find('.accepted-check')
            .should('exist');
          break;
        }
        case IconState.Rejected: {
          cy.get('td')
            .eq(index + 1)
            .find('.rejected-check')
            .should('exist');
          break;
        }
        case IconState.None: {
          cy.get('td')
            .eq(index + 1)
            .find('.accepted-check')
            .should('not.exist');
          cy.get('td')
            .eq(index + 1)
            .find('.rejected-check')
            .should('not.exist');
          break;
        }
      }
    });
  });
}

/**
 * Patches QA-report datapoints, reloads, and verifies the table renders.
 *
 * @param datasetJudgementId - The dataset judgement id to update.
 * @param judgeToken - Bearer token for the judge user.
 */
function judgeDatapointsWithQaReports(datasetJudgementId: string, judgeToken: string): void {
  patchDataPoint(datasetJudgementId, judgeToken, {
    dataPointType: 'extendedDateFiscalYearEnd',
    acceptedSource: 'Original',
  });
  patchDataPoint(datasetJudgementId, judgeToken, {
    dataPointType: 'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount',
    acceptedSource: 'Custom',
    customDataPoint: '{"value":"400400400.23", "currency":"EUR"}',
  });
  patchDataPoint(datasetJudgementId, judgeToken, {
    dataPointType: 'extendedEnumYesNoIsNfrdMandatory',
    acceptedSource: 'Qa',
    reporterUserIdOfAcceptedQaReport: reviewer_userId,
    customDataPoint: '{"value":"No"}',
  });
  patchDataPoint(datasetJudgementId, judgeToken, {
    dataPointType: 'extendedDecimalNumberOfEmployees',
    acceptedSource: 'Qa',
    reporterUserIdOfAcceptedQaReport: admin_userId,
  });

  cy.reload();
  cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
  cy.contains(0 + ' / ' + amountOfDataPointsToReview + ' data points to review').should('be.visible');

  checkRowIcons(dataPointsWithQaReports['extendedDateFiscalYearEnd'], [
    IconState.Accepted,
    IconState.None,
    IconState.None,
    IconState.None,
  ]);

  checkRowIcons(
    dataPointsWithQaReports[
      'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount'
    ],
    [IconState.Rejected, IconState.Rejected, IconState.Rejected, IconState.Accepted]
  );

  checkRowIcons(dataPointsWithQaReports['extendedEnumYesNoIsNfrdMandatory'], [
    IconState.Rejected,
    IconState.Accepted,
    IconState.None,
    IconState.Rejected,
  ]);

  checkRowIcons(dataPointsWithQaReports['extendedDecimalNumberOfEmployees'], [
    IconState.Rejected,
    IconState.None,
    IconState.Accepted,
    IconState.None,
  ]);
}

/**
 * Patches all data points without QA reports as accepted original,
 * reloads once, and then verifies the expected icons in each row.
 *
 * @param datasetJudgementId - The dataset judgement id to update.
 * @param judgeToken - Bearer token for the judge user.
 */
function judgeDatapointsWithoutQaReports(datasetJudgementId: string, judgeToken: string): void {
  const dataPointEntries = Object.entries(dataPointsWithoutQaReports);
  cy.then(() => {
    dataPointEntries.forEach(([dataPointType]) => {
      patchDataPoint(datasetJudgementId, judgeToken, {
        dataPointType: dataPointType,
        acceptedSource: 'Original',
      });
    });
  }).then(() => checkOriginalDatapointsAccepted(dataPointEntries));
}

/**
 * Reloads the review page and checks the expected icons for the original datapoint.
 *
 * @param dataPointEntries - Tuple entries of dataPointType and dataPointId.
 */
function checkOriginalDatapointsAccepted(dataPointEntries: Array<[string, string]>): void {
  cy.reload();
  cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
  cy.contains(
    Object.keys(dataPointsWithQaReports).length + ' / ' + amountOfDataPointsToReview + ' data points to review'
  ).should('be.visible');
  cy.then(() => {
    dataPointEntries.forEach(([, dataPointId]) => {
      checkRowIcons(dataPointId, [IconState.Accepted, IconState.None, IconState.None, IconState.None]);
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
  cy.visitAndCheckAppMount('/qualityassurance');
  qaOverviewNavigateToLastPage();
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

  cy.contains('button', 'REVIEW PAGE').should('be.visible').click();
  cy.contains('span', 'Custom Data Point').should('be.visible');

  return cy
    .wait('@startJudgementRequest')
    .then((interception) => {
      expect(interception.response?.statusCode).to.eq(201);
      cy.log(`startJudgement url: ${interception.request?.url}`);
      cy.log(`startJudgement status: ${interception.response?.statusCode}`);
      cy.log(`startJudgement response body: ${JSON.stringify(interception.response?.body ?? null)}`);
      const dataSetJudgementId = interception.response?.body?.dataSetJudgementId;
      cy.log(`datasetJudgementId: ${dataSetJudgementId}`);
      return cy.wrap(dataSetJudgementId as string, { log: false });
    })
    .should('exist');
}

/**
 * Uploads QA reports for selected data point types in the given dataset.
 *
 * Uses the admin token to resolve data point IDs via the metadata endpoint and
 * then posts QA reports using the reviewer/admin tokens based on the configured scenarios.
 *
 * @param {DataMetaInformation} dataMetaInfo Metadata of the dataset whose data points are queried.
 * @param {{ reviewerToken: string; adminToken: string }} tokens Reviewer/admin bearer tokens used for QA report uploads.
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
    const allDataPoints = response.body?.data ?? response.body ?? {};
    amountOfDataPointsToReview = Object.keys(allDataPoints).length;
    cy.log(`GET /metadata/${dataMetaInfo.dataId}/data-points response: ${JSON.stringify(response.body)}`);
    Object.keys(dataPointsWithQaReports).forEach((key) => delete dataPointsWithQaReports[key]);
    Object.keys(dataPointsWithoutQaReports).forEach((key) => delete dataPointsWithoutQaReports[key]);
    Object.entries(allDataPoints).forEach(([dataPointType, dataPointId]) => {
      if (dataPointTypesWithCorrectedValues.has(dataPointType)) {
        dataPointsWithQaReports[dataPointType] = dataPointId as string;
      } else {
        dataPointsWithoutQaReports[dataPointType] = dataPointId as string;
      }
    });
    cy.log(`dataPointsWithQaReports: ${JSON.stringify(dataPointsWithQaReports)}`);
    cy.log(`dataPointsWithoutQaReports: ${JSON.stringify(dataPointsWithoutQaReports)}`);
    cy.log(`dataPointsWithoutQaReports: ${amountOfDataPointsToReview}`);
    Array.from(dataPointTypesWithCorrectedValues.entries()).forEach(([dataPointType, correctedValue], index) => {
      const dataPointId = dataPointsWithQaReports[dataPointType];
      qaReportScenarios[index](dataPointId, correctedValue);
    });
  });
}

/**
 * Retrieves reviewer, admin, uploader, and judge Keycloak tokens for subsequent API requests.
 *
 */
function getTokens(): Cypress.Chainable<{
  reviewerToken: string;
  adminToken: string;
  uploaderToken: string;
  judgeToken: string;
}> {
  return getKeycloakToken(reviewer_name, reviewer_pw).then((reviewerToken: string) => {
    return getKeycloakToken(admin_name, admin_pw).then((adminToken: string) => {
      return getKeycloakToken(uploader_name, uploader_pw).then((uploaderToken: string) => {
        return getKeycloakToken(judge_name, judge_pw).then((judgeToken: string) => {
          return { reviewerToken, adminToken, uploaderToken, judgeToken };
        });
      });
    });
  });
}

/**
 * Uploads a single QA report for the given data point with the verdict QaRejected.
 *
 * @param {string} dataPointId The data point id the QA report is attached to.
 * @param {string} token Bearer token used to authorize the QA report upload.
 * @param {string} correctedValue JSON string representing the corrected value payload.
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
 * @param {string} dataPointId The data point id the QA report is attached to.
 * @param {string} token Bearer token used to authorize the QA report upload.
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
 * @param {string} reviewerToken Bearer token for the reviewer user.
 * @param {string} adminToken Bearer token for the admin user.
 * @returns {Array<(dataPointId: string, correctedValue: string) => void>} List of scenario functions executed by index order.
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
 * @returns {Map<string, string>} Map of data point type -> corrected value (JSON string).
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
