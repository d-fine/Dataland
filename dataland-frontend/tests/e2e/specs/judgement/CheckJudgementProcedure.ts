import { type DataMetaInformation, type EutaxonomyFinancialsData, type StoredCompany } from '@clients/backend';
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

enum IconState {
  Accepted,
  Rejected,
  None,
}

type QaVerdict = 'QaAccepted' | 'QaRejected';
type QaRole = 'reviewer' | 'admin';
type AcceptedSource = 'Original' | 'Qa' | 'Custom';

type QaTokens = {
  reviewerToken: string;
  adminToken: string;
  uploaderToken: string;
  judgeToken: string;
};

interface QaReport {
  role: QaRole;
  verdict: QaVerdict;
  correctedValue?: string;
}

interface QaJudgement {
  acceptedSource?: AcceptedSource;
  reporterUserIdOfAcceptedQaReport?: string;
  customDataPoint?: string;
}

interface QaScenarioConfig {
  dataPointType: DataPointType;
  qaReports: QaReport[];
  judgement: QaJudgement;
}

interface DataPointOverview {
  dataPointsWithQaReports: Record<string, string>;
  dataPointsWithoutQaReports: Record<string, string>;
  amountOfDataPointsToReview: number;
}

interface PatchDataPointOptions extends QaJudgement {
  dataPointType: string;
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

const QA_SCENARIO_CONFIG: QaScenarioConfig[] = [
  {
    dataPointType: DATA_POINT_TYPES.fiscalYearEnd,
    qaReports: [
      { role: 'reviewer', verdict: 'QaAccepted' },
      { role: 'admin', verdict: 'QaAccepted' },
    ],
    judgement: {
      acceptedSource: 'Original',
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.greenAssetRatioTotal,
    qaReports: [
      { role: 'reviewer', verdict: 'QaRejected', correctedValue: '{"value":"5453445343", "currency":"EUR"}' },
      { role: 'admin', verdict: 'QaRejected', correctedValue: '{"value":"74568964325", "currency":"EUR"}' },
    ],
    judgement: {
      acceptedSource: 'Custom',
      customDataPoint: '{"value":"400400400.23", "currency":"EUR"}',
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.isNfrdMandatory,
    qaReports: [
      { role: 'reviewer', verdict: 'QaRejected', correctedValue: '{"value":"No"}' },
      { role: 'admin', verdict: 'QaAccepted' },
    ],
    judgement: {
      acceptedSource: 'Qa',
      reporterUserIdOfAcceptedQaReport: reviewer_userId,
      customDataPoint: '{"value":"No"}',
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.numberOfEmployees,
    qaReports: [
      { role: 'reviewer', verdict: 'QaAccepted' },
      { role: 'admin', verdict: 'QaRejected', correctedValue: '{"value":"2409600.75"}' },
    ],
    judgement: {
      acceptedSource: 'Qa',
      reporterUserIdOfAcceptedQaReport: admin_userId,
    },
  },
];

describeIf(
  'As a user, I expect to be able to go through the full judgement process',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let storedCompany: StoredCompany;
    let preparedEuTaxonomyFixtures: Array<FixtureData<EutaxonomyFinancialsData>>;
    let companyName: string;
    let tokens: QaTokens;
    let uploadedDataMetaInfo: DataMetaInformation;
    let overview: DataPointOverview;

    before(function () {
      cy.fixture('CompanyInformationWithEutaxonomyFinancialsPreparedFixtures').then(function (jsonContent) {
        preparedEuTaxonomyFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const testCompany = generateDummyCompanyInformation(`company-for-testing-judgement-${Date.now()}`);
        return uploadCompanyViaApi(token, testCompany).then((newCompany) => {
          storedCompany = newCompany;
          companyName = storedCompany.companyInformation.companyName;
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

    it('Check full judgement process from upload to acceptance', () => {
      checkoutDataset(companyName);
      return startJudgement(companyName).then((datasetJudgementId: string) => {
        changeJudgeAssignment(companyName);
        judgeDataPointsWithoutQaReports(datasetJudgementId, tokens.judgeToken, overview);
        tryFinishingJudgementBeforeAllDataPointsReviewed();
        judgeDataPointsWithQaReports(datasetJudgementId, tokens.judgeToken, overview);
        finishJudgement(companyName);
      });
    });

    it('Check rejecting a Dataset on the Judgement Page works as expected', () => {
      login(admin_name, admin_pw);
      return startJudgement(companyName).then(() => {
        rejectDatasetInJudgementModal(companyName);
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
      reviewerToken: reviewerToken,
      adminToken: adminToken,
      uploaderToken: uploaderToken,
      judgeToken: judgeToken,
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
      } as DataPointOverview;
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
  cy.intercept('POST', '**/qa/dataset-judgements/**').as('startJudgementRequest');
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .contains('[data-test="qa-review-company-name"]', companyName)
    .closest('tr')
    .contains('td', 'Start Review')
    .click();

  cy.get('.p-dialog').should('be.visible').contains('button', 'CONFIRM').click();

  return cy.wait('@startJudgementRequest').then((interception) => {
    expect(interception.response?.statusCode).to.eq(201);
    const dataSetJudgementId = interception.response?.body?.dataSetJudgementId;
    return cy.wrap(dataSetJudgementId as string, { log: false });
  });
}

/**
 * Switches to the judge user, opens the review entry for the company, and assigns the dataset to the judge.
 *
 * @param companyName The name of the company owning the dataset to be judged.
 */
function changeJudgeAssignment(companyName: string): void {
  cy.intercept('PATCH', '**/qa/dataset-judgements/**/judge').as('reassignJudgement');
  logout();
  login(judge_name, judge_pw);
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .contains('[data-test="qa-review-company-name"]', companyName)
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

  dataPointEntries.forEach(([dataPointType]) => {
    patchDataPoint(datasetJudgementId, judgeToken, {
      dataPointType,
      acceptedSource: 'Original',
    });
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
  QA_SCENARIO_CONFIG.forEach((scenario) => {
    patchDataPoint(datasetJudgementId, judgeToken, {
      dataPointType: scenario.dataPointType,
      ...scenario.judgement,
    });
  });

  cy.reload();
  cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
  cy.contains(`0 / ${overview.amountOfDataPointsToReview} data points to review`).should('be.visible');

  QA_SCENARIO_CONFIG.forEach((scenario) => {
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
  const originalIcon = scenario.judgement.acceptedSource === 'Original' ? IconState.Accepted : IconState.Rejected;

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
 * - If verdict = QaAccepted: IconState.None (no icon in the QA column).
 * - If verdict = QaRejected:
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

  if (qaReport.verdict === 'QaAccepted') {
    return IconState.None;
  }

  const isAcceptedQa =
    judgement.acceptedSource === 'Qa' && judgement.reporterUserIdOfAcceptedQaReport === reporterUserId;

  return isAcceptedQa ? IconState.Accepted : IconState.Rejected;
}

/**
 * Determines the expected icon state for the custom column based on the judgement configuration.
 *
 * Rules:
 * - If no customDataPoint is set -> IconState.None
 * - If customDataPoint is set:
 *   - and acceptedSource = 'Custom' -> IconState.Accepted
 *   - otherwise -> IconState.Rejected
 *
 * @param judgement Judgement configuration of the QA scenario used to evaluate the custom data point.
 * @return          The icon state representing an accepted, rejected, or absent custom data point.
 */
function buildCustomIcon(judgement: QaJudgement): IconState {
  if (!judgement.customDataPoint) {
    return IconState.None;
  }
  return judgement.acceptedSource === 'Custom' ? IconState.Accepted : IconState.Rejected;
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
 * @param datasetJudgementId The dataset for which the datapoint should be patched.
 * @param token              Authentication token.
 * @param options            Options for patching the datapoint, including:
 *                           - dataPointType
 *                           - judgement-related fields (acceptedSource, reporterUserIdOfAcceptedQaReport, customDataPoint)
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
 * @param companyName Name of the company whose dataset should be rejected.
 */
function rejectDatasetInJudgementModal(companyName: string): void {
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
