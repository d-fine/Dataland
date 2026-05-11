import { type DataMetaInformation, type EutaxonomyFinancialsData, type StoredCompany } from '@clients/backend';
import { AcceptedDataPointSource, QaReportDataPointVerdict } from '@clients/qaservice';
import { describeIf } from '@e2e/support/TestUtility.ts';
import {
  getAdminToken,
  getUploaderToken,
  getReviewerToken,
  getJudgeToken,
  loginAsAdmin,
  loginAsJudge,
  logout,
} from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { admin_userId, getBaseUrl, reviewer_userId } from '@e2e/utils/Cypress';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import {
  selectNextDataPointToJudge,
  goToSelectedDataPoint,
  checkPatchDataPointsCalledCorrectly,
  makeJudgementDecision,
} from '@e2e/utils/CheckJudgement';
import type {
  QaReport,
  QaJudgement,
  QaScenarioConfig,
  DataPointOverview,
  QaRole,
} from '@e2e/utils/CheckJudgementJson.ts';
import {
  QA_SCENARIO_CONFIG,
  DATA_POINT_PATH_MAP,
  stripAssuranceFromFixture,
  parseJsonValue,
  extractValueForType,
} from '@e2e/utils/CheckJudgementJson.ts';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';

enum IconState {
  Accepted,
  Rejected,
  None,
}

type QaTokens = {
  reviewerToken: string;
  adminToken: string;
  uploaderToken: string;
  judgeToken: string;
};

const apiBaseUrl = getBaseUrl();

describeIf(
  'As a user, I expect to be able to go through the full judgement process',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let storedCompany: StoredCompany;
    let preparedEuTaxonomyFixtures: Array<FixtureData<EutaxonomyFinancialsData>>;
    let tokens: QaTokens;
    let uploadedDataMetaInfo: DataMetaInformation;
    let overview: DataPointOverview;

    before(function () {
      cy.fixture('CompanyInformationWithEutaxonomyFinancialsPreparedFixtures').then((jsonContent) => {
        const rawFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
        preparedEuTaxonomyFixtures = rawFixtures.map(stripAssuranceFromFixture);
      });

      getAdminToken().then((token: string) => {
        const testCompany = generateDummyCompanyInformation(`company-for-testing-judgement-${Date.now()}`);
        return uploadCompanyViaApi(token, testCompany).then((newCompany) => {
          storedCompany = newCompany;
        });
      });
    });

    beforeEach(() =>
      getTokens().then((retrievedTokens) => {
        tokens = retrievedTokens;
        const euTaxonomyData = getPreparedFixture('lightweight-eu-taxo-financials-dataset', preparedEuTaxonomyFixtures);

        return uploadFrameworkDataForPublicToolboxFramework(
          EuTaxonomyFinancialsBaseFrameworkDefinition,
          tokens.uploaderToken,
          storedCompany.companyId,
          '2024',
          euTaxonomyData.t,
          false
        ).then((dataMetaInfo: DataMetaInformation) => {
          uploadedDataMetaInfo = dataMetaInfo;
          return initializeDataPointOverviewForDataset(uploadedDataMetaInfo, tokens.adminToken).then(
            (preparedOverview: DataPointOverview) => {
              overview = preparedOverview;
              uploadQaReportsForDataset(overview, {
                reviewerToken: tokens.reviewerToken,
                adminToken: tokens.adminToken,
              });
            }
          );
        });
      })
    );

    it('Check creating a Judgement and reassigning the Judge works as expected', () => {
      const dataSetId = uploadedDataMetaInfo.dataId;
      checkoutDataset(dataSetId);
      startJudgement(dataSetId);
      changeJudgeAssignment(dataSetId);
    });

    it('Check judge modal selects expected values and stores them after finishing review', () => {
      createJudgementAndOpenReviewPage(uploadedDataMetaInfo, tokens.judgeToken).then((dataSetJudgementId) => {
        judgeDataPointsWithoutQaReports(dataSetJudgementId, tokens.judgeToken, overview);
        tryFinishingJudgementBeforeAllDataPointsReviewed();
        judgeDataPointsWithQaReports(dataSetJudgementId, tokens.judgeToken, overview);
        finishJudgement(uploadedDataMetaInfo.dataId);

        cy.waitUntil(() =>
          cy
            .request({
              method: 'GET',
              url: `${apiBaseUrl}/api/metadata/${uploadedDataMetaInfo.dataId}`,
              headers: { Authorization: `Bearer ${tokens.adminToken}` },
              failOnStatusCode: false,
            })
            .then((resp) => {
              if (resp.status !== 200) return false;
              const qaStatus = String(resp.body?.qaStatus ?? '').toLowerCase();
              return qaStatus === 'accepted';
            })
        );

        const euTaxonomyData = getPreparedFixture('lightweight-eu-taxo-financials-dataset', preparedEuTaxonomyFixtures);

        verifyJudgementDataStoredCorrectly(
          overview,
          QA_SCENARIO_CONFIG,
          euTaxonomyData.t,
          storedCompany.companyId,
          uploadedDataMetaInfo.reportingPeriod,
          tokens.judgeToken
        );
      });
    });

    it('Check rejecting a Dataset on the Judgement Page works as expected', () => {
      createJudgementAndOpenReviewPage(uploadedDataMetaInfo, tokens.judgeToken);
      rejectDatasetInJudgementModal(uploadedDataMetaInfo.dataId);
    });
  }
);

/**
 * Starts a dataset judgement via the backend API and returns the created dataset judgement id.
 *
 * @param uploadedDataMetaInfo Metadata of the uploaded dataset for which the judgement should be created.
 * @param token                Bearer token used to authenticate the request against the QA backend.
 * @returns                    A Cypress.Chainable that resolves to the created dataset judgement id.
 */
function createJudgementAndOpenReviewPage(
  uploadedDataMetaInfo: DataMetaInformation,
  token: string
): Cypress.Chainable<string> {
  loginAsJudge();
  return cy
    .request({
      method: 'POST',
      url: `${apiBaseUrl}/qa/dataset-judgements/${uploadedDataMetaInfo.dataId}`,
      headers: { Authorization: `Bearer ${token}` },
    })
    .then((response) => {
      expect(response.status).to.eq(201);
      const dataSetJudgementId = response.body?.dataSetJudgementId as string;
      cy.visitAndCheckAppMount(`/qualityassurance/review/${dataSetJudgementId}`);
      cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
      return cy.wrap(dataSetJudgementId, { log: false });
    });
}

/**
 * Retrieves reviewer, admin, uploader, and judge Keycloak tokens for subsequent API requests.
 *
 * @returns A Cypress.Chainable that resolves to an object containing the retrieved tokens.
 */
function getTokens(): Cypress.Chainable<QaTokens> {
  let reviewerToken: string;
  let adminToken: string;
  let uploaderToken: string;

  return getReviewerToken()
    .then((token) => {
      reviewerToken = token;
      return getAdminToken();
    })
    .then((token) => {
      adminToken = token;
      return getUploaderToken();
    })
    .then((token) => {
      uploaderToken = token;
      return getJudgeToken();
    })
    .then((judgeToken) => ({ reviewerToken, adminToken, uploaderToken, judgeToken }));
}

/**
 * Uploads a QA report for a specific data point via the QA API.
 *
 * @param dataPointId     Identifier of the data point for which the QA report is created.
 * @param token           Bearer token used for authentication in the request header.
 * @param verdict         QA verdict for the data point, using `QaReportDataPointVerdict` values.
 * @param correctedValue  Optional corrected value for the data point, required if the verdict is
 *                        `QaReportDataPointVerdict.QaRejected`.
 */
function uploadQaReportForDataPoint(
  dataPointId: string,
  token: string,
  verdict: QaReportDataPointVerdict,
  correctedValue?: string
): void {
  cy.request({
    method: 'POST',
    url: `${apiBaseUrl}/qa/data-points/${dataPointId}/reports`,
    headers: { Authorization: `Bearer ${token}` },
    body: {
      comment:
        verdict === QaReportDataPointVerdict.QaAccepted
          ? 'The data point is correct and hence accepted.'
          : 'The data point is not correct and hence rejected.',
      verdict,
      ...(verdict === QaReportDataPointVerdict.QaRejected && correctedValue ? { correctedData: correctedValue } : {}),
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
  const qaConfigByType = new Map<string, QaScenarioConfig>();
  QA_SCENARIO_CONFIG.forEach((entry) => qaConfigByType.set(entry.dataPointType, entry));

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

      return {
        dataPointsWithQaReports: dataPointsWithQaReports,
        dataPointsWithoutQaReports: dataPointsWithoutQaReports,
        amountOfDataPointsToReview: Object.keys(allDataPoints).length,
      };
    });
}

/**
 * Uploads QA reports for all configured data point types using the provided DataPointOverview.
 *
 * For each entry in `QA_SCENARIO_CONFIG`, the corresponding data point ID is resolved from
 * `overview.dataPointsWithQaReports` and the configured QA actions are executed.
 *
 * @param overview DataPointOverview containing data point IDs and counts.
 * @param tokens   Authentication tokens for the reviewer and admin users, used to upload QA reports.
 */
function uploadQaReportsForDataset(
  overview: DataPointOverview,
  tokens: { reviewerToken: string; adminToken: string }
): void {
  QA_SCENARIO_CONFIG.forEach((config) => {
    const dataPointId = overview.dataPointsWithQaReports[config.dataPointType];
    if (!dataPointId) {
      return;
    }
    runQaScenarioForDataPoint(dataPointId, config, tokens);
  });
}

/**
 * Executes all configured QA reports for a single data point.
 *
 * @param dataPointId Identifier of the data point for which QA reports should be created.
 * @param config      QA scenario configuration describing verdicts, roles, and corrected values.
 * @param tokens      Authentication tokens for reviewer and admin.
 */
function runQaScenarioForDataPoint(
  dataPointId: string,
  config: QaScenarioConfig,
  tokens: { reviewerToken: string; adminToken: string }
): void {
  config.qaReports.forEach((qaReport) => {
    const token = qaReport.role === 'reviewer' ? tokens.reviewerToken : tokens.adminToken;
    uploadQaReportForDataPoint(dataPointId, token, qaReport.verdict, qaReport.correctedValue);
  });
}

/**
 * Checks out the dataset for the given company by navigating to the QA overview and selecting the dataset row
 *
 * @param dataSetId Id of the dataset whose QA dataset row should be selected.
 */
function checkoutDataset(dataSetId: string): void {
  loginAsAdmin();
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .contains('[data-test="qa-review-data-id"]', dataSetId)
    .should('be.visible')
    .click();
  cy.get('[data-test="qaReviewPageButton"]').should('be.visible').and('be.disabled');
}

/**
 * Starts a judgement by navigating to QA and verifying the uploaded dataset is listed for the company.
 *
 * @param dataSetId Id of the dataset for which we want to start the judgement
 */
function startJudgement(dataSetId: string): void {
  cy.intercept('POST', '**/qa/dataset-judgements/*').as('startJudgementRequest');
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .contains('[data-test="qa-review-data-id"]', dataSetId)
    .closest('tr')
    .contains('td', 'Start Review')
    .click();

  cy.get('.p-dialog').should('be.visible').contains('button', 'CONFIRM').click();

  cy.wait('@startJudgementRequest').then((interception) => {
    expect(interception.response?.statusCode).to.eq(201);
  });
}

/**
 * Switches to the judge user, opens the review entry for the company, and assigns the dataset to the judge.
 *
 * @param dataSetId The Id of the dataset to be judged.
 */
function changeJudgeAssignment(dataSetId: string): void {
  cy.intercept('PATCH', '**/qa/dataset-judgements/**/judge').as('reassignJudgement');
  logout();
  loginAsJudge();
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .contains('[data-test="qa-review-data-id"]', dataSetId)
    .closest('tr')
    .contains('td', 'Data Admin')
    .click();
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
 * @param datasetJudgementId The dataset judgement id to update.
 * @param judgeToken         Bearer token for the judge user.
 * @param overview           DataPointOverview containing data point IDs and counts.
 */
function judgeDataPointsWithoutQaReports(
  datasetJudgementId: string,
  judgeToken: string,
  overview: DataPointOverview
): void {
  const dataPointEntries = Object.entries(overview.dataPointsWithoutQaReports);

  if (dataPointEntries.length === 0) return;

  // 1) Open the judge modal on the first data point without QA
  const [, firstDataPointId] = dataPointEntries[0];
  cy.get(`[data-test="data-point-row-${firstDataPointId}"]`).find('button.kpi-link').click();
  cy.get('[data-test="judge-modal"]').should('be.visible');

  // 2 Loop through all datapoints without QA reports and patch them as accepted original
  dataPointEntries.forEach(([dataPointType], index) => {
    if (index > 0) {
      selectNextDataPointToJudge(dataPointType);
      goToSelectedDataPoint();
    }

    const judgement: QaJudgement = {
      acceptedSource: AcceptedDataPointSource.Original,
    };

    const alias = `patchDatapoint_${dataPointType}`;
    cy.intercept('PATCH', `**/qa/dataset-judgements/**/data-points/${dataPointType}**`).as(alias);

    makeJudgementDecision(judgement);
    cy.wait(`@${alias}`).then((interception) => {
      checkPatchDataPointsCalledCorrectly(interception, judgement);
    });

    const dataPointId = overview.dataPointsWithoutQaReports[dataPointType];

    cy.waitUntil(() =>
      cy
        .get(`[data-test="data-point-row-${dataPointId}"] td`)
        .eq(1)
        .then(($td) => $td.find('.accepted-check').length > 0)
    );
  });

  checkOriginalDataPointsAccepted(dataPointEntries, overview);
}

/**
 * Reloads the Judgement page and checks the expected icons for the original datapoints.
 *
 * @param dataPointEntries Tuple entries of dataPointType and dataPointId.
 * @param overview         DataPointOverview containing data point IDs and counts.
 */
function checkOriginalDataPointsAccepted(dataPointEntries: Array<[string, string]>, overview: DataPointOverview): void {
  cy.reload();
  cy.closeCookieBannerIfItExists();
  cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
  cy.contains(
    `${Object.keys(overview.dataPointsWithQaReports).length} / ${overview.amountOfDataPointsToReview} data points to review`
  ).should('be.visible');

  dataPointEntries.forEach(([, dataPointId]) => {
    checkRowIcons(dataPointId, [IconState.Accepted, IconState.None, IconState.None, IconState.None]);
  });
}

/**
 * Checks if the "Finish Judgement" button is visible and disabled.
 */
function tryFinishingJudgementBeforeAllDataPointsReviewed(): void {
  cy.get('[data-test="qaReviewPageFinishButton"]').should('be.visible').and('be.disabled');
}

/**
 * Patches QA-report data points according to the configured QA scenarios, reloads once,
 * and verifies the expected icons for each data point row.
 *
 * @param datasetJudgementId The dataset judgement id to update.
 * @param judgeToken         Bearer token for the judge user.
 * @param overview           DataPointOverview containing data point IDs and counts.
 */
function judgeDataPointsWithQaReports(
  datasetJudgementId: string,
  judgeToken: string,
  overview: DataPointOverview
): void {
  const scenarios = QA_SCENARIO_CONFIG;

  // 1) Open judge modal for the first data point with QA
  const firstScenario = scenarios[0];
  const firstDataPointTypeId = overview.dataPointsWithQaReports[firstScenario.dataPointType];

  cy.get(`[data-test="data-point-row-${firstDataPointTypeId}"]`).find('button.kpi-link').click();
  cy.get('[data-test="judge-modal"]').should('be.visible');

  // 2) Loop through QA scenarios, using the modal + helpers
  scenarios.forEach((scenario, index) => {
    if (index > 0) {
      selectNextDataPointToJudge(scenario.dataPointType);
      goToSelectedDataPoint();
    }

    const judgement = scenario.judgement;

    const patchDataPointAlias = `patchDatapoint_${scenario.dataPointType}`;
    cy.intercept('PATCH', `**/qa/dataset-judgements/**/data-points/${scenario.dataPointType}**`).as(
      patchDataPointAlias
    );
    makeJudgementDecision(judgement);

    cy.wait(`@${patchDataPointAlias}`).then((interception) => {
      checkPatchDataPointsCalledCorrectly(interception, judgement);
    });

    const dataPointId = overview.dataPointsWithQaReports[scenario.dataPointType];

    cy.waitUntil(() =>
      cy.get(`[data-test="data-point-row-${dataPointId}"] td`).then(($tds) => {
        const accepted = $tds.eq(1).find('.accepted-check').length > 0;
        const rejected = $tds.eq(1).find('.rejected-check').length > 0;
        return accepted || rejected;
      })
    );
  });

  // 3) Reload and assert icons as before
  cy.reload();
  cy.closeCookieBannerIfItExists();
  cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
  cy.contains(`0 / ${overview.amountOfDataPointsToReview} data points to review`).should('be.visible');

  scenarios.forEach((scenario) => {
    const dataPointId = overview.dataPointsWithQaReports[scenario.dataPointType];
    const expectedIcons = buildExpectedIconsForScenario(scenario);
    checkRowIcons(dataPointId, expectedIcons);
  });
}

/**
 * Calculate the expected Icons for a data point based on the QA scenario configuration and the judgement.
 *
 * Column Order: [Original, Reviewer-QA, Admin-QA, Custom]
 *
 * @param scenario QA scenario configuration including judgement rules.
 */
function buildExpectedIconsForScenario(scenario: QaScenarioConfig): IconState[] {
  const originalIcon =
    scenario.judgement.acceptedSource === AcceptedDataPointSource.Original ? IconState.Accepted : IconState.Rejected;

  const reviewerAction = scenario.qaReports.find((a) => a.role === 'reviewer');
  const adminAction = scenario.qaReports.find((a) => a.role === 'admin');

  const reviewerIcon = buildQaIconForAction(reviewerAction, scenario.judgement, reviewer_userId);
  const adminIcon = buildQaIconForAction(adminAction, scenario.judgement, admin_userId);

  const customIcon = buildCustomIcon(scenario.judgement);

  return [originalIcon, reviewerIcon, adminIcon, customIcon];
}

/**
 * Determines the expected icon state for a QA report based on verdict, judgement, and reporter.
 *
 * Rules:
 * - If no QA report is present: IconState.None.
 * - If verdict = `QaReportDataPointVerdict.QaAccepted`: IconState.None (no icon in the QA column).
 * - If verdict = `QaReportDataPointVerdict.QaRejected`:
 *   - and this QA was accepted as the source -> IconState.Accepted
 *   - otherwise -> IconState.Rejected
 *
 * @param qaReport      QA report whose icon state should be calculated; if undefined, no icon is shown.
 * @param judgement     Judgement configuration of the QA scenario used to interpret the QA report.
 * @param reporterUserId User ID of the reporter for whom the icon state should be determined.
 * @return              The icon state representing an accepted, rejected, or absent QA report.
 */
function buildQaIconForAction(
  qaReport: QaReport | undefined,
  judgement: QaJudgement,
  reporterUserId: string
): IconState {
  if (!qaReport) {
    return IconState.None;
  }

  if (qaReport.verdict === QaReportDataPointVerdict.QaAccepted) {
    return IconState.None;
  }

  const isAcceptedQa =
    judgement.acceptedSource === AcceptedDataPointSource.Qa &&
    judgement.reporterUserIdOfAcceptedQaReport === reporterUserId;

  return isAcceptedQa ? IconState.Accepted : IconState.Rejected;
}

/**
 * Determines the expected icon state for the custom column based on the judgement configuration.
 *
 * Rules:
 * - If no customValue is set -> IconState.None
 * - If customValue is set:
 *   - and acceptedSource = 'Custom' -> IconState.Accepted
 *   - otherwise -> IconState.Rejected
 *
 * @param judgement Judgement configuration of the QA scenario used to evaluate the custom data point.
 * @return          The icon state representing an accepted, rejected, or absent custom data point.
 */
function buildCustomIcon(judgement: QaJudgement): IconState {
  if (!judgement.customValue) {
    return IconState.None;
  }
  return judgement.acceptedSource === AcceptedDataPointSource.Custom ? IconState.Accepted : IconState.Rejected;
}

/**
 * Finishes the judgement with the given dataset judgement id by clicking the "Finish Judgement" button
 *
 * @param dataSetId The id of the dataset that should be finished.
 */
function finishJudgement(dataSetId: string): void {
  cy.contains('button', 'FINISH REVIEW').should('be.visible').click();
  cy.get('.p-dialog')
    .should('be.visible')
    .within(() => {
      cy.contains('button', 'CONFIRM').should('exist').click();
      cy.contains('Dataset review completed.').should('be.visible');
    });
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.contains('[data-test="qa-review-data-id"]', dataSetId).should('not.exist');
}

/**
 * Builds a map of expected data point values by type, derived from QA scenarios and the original fixture.
 *
 * For each data point type in the path map:
 * - Uses the original fixture value if no scenario exists or the accepted source is `Original`.
 * - Uses the custom value from the scenario if the accepted source is `Custom`.
 * - Uses the corrected value of the accepted QA reporter if the accepted source is `Qa`.
 *
 * @param scenarios QA scenario configurations defining accepted sources and expected values.
 * @param fixture   Original uploaded fixture used as baseline/fallback values.
 * @returns         A record mapping each data point type to its expected string value.
 */
export function buildDatasetExpectationFromQaScenario(
  scenarios: QaScenarioConfig[],
  fixture: EutaxonomyFinancialsData
): Record<string, string> {
  const scenarioByDatapointType = new Map<string, QaScenarioConfig>();
  scenarios.forEach((s) => scenarioByDatapointType.set(s.dataPointType, s));

  const result: Record<string, string> = {};

  Object.keys(DATA_POINT_PATH_MAP).forEach((dataPointType) => {
    const originalValue = extractValueForType(dataPointType, fixture);
    const scenario = scenarioByDatapointType.get(dataPointType);

    const acceptedSource = scenario?.judgement.acceptedSource;
    if (acceptedSource === AcceptedDataPointSource.Custom) {
      const custom = parseJsonValue(scenario!.judgement.customValue);
      result[dataPointType] = custom ?? originalValue;
    } else if (acceptedSource === AcceptedDataPointSource.Qa) {
      const acceptedReporterId = scenario!.judgement.reporterUserIdOfAcceptedQaReport;

      let acceptedRole: QaRole | undefined;

      if (acceptedReporterId === reviewer_userId) {
        acceptedRole = 'reviewer';
      } else if (acceptedReporterId === admin_userId) {
        acceptedRole = 'admin';
      }

      const acceptedQaReport = acceptedRole ? scenario!.qaReports.find((r) => r.role === acceptedRole) : undefined;
      const qaValue = parseJsonValue(acceptedQaReport?.correctedValue);

      result[dataPointType] = qaValue ?? originalValue;
    } else {
      result[dataPointType] = originalValue;
    }
  });

  return result;
}

/**
 * Verifies that judged data points are persisted with the expected values.
 *
 * The helper derives expected values from QA scenarios and fixture defaults, fetches the
 * stored EU taxonomy financials for the given company/reporting period, and compares each
 * data point in the overview against the persisted backend value.
 *
 * @param overview         Data point IDs (with and without QA reports) used to determine which keys to verify.
 * @param scenarios        QA scenario configurations defining accepted sources and expected values.
 * @param fixture          Original uploaded fixture data used as a baseline/fallback for expectations.
 * @param companyId        Company ID used to query the active dataset.
 * @param reportingPeriod  Reporting period used to query the active dataset.
 * @param judgeToken       Bearer token for authentication.
 */
function verifyJudgementDataStoredCorrectly(
  overview: DataPointOverview,
  scenarios: QaScenarioConfig[],
  fixture: EutaxonomyFinancialsData,
  companyId: string,
  reportingPeriod: string,
  judgeToken: string
): void {
  const expectedValuesByType = buildDatasetExpectationFromQaScenario(scenarios, fixture);

  const flatOverview = { ...overview.dataPointsWithQaReports, ...overview.dataPointsWithoutQaReports };

  cy.waitUntil(() =>
    cy
      .request({
        method: 'GET',
        url: `${apiBaseUrl}/api/data/eutaxonomy-financials/`,
        qs: { companyId, reportingPeriod },
        headers: { Authorization: `Bearer ${judgeToken}` },
        failOnStatusCode: false,
      })
      .then((response) => {
        if (response.status !== 200) return false;
        const data = (response.body as { data: EutaxonomyFinancialsData }).data;

        return Object.keys(flatOverview).every((key) => {
          const expected = expectedValuesByType[key];
          const actual = extractValueForType(key, data);
          return actual === expected;
        });
      })
  );
}

/**
 * Checks the icons in the row of the given data point id and verifies that they match the expected ones.
 *
 * @param dataPointId   Id of the datapoint we want to check.
 * @param expectedIcons The expected icons of the given datapoint.
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
 * Reject Dataset via the button on the Judgement Page.
 *
 * @param dataSetId The id of the dataset that should be rejected.
 */
function rejectDatasetInJudgementModal(dataSetId: string): void {
  cy.get('[data-test="qaReviewPageRejectButton"]').should('be.visible').click();
  cy.get('.p-dialog')
    .should('be.visible')
    .within(() => {
      cy.contains('button', 'CONFIRM').should('exist').click();
      cy.contains('Dataset successfully rejected.').should('be.visible');
    });
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.contains('[data-test="qa-review-data-id"]', dataSetId).should('not.exist');
}
