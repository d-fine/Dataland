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
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';
import type { Interception } from 'cypress/types/net-stubbing';

// const shortTimeoutInMs = Number(Cypress.expose('short_timeout_in_ms') ?? 10000); // 1sec instead of default 10 secs
const shortTimeoutInMs = Number(750);

enum IconState {
  Accepted,
  Rejected,
  None,
}

type QaRole = 'reviewer' | 'admin';

type QaTokens = {
  reviewerToken: string;
  adminToken: string;
  uploaderToken: string;
  judgeToken: string;
};

interface QaReport {
  role: QaRole;
  verdict: QaReportDataPointVerdict;
  correctedValue?: string;
}

interface QaJudgement {
  acceptedSource?: AcceptedDataPointSource;
  reporterUserIdOfAcceptedQaReport?: string;
  reporterUserNameOfAcceptedQaReport?: string;
  customDataPoint?: string;
  customValue?: string;
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
      { role: 'reviewer', verdict: QaReportDataPointVerdict.QaAccepted },
      { role: 'admin', verdict: QaReportDataPointVerdict.QaAccepted },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Original,
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.greenAssetRatioTotal,
    qaReports: [
      {
        role: 'reviewer',
        verdict: QaReportDataPointVerdict.QaRejected,
        correctedValue: '{"value":"5453445343", "currency":"EUR"}',
      },
      {
        role: 'admin',
        verdict: QaReportDataPointVerdict.QaRejected,
        correctedValue: '{"value":"74568964325", "currency":"EUR"}',
      },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Custom,
      customValue: '400400400.23', // '{"value":"400400400.23", "currency":"EUR"}',
      customDataPoint: '400400400.23',
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.isNfrdMandatory,
    qaReports: [
      { role: 'reviewer', verdict: QaReportDataPointVerdict.QaRejected, correctedValue: '{"value":"No"}' },
      { role: 'admin', verdict: QaReportDataPointVerdict.QaAccepted },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Qa,
      reporterUserIdOfAcceptedQaReport: reviewer_userId,
      reporterUserNameOfAcceptedQaReport: 'Data Reviewer',
    },
  },
  {
    dataPointType: DATA_POINT_TYPES.numberOfEmployees,
    qaReports: [
      { role: 'reviewer', verdict: QaReportDataPointVerdict.QaAccepted },
      { role: 'admin', verdict: QaReportDataPointVerdict.QaRejected, correctedValue: '{"value":"2409600.75"}' },
    ],
    judgement: {
      acceptedSource: AcceptedDataPointSource.Qa,
      reporterUserIdOfAcceptedQaReport: admin_userId,
      reporterUserNameOfAcceptedQaReport: 'Data Admin',
    },
  },
];

/**
 * Removes the assurance datapoint from the dataset. The data point does not match the standard format and creates known issues with the judge modal
 * @param fixture
 */
function stripAssuranceFromFixture(
  fixture: FixtureData<EutaxonomyFinancialsData>
): FixtureData<EutaxonomyFinancialsData> {
  // Deep-clone so we never mutate the original fixture object
  const clone = JSON.parse(JSON.stringify(fixture)) as FixtureData<EutaxonomyFinancialsData>;

  try {
    const t = clone.t as unknown as {
      general?: {
        assurance?: unknown;
        general?: { assurance?: unknown } & Record<string, unknown>;
      } & Record<string, unknown>;
    };

    // Remove nested "assurance" under general.general.assurance
    if (t?.general?.general && Object.prototype.hasOwnProperty.call(t.general.general, 'assurance')) {
      delete (t.general.general as Record<string, unknown>)['assurance'];
    }

    // Remove top-level "assurance" under general.assurance
    if (t?.general && Object.prototype.hasOwnProperty.call(t.general, 'assurance')) {
      delete (t.general as Record<string, unknown>)['assurance'];
    }
  } catch {
    // If the structure is different for some fixture, ignore and leave it as-is
  }

  return clone;
}

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

        // Strip Assurance from every prepared fixture we’ll potentially use
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

    it.only('Check judge modal selects expected values and stores them after finishing review', () => {
      createJudgementAndOpenReviewPage(uploadedDataMetaInfo, tokens.judgeToken).then((dataSetJudgementId) => {
        judgeDataPointsWithoutQaReports(dataSetJudgementId, tokens.judgeToken, overview);
        tryFinishingJudgementBeforeAllDataPointsReviewed();
        judgeDataPointsWithQaReports(dataSetJudgementId, tokens.judgeToken, overview);
        finishJudgement(uploadedDataMetaInfo.dataId);

        cy.wait(shortTimeoutInMs * 4); // allow backend processing (adjust as needed)

        cy.request({
          method: 'GET',
          url: `${apiBaseUrl}/api/metadata/${uploadedDataMetaInfo.dataId}`,
          headers: { Authorization: `Bearer ${tokens.adminToken}` },
        }).then((response) => {
          expect(response.status).to.eq(200); // qaStatus value sometimes varies in case; normalize to be robust
          const qaStatus = String(response.body?.qaStatus ?? '').toLowerCase();
          expect(qaStatus, 'dataset qaStatus').to.eq('accepted');
        });

        cy.log(`here we are`);
        cy.pause();

        const euTaxonomyData = getPreparedFixture('lightweight-eu-taxo-financials-dataset', preparedEuTaxonomyFixtures);

        cy.log(`overview: ${JSON.stringify(overview)}`);
        cy.log(`overview: ${JSON.stringify(overview.dataPointsWithQaReports)}`);
        cy.log(`overview: ${JSON.stringify(overview.dataPointsWithoutQaReports)}`);
        cy.pause();

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
      cy.visit(`/qualityassurance/review/${dataSetJudgementId}`);
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
 * Selects the given data point type in the judge modal's "Next datapoint" dropdown.
 *
 * @param dataPointTypeId The data point type ID (e.g. DATA_POINT_TYPES.numberOfEmployees).
 */
function selectNextDataPointToJudge(dataPointTypeId: string): void {
  cy.get('[data-test="next-datapoint-section"]').within(() => {
    cy.get('[data-test="next-datapoint-select"]').click();
  });

  // Store the selector in a variable to keep the code clean
  const optionSelector = `[data-test="next-datapoint-option-${dataPointTypeId}"]`;

  // Split the chain into two separate Cypress commands
  cy.get(optionSelector).scrollIntoView();
  cy.get(optionSelector).click({ force: true });
}

/**
 * This function is only a debug helper which can be deleted later
 */
function goToSelectedDataPoint(): void {
  cy.get('[data-test="next-datapoint-section"]').within(() => {
    cy.get('[data-test="go-to-datapoint-button"]').click();
  });
}

/**
 * Helper that checks that the PATCH request to update a data point is called with the expected request body based on the judgement configuration.
 *
 * @param interception The Cypress interception object containing request and response details of the PATCH request.
 * @param judgement    The judgement configuration used to determine the expected request body values.
 */
function checkPATCHDataPointsCalledCorrectly(interception: Interception, judgement: QaJudgement): void {
  expect(interception.response?.statusCode, 'PATCH status code').to.eq(200);

  cy.log(
    `[patch] body.acceptedSource=${String(interception.request.body?.acceptedSource)} | expected=${String(judgement.acceptedSource)}`
  );
  // cy.pause();

  const body = interception.request.body ?? {};

  if (judgement.acceptedSource == null) {
    expect(body.acceptedSource ?? null, 'acceptedSource in request body').to.eq(null);
  } else {
    expect(body.acceptedSource, 'acceptedSource in request body').to.eq(judgement.acceptedSource);
  }

  if (judgement.reporterUserIdOfAcceptedQaReport == null) {
    expect(body.reporterUserIdOfAcceptedQaReport ?? null, 'reporterUserIdOfAcceptedQaReport in request body').to.eq(
      null
    );
  } else {
    expect(body.reporterUserIdOfAcceptedQaReport, 'reporterUserIdOfAcceptedQaReport in request body').to.eq(
      judgement.reporterUserIdOfAcceptedQaReport
    );
  }

  if (judgement.customDataPoint == null) {
    expect(body.customDataPoint ?? null, 'customDataPoint in request body').to.eq(null);
  } else {
    expect(body.customDataPoint, 'customDataPoint in request body').to.eq(judgement.customDataPoint);
  }
}

/**
 * Clicks the "Next" button in the Judge modal to navigate to the next QA report entry, and verifies that the label has changed.
 * @param targetReporterName
 * @param currentLabel
 */
function goToNextReportAndRecurse(targetReporterName: string, currentLabel: string): void {
  cy.log('Clicking next button...');

  cy.get('[data-test="corrected-datapoint-section"] [data-test="qa-next-button"]').then(($buttons) => {
    const $visible = $buttons.filter(':visible');
    const $next = $visible.length > 0 ? $visible.first() : $buttons.first();
    const isDisabled = $next.prop('disabled') === true || $next.is(':disabled');

    if (isDisabled) {
      throw new Error(`Reporter "${targetReporterName}" not found. No more entries.`);
    }

    cy.wrap($next).click({ force: $visible.length === 0 });
  });

  cy.get('[data-test="qa-current-reporter-label"]')
    .invoke('text')
    .should('not.equal', currentLabel) // 3. Simplified assertion
    .then(() => {
      navigateToProperQaReportRecursively(targetReporterName);
    });
}

/**
 * This function is used inside makeJudgementDecision to recursively scan QA report entries in the Judge modal until
 * the target reporter label is found.
 *
 * Failure behaviour: should throw if no further QA entry exists (next button disabled) and target was not found.
 */
export function navigateToProperQaReportRecursively(targetReporterName: string): void {
  cy.get('[data-test="qa-current-reporter-label"]')
    .invoke('text')
    .then((txt) => {
      const current = txt.trim(); // Good practice to trim immediately
      cy.log(`Current QA label: "${current}" | Target: "${targetReporterName}"`);

      if (current === targetReporterName) {
        cy.log('Label matched! Clicking accept-report-button');
        cy.get('[data-test="accept-report-button"]').click();
      } else {
        goToNextReportAndRecurse(targetReporterName, current);
      }
    });
}

/**
 * Helper that executes the UI interaction needed to apply a judgement in the open Judge modal.
 *
 * @param judgement Judgement configuration defining source selection and optional custom value.
 */
function makeJudgementDecision(judgement: QaJudgement): void {
  cy.log(`customValue: "${judgement.customValue}"`);

  if (judgement.customValue != null) {
    cy.log(`customValue is not null`);
    // cy.pause();
    cy.get('[data-test="custom-value-field"]').click();
    cy.get('[data-test="custom-value-field"]').clear();
    cy.get('[data-test="custom-value-field"]').type(judgement.customValue);
  }

  if (judgement.acceptedSource === AcceptedDataPointSource.Original) {
    // cy.log(`acceptedSource: original`);
    // cy.pause();
    cy.get('[data-test="accept-original-button"]').click();
    return;
  }

  if (judgement.acceptedSource === AcceptedDataPointSource.Custom) {
    cy.log(`acceptedSource: Custom`);
    // cy.pause();
    cy.get('[data-test="accept-custom-button"]').click();
    return;
  }

  if (judgement.acceptedSource === AcceptedDataPointSource.Qa) {
    cy.log(`acceptedSource: Qa`);
    // cy.pause();
    const target = judgement.reporterUserNameOfAcceptedQaReport;
    cy.log(`target: "${target}"`);

    if (!target) {
      throw new Error('Qa judgement requires reporterUserNameOfAcceptedQaReport for modal matching');
    }

    navigateToProperQaReportRecursively(target);
  }
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
  const dataPointEntries = Object.entries(overview.dataPointsWithoutQaReports); //.filter(
  //([dataPointType]) => !UNPATCHABLE_DATA_POINT_TYPES.includes(dataPointType)
  // );

  cy.log(`dataPointEntries: ${JSON.stringify(dataPointEntries)}`);
  // cy.pause();
  if (dataPointEntries.length === 0) return;

  // 1) Open the judge modal on the first datapoint without QA
  const [, firstDataPointId] = dataPointEntries[0];
  cy.get(`[data-test="data-point-row-${firstDataPointId}"]`).find('button.kpi-link').click();
  cy.get('[data-test="judge-modal"]').should('be.visible');

  // 2 Loop through all datapoints without QA reports and patch them as accepted original
  dataPointEntries.forEach(([dataPointType], index) => {
    cy.log(
      `[judge/no-qa] iteration=${index + 1}/${dataPointEntries.length}, index=${index}, dataPointType=${dataPointType}`
    );

    if (index > 0) {
      selectNextDataPointToJudge(dataPointType);
      goToSelectedDataPoint();
    }

    const judgement: QaJudgement = {
      acceptedSource: AcceptedDataPointSource.Original,
    };

    // 3 Make the judgement decision (click the button) and check if the decision is actually made
    // const dataPointId = overview.dataPointsWithoutQaReports[dataPointType];
    cy.intercept('PATCH', `**/qa/dataset-judgements/**/data-points/${dataPointType}**`).as('patchDatapoint');
    // cy.log(`[expected] dataPointId=${dataPointId}`);
    // cy.intercept('PATCH', '**/qa/dataset-judgements/**/data-points/**').as('patchDatapoint');
    // cy.log(`[judge/no-qa] applying judgement, acceptedSource=${judgement.acceptedSource}`);
    makeJudgementDecision(judgement);
    cy.wait('@patchDatapoint').then((interception) => {
      checkPATCHDataPointsCalledCorrectly(interception, judgement);
      cy.log(`url: ${interception.request.url}`);
      cy.log(`[judge/no-qa] PATCH finished for index=${index}, dataPointType=${dataPointType}`);
      console.log('[judge/no-qa] context', { index, dataPointType, judgement });
    });
    // cy.pause();
    cy.wait(shortTimeoutInMs); // This waiting time is crucial in order to run the e2e tests, patching the correct URL!
    // cy.pause();
  });

  // cy.pause();
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

  cy.log(
    `Check succeeded!! There are exactly ${Object.keys(overview.dataPointsWithQaReports).length} / ${overview.amountOfDataPointsToReview} data points to review`
  );
  // cy.pause();

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

  if (scenarios.length === 0) return;

  // 1) Open judge modal for the first datapoint with QA
  const firstScenario = scenarios[0];
  const firstDataPointTypeId = overview.dataPointsWithQaReports[firstScenario.dataPointType];

  cy.get(`[data-test="data-point-row-${firstDataPointTypeId}"]`).find('button.kpi-link').click();
  cy.get('[data-test="judge-modal"]').should('be.visible');

  // 2) Loop through QA scenarios, using the modal + helpers
  scenarios.forEach((scenario, index) => {
    cy.log(`message 1/2 loop through QA scenarios, index: ${index + 1}, datapointType: ${scenario.dataPointType}`);
    // cy.pause();

    if (index > 0) {
      selectNextDataPointToJudge(scenario.dataPointType);
      goToSelectedDataPoint();
    }

    const judgement = scenario.judgement;

    cy.intercept('PATCH', `**/qa/dataset-judgements/**/data-points/${scenario.dataPointType}**`).as('patchDatapoint');
    makeJudgementDecision(judgement);

    cy.log(`message 2/2 loop through QA scenarios, index: ${index + 1}, datapointType: ${scenario.dataPointType}`);
    // cy.pause();

    cy.wait('@patchDatapoint').then((interception) => {
      cy.log(
        `[patch] body.acceptedSource=${String(interception.request.body?.acceptedSource)} | expected=${String(judgement.acceptedSource)}`
      );
      cy.log(`[patch] url=${interception.request.url}`);
      // cy.pause();
      cy.wait(shortTimeoutInMs);
      //checkPATCHDataPointsCalledCorrectly(interception, judgement);
    });
    cy.log(`patched`);
    // cy.pause();
  });

  // 3) Reload and assert icons as before
  cy.reload();
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
 * Safely parses a JSON string and extracts the 'value' property if it exists.
 * If the value is an object, it is stringified via JSON to avoid [object Object].
 *
 * @param raw - The raw JSON string to be parsed.
 * @returns The extracted value as a string, or the original string if parsing fails.
 */
function parseJsonValue(raw?: string): string | undefined {
  if (raw == null) return undefined;

  try {
    const parsed: unknown = JSON.parse(raw);

    if (typeof parsed !== 'object' || parsed === null || !('value' in parsed)) {
      return raw;
    }

    const val = (parsed as { value?: unknown }).value;

    if (val == null) {
      return raw;
    }

    if (typeof val === 'string') {
      return val;
    }

    if (typeof val === 'number' || typeof val === 'boolean') {
      return val.toString();
    }

    return JSON.stringify(val);
  } catch {
    return raw;
  }
}

/**
 * Extract value from get endpoint
 * @param dataPointType
 * @param data
 */
function extractValueForType(dataPointType: string, data: EutaxonomyFinancialsData): string {
  switch (dataPointType) {
    case DATA_POINT_TYPES.fiscalYearEnd:
      return String(data.general?.general?.fiscalYearEnd?.value ?? '');
    case DATA_POINT_TYPES.isNfrdMandatory:
      return String(data.general?.general?.isNfrdMandatory?.value ?? '');
    case DATA_POINT_TYPES.numberOfEmployees:
      return String(data.general?.general?.numberOfEmployees?.value ?? '');
    case 'extendedEnumFiscalYearDeviation':
      return String(data.general?.general?.fiscalYearDeviation?.value ?? '');
    case 'extendedEnumYesNoAreAllGroupEntitiesCoveredByEuTaxonomyReports':
      return String(data.general?.general?.areAllGroupEntitiesCovered?.value ?? '');

    case DATA_POINT_TYPES.greenAssetRatioTotal:
      return String(
        data.creditInstitution?.assetsForCalculationOfGreenAssetRatio?.totalGrossCarryingAmount?.value ?? ''
      );

    case 'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfAssetsTowardsTaxonomyRelevantSectorsTaxonomyEligible':
      return String(
        data.creditInstitution?.assetsForCalculationOfGreenAssetRatio
          ?.totalAmountOfAssetsTowardsTaxonomyRelevantSectorsTaxonomyEligible?.value ?? ''
      );

    case 'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfAssetsWhichAreEnvironmentallySustainableTaxonomyAligned':
      return String(
        data.creditInstitution?.assetsForCalculationOfGreenAssetRatio
          ?.totalAmountOfAssetsWhichAreEnvironmentallySustainableTaxonomyAligned?.value ?? ''
      );

    case 'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreUseOfProceeds':
      return String(
        data.creditInstitution?.assetsForCalculationOfGreenAssetRatio
          ?.totalAmountOfEnvironmentallySustainableAssetsWhichAreUseOfProceeds?.value ?? ''
      );

    case 'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreTransitional':
      return String(
        data.creditInstitution?.assetsForCalculationOfGreenAssetRatio
          ?.totalAmountOfEnvironmentallySustainableAssetsWhichAreTransitional?.value ?? ''
      );

    case 'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreEnabling':
      return String(
        data.creditInstitution?.assetsForCalculationOfGreenAssetRatio
          ?.totalAmountOfEnvironmentallySustainableAssetsWhichAreEnabling?.value ?? ''
      );
  }
  // Ensure a string is always returned for any (possibly unknown) dataPointType
  return '';
}

// Generic KPI extraction utility: traverses the fixture and returns dotted-path
// KPI name / value pairs for any node that has an own "value" property.
type KPI = { name: string; value: string | number | null };

/**
 * Extract KPI name/value pairs from an arbitrary fixture.
 * A KPI node is any object that has an own `value` property.
 */
function extractKpis(fixture: unknown, options?: { rootKey?: string; includeArrayIndex?: boolean }): KPI[] {
  const out: KPI[] = [];
  const root =
    options?.rootKey &&
    fixture &&
    typeof fixture === 'object' &&
    options.rootKey in (fixture as Record<string, unknown>)
      ? (fixture as Record<string, unknown>)[options.rootKey]
      : fixture;

  const normalize = (v: unknown): string | number | null => {
    if (v === null || v === undefined) return null;
    if (typeof v === 'number') return v;
    if (typeof v === 'string') return v;
    if (typeof v === 'boolean') return String(v);
    try {
      return JSON.stringify(v);
    } catch {
      return null;
    }
  };

  /**
   * If `node` contains a value property, push it to the result and return true.
   */
  function pushIfValue(node: unknown, path: string[]): boolean {
    if (node == null || typeof node !== 'object') return false;
    if (!Object.prototype.hasOwnProperty.call(node, 'value')) return false;

    const nodeRec = node as Record<string, unknown>;
    let v = nodeRec['value'];

    // prefer inner .value when present and primitive-ish
    if (v && typeof v === 'object' && Object.prototype.hasOwnProperty.call(v, 'value')) {
      const inner = (v as Record<string, unknown>)['value'];
      if (
        inner === null ||
        inner === undefined ||
        typeof inner === 'number' ||
        typeof inner === 'string' ||
        typeof inner === 'boolean'
      ) {
        v = inner;
      }
    }

    out.push({ name: path.join('.'), value: normalize(v) });
    return true;
  }

  /**
   * Recursively walk the fixture tree and collect KPI nodes.
   */
  function walk(node: unknown, path: string[]): void {
    if (node == null) return;
    if (pushIfValue(node, path)) return;

    if (Array.isArray(node)) {
      node.forEach((item, idx) => {
        if (options?.includeArrayIndex) {
          path.push(String(idx));
          walk(item, path);
          path.pop();
        } else {
          walk(item, path);
        }
      });
      return;
    }

    if (typeof node === 'object') {
      for (const key of Object.keys(node as Record<string, unknown>)) {
        path.push(key);
        walk((node as Record<string, unknown>)[key], path);
        path.pop();
      }
    }
  }

  walk(root, []);
  return out;
}

/** This function returns the selected value for each KPI, based on the indicated AcceptedDataPointSource
 *
 * @param scenarios
 * @param fixture
 */
function buildExpectedByType(
  scenarios: QaScenarioConfig[],
  fixture: EutaxonomyFinancialsData
): Record<DataPointType, string> {
  const scenarioByType = new Map<DataPointType, QaScenarioConfig>();
  scenarios.forEach((s) => scenarioByType.set(s.dataPointType, s));

  const result = {} as Record<DataPointType, string>;

  // Determine which KPI types actually appear in the fixture by checking
  // whether extractValueForType returns a non-empty value. This ensures we
  // consider every KPI present in the fixture. If none are detectable, fall
  // back to the full set of known DATA_POINT_TYPES for backward compatibility.
  const allTypes = Object.values(DATA_POINT_TYPES) as DataPointType[];
  const presentTypes = allTypes.filter((t) => {
    const v = extractValueForType(t, fixture);
    return v != null && v !== '';
  });

  const typesToConsider = presentTypes.length > 0 ? presentTypes : allTypes;

  typesToConsider.forEach((dataPointType) => {
    const originalValue = extractValueForType(dataPointType, fixture);
    const scenario = scenarioByType.get(dataPointType);

    const acceptedSource = scenario?.judgement.acceptedSource;
    if (!scenario || acceptedSource == null || acceptedSource === AcceptedDataPointSource.Original) {
      result[dataPointType] = originalValue;
      cy.log(`data point: ${dataPointType}, value: ${result[dataPointType]} (original)`);
      return;
    }

    if (acceptedSource === AcceptedDataPointSource.Custom) {
      const custom = parseJsonValue(scenario.judgement.customValue ?? scenario.judgement.customDataPoint);
      result[dataPointType] = custom ?? originalValue;
      return;
    }

    if (acceptedSource === AcceptedDataPointSource.Qa) {
      const acceptedReporterId = scenario.judgement.reporterUserIdOfAcceptedQaReport;
      const acceptedRole: QaRole | undefined =
        acceptedReporterId === reviewer_userId ? 'reviewer' : acceptedReporterId === admin_userId ? 'admin' : undefined;

      const acceptedQaReport = acceptedRole ? scenario.qaReports.find((r) => r.role === acceptedRole) : undefined;
      const qaValue = parseJsonValue(acceptedQaReport?.correctedValue);

      result[dataPointType] = qaValue ?? originalValue;
      return;
    }

    result[dataPointType] = originalValue;
  });

  // Also include any KPIs found in the fixture that are not represented by a
  // DataPointType entry. This ensures the returned map contains keys for
  // dotted-path KPIs (e.g. "general.general.fiscalYearEnd") so callers that
  // inspect fixture-level KPIs can find the original values.
  try {
    const fixtureKpis = extractKpis(fixture, { rootKey: undefined, includeArrayIndex: false });
    fixtureKpis.forEach((k) => {
      const asMap = result as Record<string, string>;
      if (!Object.prototype.hasOwnProperty.call(asMap, k.name)) {
        asMap[k.name] = k.value == null ? '' : String(k.value);
      }
    });
  } catch {
    // If extraction fails for any reason, silently continue and return the
    // DataPointType-based result as before.
  }

  return result;
}

type MyMap = Record<string, string>;

/** buildMyMap
 *
 */
function buildMyMap(): MyMap {
  return {
    extendedDateFiscalYearEnd: 'general.general.fiscalYearEnd',
    extendedEnumYesNoIsNfrdMandatory: 'general.general.isNfrdMandatory',
    extendedDecimalNumberOfEmployees: 'general.general.numberOfEmployees',
    extendedEnumYesNoAreAllGroupEntitiesCoveredByEuTaxonomyReports: 'general.general.areAllGroupEntitiesCovered',
    extendedEnumFiscalYearDeviation: 'general.general.fiscalYearDeviation',

    extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount:
      'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalGrossCarryingAmount',

    extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfAssetsTowardsTaxonomyRelevantSectorsTaxonomyEligible:
      'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfAssetsTowardsTaxonomyRelevantSectorsTaxonomyEligible',

    extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfAssetsWhichAreEnvironmentallySustainableTaxonomyAligned:
      'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfAssetsWhichAreEnvironmentallySustainableTaxonomyAligned',

    extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreUseOfProceeds:
      'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfEnvironmentallySustainableAssetsWhichAreUseOfProceeds',

    extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreTransitional:
      'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfEnvironmentallySustainableAssetsWhichAreTransitional',

    extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalAmountOfEnvironmentallySustainableAssetsWhichAreEnabling:
      'creditInstitution.assetsForCalculationOfGreenAssetRatio.totalAmountOfEnvironmentallySustainableAssetsWhichAreEnabling',
  };
}

// /**
//  * Supports dot notation paths like:
//  * "general.general.fiscalYearEnd"
//  */
// function getNestedValue(obj: any, path: string): any {
//   return path.split('.').reduce((acc, part) => {
//     if (acc && typeof acc === 'object') {
//       return acc[part];
//     }
//     return undefined;
//   }, obj);
// }

/**
 *
 * @param extracted_KPIs
 */
function toKpiMap(extracted_KPIs: KPI[]): Record<string, string | number | null> {
  return Object.fromEntries(extracted_KPIs.map((kpi) => [kpi.name, kpi.value as string | number | null])) as Record<
    string,
    string | number | null
  >;
}

/** resolveExpectedValue
 *
 * @param key
 * @param expectedValuesByType
 * @param kpis
 * @param myMap
 */
function resolveExpectedValue(
  key: string,
  expectedValuesByType: Record<string, string>,
  // fixture: EutaxonomyFinancialsData,
  kpis: KPI[],
  myMap: MyMap
): string {
  if (key in expectedValuesByType) {
    return expectedValuesByType[key];
  }

  const fixturePath = myMap[key];
  if (!fixturePath) {
    throw new Error(`No mapping found for key: ${key}`);
  }

  const kpiMap = toKpiMap(kpis);

  // const value = getNestedValue(fixture, fixturePath);
  const value = kpiMap[fixturePath];

  return value !== undefined && value !== null ? String(value) : '';
}

/**
 * Verifies that after finishing the judgement, the active dataset returned by the backend API
 * contains the expected values for each data point based on its accepted source.
 *
 * @param overview
 * @param scenarios        QA scenario configurations defining accepted sources and expected values.
 * @param fixture          The original fixture data uploaded before the judgement.
 * @param companyId        The company ID used to query the active dataset.
 * @param reportingPeriod  The reporting period used to query the active dataset.
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
  cy.log(`scenarios: ${JSON.stringify(scenarios)}`);
  cy.pause();

  cy.log(`fixture: ${JSON.stringify(fixture)}`);
  cy.pause();

  // Extract and log all KPI name/value pairs found in the fixture
  const kpis = extractKpis(fixture, { rootKey: undefined, includeArrayIndex: false });
  cy.log(`extracted KPIs (${kpis.length}): ${JSON.stringify(kpis)}`);

  cy.pause();
  // const fixtureTypes = allTypes.filter((t) => hasFieldForType(t, fixture));
  // cy.log(`fixtureTypes: ${JSON.}`)

  const expectedValuesByType = buildExpectedByType(scenarios, fixture);
  cy.log(`[verify] expected values: ${JSON.stringify(expectedValuesByType)}`);

  cy.pause();

  // Allow the message queue to process data point replacements posted during finalization.
  cy.wait(shortTimeoutInMs * 4);

  const myMap = buildMyMap();

  cy.request({
    method: 'GET',
    url: `${apiBaseUrl}/api/data/eutaxonomy-financials/`,
    qs: { companyId, reportingPeriod },
    headers: { Authorization: `Bearer ${judgeToken}` },
  }).then((response) => {
    expect(response.status, 'GET eutaxonomy-financials status').to.eq(200);
    const data = (response.body as { data: EutaxonomyFinancialsData }).data;

    const flatOverview = { ...overview.dataPointsWithQaReports, ...overview.dataPointsWithoutQaReports };

    cy.log(`flatOverview: ${JSON.stringify(flatOverview)}`);
    cy.pause();

    Object.keys(flatOverview).forEach((key) => {
      // const expected = (expectedValuesByType as Record<string, string>)[key];
      const expected = resolveExpectedValue(key, expectedValuesByType, kpis, myMap);
      const actual = extractValueForType(key, data);
      cy.log(`[verify] ${key}: actual="${actual}" expected="${expected}"`);
      expect(actual, `stored value for ${key}`).to.eq(expected);
      cy.pause();
    });
  });
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
