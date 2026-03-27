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
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition';

enum IconState {
  Accepted,
  Rejected,
  None,
}

type QaVerdict = 'QaAccepted' | 'QaRejected';
type QaRole = 'reviewer' | 'admin';

type QaTokens = {
  reviewerToken: string;
  adminToken: string;
  uploaderToken: string;
  judgeToken: string;
};

interface QaAction {
  role: QaRole;
  verdict: QaVerdict;
}

interface QaReportData {
  dataPointType: DataPointType;
  correctedValue?: string;
  actions: QaAction[];
}

interface DataPointOverview {
  dataPointsWithQaReports: Record<string, string>;
  dataPointsWithoutQaReports: Record<string, string>;
  amountOfDataPointsToReview: number;
}

interface PatchDataPointOptions {
  dataPointType: string;
  acceptedSource?: string;
  reporterUserIdOfAcceptedQaReport?: string;
  customDataPoint?: string;
}

const DATA_POINT_TYPES = {
  fiscalYearEnd: 'extendedDateFiscalYearEnd',
  greenAssetRatioTotal:
    'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount',
  isNfrdMandatory: 'extendedEnumYesNoIsNfrdMandatory',
  numberOfEmployees: 'extendedDecimalNumberOfEmployees',
} as const;

type DataPointTypeKey = keyof typeof DATA_POINT_TYPES;
type DataPointType = (typeof DATA_POINT_TYPES)[DataPointTypeKey];

const apiBaseUrl = getBaseUrl();

const QA_REPORT_CONFIG: QaReportData[] = [
  {
    dataPointType: DATA_POINT_TYPES.fiscalYearEnd,
    correctedValue: '{"value":"2026-03-23"}',
    actions: [
      { role: 'reviewer', verdict: 'QaAccepted' },
      { role: 'admin', verdict: 'QaAccepted' },
    ],
  },
  {
    dataPointType: DATA_POINT_TYPES.greenAssetRatioTotal,
    correctedValue: '{"value":"74568964325", "currency":"EUR"}',
    actions: [
      { role: 'reviewer', verdict: 'QaRejected' },
      { role: 'admin', verdict: 'QaRejected' },
    ],
  },
  {
    dataPointType: DATA_POINT_TYPES.isNfrdMandatory,
    correctedValue: '{"value":"No"}',
    actions: [
      { role: 'reviewer', verdict: 'QaRejected' },
      { role: 'admin', verdict: 'QaAccepted' },
    ],
  },
  {
    dataPointType: DATA_POINT_TYPES.numberOfEmployees,
    correctedValue: '{"value":"2409600.75"}',
    actions: [
      { role: 'reviewer', verdict: 'QaAccepted' },
      { role: 'admin', verdict: 'QaRejected' },
    ],
  },
];

describeIf(
  'As a user, I expect to be able to log in',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let storedCompany: StoredCompany;
    let preparedEuTaxonomyFixtures: Array<FixtureData<EutaxonomyFinancialsData>>;
    let preparedSfdrFixtures: Array<FixtureData<SfdrData>>;
    let companyName: string;

    before(function () {
      cy.fixture('CompanyInformationWithEutaxonomyFinancialsPreparedFixtures').then(function (jsonContent) {
        preparedEuTaxonomyFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
      });

      cy.fixture('CompanyInformationWithSfdrPreparedFixtures').then(function (jsonContent) {
        preparedSfdrFixtures = jsonContent as Array<FixtureData<SfdrData>>;
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const testCompany = generateDummyCompanyInformation(`company-for-testing-judgement-${Date.now()}`);
        return uploadCompanyViaApi(token, testCompany).then((newCompany) => {
          storedCompany = newCompany;
          companyName = storedCompany.companyInformation.companyName;
        });
      });
    });

    it('Check full judgement process from upload to acceptance', () => {
      const euTaxonomyData = getPreparedFixture('lightweight-eu-taxo-financials-dataset', preparedEuTaxonomyFixtures);

      let tokens: QaTokens;
      let overview: DataPointOverview;

      return getTokens()
        .then((t) => {
          tokens = t;
          return uploadFrameworkDataForPublicToolboxFramework(
            EuTaxonomyFinancialsBaseFrameworkDefinition,
            tokens.uploaderToken,
            storedCompany.companyId,
            '2024',
            euTaxonomyData.t,
            false
          );
        })
        .then((dataMetaInfo: DataMetaInformation) =>
          initializeDataPointOverviewForDataset(dataMetaInfo, tokens.adminToken)
        )
        .then((o) => {
          overview = o;
          uploadQaReportsForDataset(overview, {
            reviewerToken: tokens.reviewerToken,
            adminToken: tokens.adminToken,
          });
          checkoutDataset(companyName);
          return startJudgement(companyName);
        })
        .then((datasetJudgementId) => {
          changeJudgeAssignment(companyName);
          judgeDatapointsWithoutQaReports(datasetJudgementId, tokens.judgeToken, overview);
          tryFinishingJudgementBeforeAllDataPointsReviewed();
          judgeDatapointsWithQaReports(datasetJudgementId, tokens.judgeToken, overview);
          finishJudgement(companyName);
        });
    });

    it('Check rejecting a Dataset on the Judgement Page works as expected', () => {
      const sfdrData = getPreparedFixture('Sfdr-dataset-with-no-null-fields', preparedSfdrFixtures);

      return getKeycloakToken(uploader_name, uploader_pw)
        .then((uploaderToken: string) =>
          uploadFrameworkDataForPublicToolboxFramework(
            SfdrBaseFrameworkDefinition,
            uploaderToken,
            storedCompany.companyId,
            '2024',
            sfdrData.t,
            false
          )
        )
        .then(() => {
          login(admin_name, admin_pw);
          return startJudgement(companyName);
        })
        .then(() => {
          rejectDatasetInJudgementModel(companyName);
        });
    });
  }
);

/**
 * Retrieves reviewer, admin, uploader, and judge Keycloak tokens for subsequent API requests.
 *
 * @returns A Cypress.Chainable that resolves to an object containing the retrieved tokens.
 */
function getTokens(): Cypress.Chainable<QaTokens> {
  let reviewerToken: string;
  let adminToken: string;
  let uploaderToken: string;

  return getKeycloakToken(reviewer_name, reviewer_pw)
    .then((token) => {
      reviewerToken = token;
      return getKeycloakToken(admin_name, admin_pw);
    })
    .then((token) => {
      adminToken = token;
      return getKeycloakToken(uploader_name, uploader_pw);
    })
    .then((token) => {
      uploaderToken = token;
      return getKeycloakToken(judge_name, judge_pw);
    })
    .then((judgeToken) => ({
      reviewerToken,
      adminToken,
      uploaderToken,
      judgeToken,
    }));
}

/**
 * Uploads a QA report for a specific data point via the QA API.
 *
 * @param dataPointId     Identifier of the data point for which the QA report is created.
 * @param token           Bearer token used for authentication in the request header.
 * @param verdict         QA verdict for the data point (e.g. accepted or rejected).
 * @param correctedValue  Optional corrected value; sent as `correctedData` when the verdict is 'QaRejected'.
 */
function uploadQaReportForDataPoint(
  dataPointId: string,
  token: string,
  verdict: QaVerdict,
  correctedValue?: string
): void {
  cy.request({
    method: 'POST',
    url: `${apiBaseUrl}/qa/data-points/${dataPointId}/reports`,
    headers: { Authorization: `Bearer ${token}` },
    body: {
      comment:
        verdict === 'QaAccepted'
          ? 'The data point is correct and hence accepted.'
          : 'The data point is not correct and hence rejected.',
      verdict,
      ...(verdict === 'QaRejected' && correctedValue ? { correctedData: correctedValue } : {}),
    },
  });
}

/**
 * Loads all data points for a dataset and builds the DataPointOverview.
 *
 * @param dataMetaInfo Metadata information of the dataset whose data points are to be loaded.
 * @param adminToken   Bearer token of the admin user used to call the metadata endpoint.
 * @returns A Cypress.Chainable that resolves to the built DataPointOverview.
 */
function initializeDataPointOverviewForDataset(
  dataMetaInfo: DataMetaInformation,
  adminToken: string
): Cypress.Chainable<DataPointOverview> {
  const qaConfigByType = new Map<string, QaReportData>();
  QA_REPORT_CONFIG.forEach((entry) => qaConfigByType.set(entry.dataPointType, entry));

  return cy
    .request({
      method: 'GET',
      url: `${apiBaseUrl}/api/metadata/${dataMetaInfo.dataId}/data-points`,
      headers: { Authorization: `Bearer ${adminToken}` },
    })
    .then((response) => {
      const allDataPoints = response.body?.data ?? response.body ?? {};

      const dataPointsWithQaReports: Record<string, string> = {};
      const dataPointsWithoutQaReports: Record<string, string> = {};

      Object.entries(allDataPoints).forEach(([dataPointType, dataPointId]) => {
        if (qaConfigByType.has(dataPointType)) {
          dataPointsWithQaReports[dataPointType] = dataPointId as string;
        } else {
          dataPointsWithoutQaReports[dataPointType] = dataPointId as string;
        }
      });

      const overview: DataPointOverview = {
        dataPointsWithQaReports,
        dataPointsWithoutQaReports,
        amountOfDataPointsToReview: Object.keys(allDataPoints).length,
      };

      return overview;
    });
}

/**
 * Uploads QA reports for all configured data point types using the provided DataPointOverview.
 *
 * For each entry in `QA_REPORT_CONFIG`, the corresponding data point ID is resolved from
 * `overview.dataPointsWithQaReports` and the configured QA actions are executed.
 *
 * @param overview DataPointOverview containing data point IDs and counts.
 * @param tokens   Authentication tokens for the reviewer and admin users, used to upload QA reports.
 */
function uploadQaReportsForDataset(
  overview: DataPointOverview,
  tokens: { reviewerToken: string; adminToken: string }
): void {
  QA_REPORT_CONFIG.forEach((config) => {
    const dataPointId = overview.dataPointsWithQaReports[config.dataPointType];
    if (!dataPointId) {
      return;
    }
    runQaScenarioForDataPoint(dataPointId, config, tokens);
  });
}

/**
 * Executes all configured QA actions for a single data point.
 *
 * @param dataPointId Identifier of the data point for which QA reports should be created.
 * @param config      QA configuration describing verdicts and roles.
 * @param tokens      Authentication tokens for reviewer and admin.
 */
function runQaScenarioForDataPoint(
  dataPointId: string,
  config: QaReportData,
  tokens: { reviewerToken: string; adminToken: string }
): void {
  config.actions.forEach((action) => {
    const token = action.role === 'reviewer' ? tokens.reviewerToken : tokens.adminToken;
    uploadQaReportForDataPoint(dataPointId, token, action.verdict, config.correctedValue);
  });
}

/**
 * Checks out the dataset for the given company by navigating to the QA overview and selecting the dataset row
 *
 * @param companyName Name of the company whose QA dataset row should be selected.
 */
function checkoutDataset(companyName: string): void {
  login(admin_name, admin_pw);
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.contains('[data-test="qa-review-company-name"]', companyName).should('be.visible').click({ force: true });
  cy.get('[data-test="qaReviewPageButton"]').should('be.visible').and('be.disabled');
}

/**
 * Starts a judgement by navigating to QA and verifying the uploaded dataset is listed for the company.
 *
 * @param companyName The company owning the dataset to be judged.
 * @returns The dataset judgement id returned by the start judgement request.
 */
function startJudgement(companyName: string): Cypress.Chainable<string> {
  cy.intercept('POST', '**/qa/**').as('startJudgementRequest');
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.get('[data-test="qa-review-section"]').should('be.visible');
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
      const dataSetJudgementId = interception.response?.body?.dataSetJudgementId;
      return cy.wrap(dataSetJudgementId as string, { log: false });
    })
    .should('exist');
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
  cy.get('[data-test="qa-review-section"]').should('be.visible');
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
 * Patches all data points without QA reports as accepted original.
 *
 * @param datasetJudgementId - The dataset judgement id to update.
 * @param judgeToken         - Bearer token for the judge user.
 * @param overview           - DataPointOverview containing data point IDs and counts.
 */
function judgeDatapointsWithoutQaReports(
  datasetJudgementId: string,
  judgeToken: string,
  overview: DataPointOverview
): void {
  const dataPointEntries = Object.entries(overview.dataPointsWithoutQaReports);

  cy.then(() => {
    dataPointEntries.forEach(([dataPointType]) => {
      patchDataPoint(datasetJudgementId, judgeToken, {
        dataPointType,
        acceptedSource: 'Original',
      });
    });
  }).then(() => checkOriginalDatapointsAccepted(dataPointEntries, overview));
}

/**
 * Checks if the "Finish Judgement" button is visible and disabled.
 */
function tryFinishingJudgementBeforeAllDataPointsReviewed(): void {
  cy.get('[data-test="qaReviewPageFinishButton"]').should('be.visible').and('be.disabled');
}

/**
 * Patches QA-report datapoints, reloads, and verifies the table renders.
 *
 * @param datasetJudgementId - The dataset judgement id to update.
 * @param judgeToken         - Bearer token for the judge user.
 * @param overview           - DataPointOverview containing data point IDs and counts.
 */
function judgeDatapointsWithQaReports(
  datasetJudgementId: string,
  judgeToken: string,
  overview: DataPointOverview
): void {
  patchDataPoint(datasetJudgementId, judgeToken, {
    dataPointType: DATA_POINT_TYPES.fiscalYearEnd,
    acceptedSource: 'Original',
  });
  patchDataPoint(datasetJudgementId, judgeToken, {
    dataPointType: DATA_POINT_TYPES.greenAssetRatioTotal,
    acceptedSource: 'Custom',
    customDataPoint: '{"value":"400400400.23", "currency":"EUR"}',
  });
  patchDataPoint(datasetJudgementId, judgeToken, {
    dataPointType: DATA_POINT_TYPES.isNfrdMandatory,
    acceptedSource: 'Qa',
    reporterUserIdOfAcceptedQaReport: reviewer_userId,
    customDataPoint: '{"value":"No"}',
  });
  patchDataPoint(datasetJudgementId, judgeToken, {
    dataPointType: DATA_POINT_TYPES.numberOfEmployees,
    acceptedSource: 'Qa',
    reporterUserIdOfAcceptedQaReport: admin_userId,
  });

  cy.reload();
  cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
  cy.contains(`0 / ${overview.amountOfDataPointsToReview} data points to review`).should('be.visible');

  checkRowIcons(overview.dataPointsWithQaReports[DATA_POINT_TYPES.fiscalYearEnd], [
    IconState.Accepted,
    IconState.None,
    IconState.None,
    IconState.None,
  ]);

  checkRowIcons(overview.dataPointsWithQaReports[DATA_POINT_TYPES.greenAssetRatioTotal], [
    IconState.Rejected,
    IconState.Rejected,
    IconState.Rejected,
    IconState.Accepted,
  ]);

  checkRowIcons(overview.dataPointsWithQaReports[DATA_POINT_TYPES.isNfrdMandatory], [
    IconState.Rejected,
    IconState.Accepted,
    IconState.None,
    IconState.Rejected,
  ]);

  checkRowIcons(overview.dataPointsWithQaReports[DATA_POINT_TYPES.numberOfEmployees], [
    IconState.Rejected,
    IconState.None,
    IconState.Accepted,
    IconState.None,
  ]);
}

/**
 * Finishes the judgement by clicking the "Finish Judgement" button.
 *
 * @param companyName Name of the company whose dataset is judged
 */
function finishJudgement(companyName: string): void {
  cy.contains('button', 'FINISH REVIEW').should('be.visible').click();
  cy.get('.p-dialog')
    .should('be.visible')
    .within(() => {
      cy.contains('button', 'CONFIRM').should('exist').click();
      cy.contains('Dataset review completed.').should('be.visible');
    });
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.contains('[data-test="qa-review-company-name"]', companyName).should('not.exist');
}

/**
 * Patches a datapoint with the given accepted source, reporter user id, and custom datapoint value.
 *
 * @param datasetJudgementId The dataset for which the datapoint should be patched
 * @param token              Authentication token
 * @param options            Options for patching the datapoint, including dataPointType, acceptedSource,
 *                           reporterUserIdOfAcceptedQaReport, and customDataPoint
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
 * @param dataPointId   Id of the datapoint we want to check
 * @param expectedIcons The expected icons of the given datapoint
 */
function checkRowIcons(dataPointId: string, expectedIcons: IconState[]): void {
  cy.get(`[data-test="data-point-row-${dataPointId}"]`).within(() => {
    expectedIcons.forEach((state, index) => {
      switch (state) {
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
 * Reloads the review page and checks the expected icons for the original datapoints.
 *
 * @param dataPointEntries - Tuple entries of dataPointType and dataPointId.
 * @param overview         - DataPointOverview containing data point IDs and counts.
 */
function checkOriginalDatapointsAccepted(dataPointEntries: Array<[string, string]>, overview: DataPointOverview): void {
  cy.reload();
  cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
  cy.contains(
    `${Object.keys(overview.dataPointsWithQaReports).length} / ${overview.amountOfDataPointsToReview} data points to review`
  ).should('be.visible');

  cy.then(() => {
    dataPointEntries.forEach(([, dataPointId]) => {
      checkRowIcons(dataPointId, [IconState.Accepted, IconState.None, IconState.None, IconState.None]);
    });
  });
}

/**
 * Reject Dataset via the button on the Judgement Page
 *
 * @param companyName Name of the company whose dataset should be rejected
 */
function rejectDatasetInJudgementModel(companyName: string): void {
  cy.get('[data-test="qaReviewPageRejectButton"]').should('be.visible').click();
  cy.get('.p-dialog')
    .should('be.visible')
    .within(() => {
      cy.contains('button', 'CONFIRM').should('exist').click();
      cy.contains('Dataset successfully rejected.').should('be.visible');
    });
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.contains('[data-test="qa-review-company-name"]', companyName).should('not.exist');
}
