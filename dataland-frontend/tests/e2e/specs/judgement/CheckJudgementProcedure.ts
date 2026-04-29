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

    // This test is only a debug helper which can be deleted later
    it('debug judge modal selectors', () => {
      createJudgementAndOpenReviewPage(uploadedDataMetaInfo, tokens.judgeToken).then(() => {
        // 1) Compute the type + id for "Number of Employees"
        const typeId = DATA_POINT_TYPES.numberOfEmployees;
        const dataPointId = overview.dataPointsWithQaReports[typeId] ?? overview.dataPointsWithoutQaReports[typeId];

        // 2) Click the KPI row to open the judge modal
        cy.get(`[data-test="data-point-row-${dataPointId}"]`).find('button.kpi-link').click();

        // 3) Now run the selector checks on the modal
        selectNextDataPointToJudge(typeId);
        goToSelectedDataPoint();

        // 4) Set up intercept BEFORE making the judgement decision
        cy.intercept('PATCH', `**/qa/dataset-judgements/**/data-points/${dataPointId}**`).as('patchDatapoint');

        // 5) Wait for all QA reports to be loaded in the modal
        const qaConfig = QA_SCENARIO_CONFIG.find((config) => config.dataPointType === typeId);
        const expectedQaReportCount = qaConfig?.qaReports.length ?? 0;

        if (expectedQaReportCount > 0) {
          // Wait for the nav count to match the expected number of QA reports
          cy.get('[data-test="corrected-datapoint-section"]')
            .contains(`(1 / ${expectedQaReportCount})`)
            .should('exist');
        }

        // 6) Get the judgement config and make the decision (this handles all UI interactions)
        const judgement = qaConfig?.judgement;
        if (judgement) {
          makeJudgementDecision(judgement);
        }
        cy.log(`makeJudgementDecision ran successfully!`);

        // 7) Wait for the PATCH and verify the request body
        cy.wait('@patchDatapoint').then((interception) => {
          if (judgement) {
            checkPATCHDataPointsCalledCorrectly(interception, judgement);
          }
        });
      });
    });

    it('Check creating a Judgement and reassigning the Judge works as expected', () => {
      const dataSetId = uploadedDataMetaInfo.dataId;
      checkoutDataset(dataSetId);
      startJudgement(dataSetId);
      changeJudgeAssignment(dataSetId);
    });

    it.only('Check accepting sources on judgement page and finishing with acceptance works as expected', () => {
      createJudgementAndOpenReviewPage(uploadedDataMetaInfo, tokens.judgeToken).then((dataSetJudgementId) => {
        judgeDataPointsWithoutQaReports(dataSetJudgementId, tokens.judgeToken, overview);
        // cy.pause();
        tryFinishingJudgementBeforeAllDataPointsReviewed();
        cy.pause();
        judgeDataPointsWithQaReports(dataSetJudgementId, tokens.judgeToken, overview);
        cy.pause();
        finishJudgement(uploadedDataMetaInfo.dataId);

        cy.log(`And now check that everything is uploaded correctly`);
        cy.pause();
        // Verify the judgement data was correctly stored in the backend and displayed on the framework page
        const euTaxonomyData = getPreparedFixture('lightweight-eu-taxo-financials-dataset', preparedEuTaxonomyFixtures);
        verifyJudgementDataStoredCorrectly(
          QA_SCENARIO_CONFIG,
          euTaxonomyData.t,
          uploadedDataMetaInfo.dataId,
          storedCompany.companyId,
          uploadedDataMetaInfo.dataType
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
      // checkPATCHDataPointsCalledCorrectly(interception, judgement);
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

// helper 1
function parseJsonValue(raw?: string): string | undefined {
  if (raw == null) return undefined;
  try {
    const parsed = JSON.parse(raw) as { value?: unknown };
    if (parsed && parsed.value != null) return String(parsed.value);
    return raw;
  } catch {
    return raw;
  }
}

// helper 2
function getOriginalValueForType(dataPointType: DataPointType, fixture: EutaxonomyFinancialsData): string {
  switch (dataPointType) {
    case DATA_POINT_TYPES.fiscalYearEnd:
      return String(fixture.general?.general?.fiscalYearEnd?.value ?? '');

    case DATA_POINT_TYPES.greenAssetRatioTotal:
      // use the exact path corresponding to this datapoint type in your fixture shape
      return String(
        fixture.alignment?.creditInstitution?.assetsForCalculationOfGreenAssetRatio?.total?.grossCarryingAmount
          ?.value ?? ''
      );

    case DATA_POINT_TYPES.isNfrdMandatory:
      return String(fixture.general?.general?.isNfrdMandatory?.value ?? '');

    case DATA_POINT_TYPES.numberOfEmployees:
      return String(fixture.general?.general?.numberOfEmployees?.value ?? '');

    default:
      return '';
  }
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

  (Object.values(DATA_POINT_TYPES) as DataPointType[]).forEach((dataPointType) => {
    const originalValue = getOriginalValueForType(dataPointType, fixture);
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

  return result;
}

/**
 * Verifies that the judgement data is correctly stored and displayed on the company framework page.
 * Navigates to the company framework page and checks that all judged data points are displayed
 * with their correct accepted sources and custom values.
 *
 * (@param dataSetId           The id of the dataset that was judged.)
 * (@param judgeToken          Bearer token for the judge user to access the API.)
 *
 * @param scenarios           Array of QA scenario configurations used to validate the stored judgement.
 * @param fixture             The input from JSON file.
 * @param dataId              The dataset ID to verify on the framework page (used in the URL).
 * @param companyId           The company ID to navigate to the framework page for UI verification.
 * @param frameworkType       The framework type (e.g., 'eutaxonomy-financials', 'sfdr') to construct the URL.
 * @returns                   A Cypress.Chainable that resolves after all verifications are complete.
 */
function verifyJudgementDataStoredCorrectly(
  scenarios: QaScenarioConfig[] = QA_SCENARIO_CONFIG,
  fixture?: EutaxonomyFinancialsData,
  dataId?: string,
  companyId?: string,
  frameworkType?: string
): void {
  // CHECK INPUT PARAMETERS DO EXIST
  // if (!companyId || !frameworkType) {
  // cy.log('⚠️  companyId or frameworkType not provided - skipping verification');
  // return cy.wrap(undefined);
  // }

  // CHECK YOU CAN GO TO THE COMPANY ID PAGE
  cy.visit(`/companies/${companyId}/frameworks/${frameworkType}/${dataId}`);

  cy.pause();
  cy.visit(`/companies/${companyId}/frameworks/${frameworkType}/${dataId}`);

  cy.pause();

  cy.get('tr[data-cell-label="Is NFRD mandatory?"]')
    .find('td')
    .eq(1)
    .invoke('text')
    .then((value) => {
      cy.log(value.trim());
    });

  cy.pause();

  // Build expected values from scenarios and fixture
  const expectedValuesByType = fixture ? buildExpectedByType(scenarios, fixture) : {};

  cy.log(`expected values: ${JSON.stringify(expectedValuesByType, null, 2)}`);
  cy.pause();

  // 1) Find the KPI row by exact label
  cy.get('tr[data-cell-label="Is NFRD mandatory?"]').within(() => {
    // 2) Click the value link in that row (the link that opens the modal)
    cy.get('a.link').first().click();
  });

  // 3) In the opened PrimeVue dialog, read the value at the right of "Value"
  cy.get('.p-dialog:visible').within(() => {
    cy.contains('th .table-left-label', 'Value')
      .closest('tr')
      .find('td')
      .invoke('text')
      .then((modalValue) => {
        const extracted = modalValue.trim();
        const expectedValue = expectedValuesByType[DATA_POINT_TYPES.isNfrdMandatory];
        cy.log(`Value from popup = ${extracted}`);
        cy.log(`Expected value = ${expectedValue}`);

        expect(extracted).to.equal(expectedValue);
      });
  });

  cy.pause();

  // after a loop over all KPIs
  cy.log('✓ All judgement data verified successfully in the company framework page');
}

/**
 * Verifies that the judgement data is correctly stored and displayed on the company framework page.
 * Navigates to the company framework page and checks that all judged data points are displayed
 * with their correct accepted sources and custom values.
 *
 * (@param dataSetId           The id of the dataset that was judged.)
 * (@param judgeToken          Bearer token for the judge user to access the API.)
 *
 * @param overview            DataPointOverview containing data point IDs and configurations.
 * @param scenarios           Array of QA scenario configurations used to validate the stored judgement.
 * @param dataId              The dataset ID to verify on the framework page (used in the URL).
 * @param companyId           The company ID to navigate to the framework page for UI verification.
 * @param frameworkType       The framework type (e.g., 'eutaxonomy-financials', 'sfdr') to construct the URL.
 * @param fixture             The input from JSON file.
 * @returns                   A Cypress.Chainable that resolves after all verifications are complete.
 */
function verifyJudgementDataStoredCorrectlyOld(
  overview: DataPointOverview,
  scenarios: QaScenarioConfig[] = QA_SCENARIO_CONFIG,
  dataId?: string,
  companyId?: string,
  frameworkType?: string,
  fixture?: EutaxonomyFinancialsData
): Cypress.Chainable<JQuery<HTMLElement>> {
  // if (!companyId || !frameworkType) {
  // cy.log('⚠️  companyId or frameworkType not provided - skipping verification');
  // return cy.wrap(undefined);
  // }

  // Build expected values from scenarios and fixture
  const expectedValuesByType = fixture ? buildExpectedByType(scenarios, fixture) : {};

  cy.visit(`/companies/${companyId}/frameworks/${frameworkType}/${dataId}`);
  // or: cy.visit('https://local-dev.dataland.com/companies/...')

  cy.get('tr[data-cell-label="Is NFRD mandatory?"]')
    .find('td')
    .eq(1)
    .invoke('text')
    .then((value) => {
      cy.log(value.trim());
    });

  cy.pause();

  // 1) Find the KPI row by exact label
  cy.get('tr[data-cell-label="Is NFRD mandatory?"]').within(() => {
    // 2) Click the value link in that row (the link that opens the modal)
    cy.get('a.link').first().click();
  });

  // 3) In the opened PrimeVue dialog, read the value at the right of "Value"
  cy.get('.p-dialog:visible').within(() => {
    cy.contains('th .table-left-label', 'Value')
      .closest('tr')
      .find('td')
      .invoke('text')
      .then((modalValue) => {
        const extracted = modalValue.trim();
        const expectedValue = expectedValuesByType[DATA_POINT_TYPES.isNfrdMandatory];
        cy.log(`Value from popup = ${extracted}`);
        cy.log(`Expected value = ${expectedValue}`);

        expect(extracted).to.equal(expectedValue);
      });
  });

  cy.pause();

  cy.get('tr[data-cell-label]').each(($row) => {
    const label = $row.attr('data-cell-label')?.trim();

    const uiValue = $row.find('td').eq(1).text().trim();

    cy.log(`label: ${label}, UI value: ${uiValue}`);
  });

  // Navigate to company framework page to verify UI displays the judged data correctly
  cy.log(`Navigating to company framework page: /companies/${companyId}/frameworks/${frameworkType}`);

  cy.request({
    url: `/companies/${companyId}/frameworks/${frameworkType}/${dataId}`,
    failOnStatusCode: false,
  })
    .its('status')
    .should('eq', 200); // or .should('be.oneOf', [200, 301, 302])

  cy.log(`The page does exist.`);
  cy.pause();

  return cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${frameworkType}/${dataId}`).then(() => {
    cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
    cy.log('✓ Framework page loaded and data table is visible');
    cy.pause();

    // Verify each data point judgement matches the expected scenario configuration
    scenarios.forEach((scenario) => {
      const dataPointId = overview.dataPointsWithQaReports[scenario.dataPointType];
      if (!dataPointId) {
        cy.log(`Skipping verification for ${scenario.dataPointType} - no data point ID found`);
        return;
      }

      cy.log(`Verifying data point ${dataPointId} (type: ${scenario.dataPointType}) - press continue`);
      cy.pause();

      // Verify the accepted source is displayed correctly
      verifyAcceptedSourceInUI(dataPointId, scenario.judgement.acceptedSource);

      // Verify custom value if expected
      if (scenario.judgement.customDataPoint) {
        verifyCustomValueInUI(dataPointId, scenario.judgement.customDataPoint);
        cy.log(`✓ Custom value verified for ${dataPointId}`);
      }

      cy.log(`✓ Data point ${dataPointId} verification passed`);
    });

    // Verify data points without QA reports are displayed as accepted original
    Object.entries(overview.dataPointsWithoutQaReports).forEach(([dataPointType, dataPointId]) => {
      cy.log(`Verifying data point without QA: ${dataPointId} (type: ${dataPointType})`);
      verifyAcceptedSourceInUI(dataPointId, AcceptedDataPointSource.Original);
      cy.log(`✓ Data point ${dataPointId} (no QA) verification passed`);
    });

    cy.log('✓ All judgement data verified successfully in the company framework page');
  });
}

/**
 * Helper function to verify that the accepted source is correctly displayed in the UI for a data point.
 *
 * @param dataPointId    The ID of the data point to verify.
 * @param acceptedSource The expected accepted source (Original, Custom, or Qa).
 */
function verifyAcceptedSourceInUI(dataPointId: string, acceptedSource: AcceptedDataPointSource | undefined): void {
  cy.log(`Verifying acceptedSource for ${dataPointId} should be ${String(acceptedSource)}`);

  // Check that the data point row exists in the table
  cy.get(`[data-test="data-point-row-${dataPointId}"]`).should('be.visible');

  if (acceptedSource === AcceptedDataPointSource.Original) {
    cy.log(`  - Checking Original column for ${dataPointId}`);
    cy.get(`[data-test="data-point-row-${dataPointId}"]`).within(() => {
      cy.get('td').eq(1).should('contain.text', 'Original').or('have.class', 'accepted-check');
    });
  } else if (acceptedSource === AcceptedDataPointSource.Custom) {
    cy.log(`  - Checking Custom column for ${dataPointId}`);
    cy.get(`[data-test="data-point-row-${dataPointId}"]`).within(() => {
      cy.get('td').eq(4).should('contain.text', 'Custom').or('have.class', 'accepted-check');
    });
  } else if (acceptedSource === AcceptedDataPointSource.Qa) {
    cy.log(`  - Checking QA column for ${dataPointId}`);
    cy.get(`[data-test="data-point-row-${dataPointId}"]`).within(() => {
      cy.get('td').eq(2).or('td').eq(3).should('have.class', 'accepted-check');
    });
  }
}

/**
 * Helper function to verify that a custom value is correctly displayed in the UI for a data point.
 *
 * @param dataPointId  The ID of the data point to verify.
 * @param customValue  The expected custom value.
 */
function verifyCustomValueInUI(dataPointId: string, customValue: string): void {
  cy.log(`Verifying custom value for ${dataPointId} should be ${customValue}`);

  // Check that the custom value is visible somewhere in the data point row or details
  cy.get(`[data-test="data-point-row-${dataPointId}"]`).within(() => {
    cy.contains(customValue).should('be.visible');
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
