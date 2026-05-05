import JudgeDialog from '@/components/resources/datasetReview/JudgeDialog.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { AcceptedDataPointSource, DatasetJudgementState, QaReportDataPointVerdict } from '@clients/qaservice';
import type { DatasetJudgementResponse, DataPointJudgement } from '@clients/qaservice';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { computed } from 'vue';
import type Keycloak from 'keycloak-js';
import type { CellRow } from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import { MLDTDisplayObjectForEmptyString } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { KEYCLOAK_ROLE_JUDGE } from '@/utils/KeycloakRoles.ts';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';

// ===== Shared test data =====

const datasetJudgementId = 'test-judgement-id';
const dataPointTypeId = 'kpiAlpha';
const dataPointId = 'dp-id-alpha';
const secondDataPointTypeId = 'kpiBeta';
const secondDataPointId = 'dp-id-beta';

const reporterUserId1 = 'reporter-user-id-1';
const reporterUserId2 = 'reporter-user-id-2';
const overflowingCommentEntry =
  'original-comment that-is-so-long-it-overflows-the-maximum-field-with-so-let-me-tell-you-why-this-is-awesome-because-we-can-then-test-the-overflow-behavior';
const correctedCommentEntry =
  'corrected-comment that-is-so-long-it-overflows-the-maximum-field-with-so-let-me-tell-you-why-this-is-awesome-because-we-can-then-test-the-overflow-behavior';
const overflowingValueEntry =
  'original-value-that-is-so-long-it-overflows-the-maximum-field-with-so-let-me-tell-you-why-this-is-awesome-because-we-can-then-test-the-overflow-behavior';
const overflowingQualityEntry =
  'original-quality-that-is-so-long-it-overflows-the-maximum-field-with-so-let-me-tell-you-why-this-is-awesome-because-we-can-then-test-the-overflow-behavior';
const overflowingDataSourceEntry = {
  fileName:
    'original-doc-that-is-so-long-it-overflows-the-maximum-field-with-so-let-me-tell-you-why-this-is-awesome-because-we-can-then-test-the-overflow-behavior.pdf',
  page: '3',
};

const originalDataPoint = {
  value: 'original-value',
  quality: 'Audited',
  comment: overflowingCommentEntry,
  dataSource: { fileName: 'original-doc.pdf', page: '3' },
};

const correctedDataPoint = {
  value: 'corrected-value',
  quality: 'Estimated',
  comment: correctedCommentEntry,
  dataSource: { fileName: 'corrected-doc.pdf', page: '7' },
};

const overflowingOriginalDataPoint = {
  value: overflowingValueEntry,
  quality: overflowingQualityEntry,
  comment: overflowingCommentEntry,
  dataSource: overflowingDataSourceEntry,
};

const baseDatasetJudgement: DatasetJudgementResponse = {
  dataSetJudgementId: datasetJudgementId,
  datasetId: 'dataset-id',
  companyId: 'company-id',
  reportingPeriod: '2023',
  dataType: 'sfdr' as DatasetJudgementResponse['dataType'],
  judgementState: DatasetJudgementState.Pending,
  qaReporters: [
    {
      reporterUserId: reporterUserId1,
      reporterUserName: 'Reporter One',
      reporterEmailAddress: 'reporter1@example.com',
    },
    {
      reporterUserId: reporterUserId2,
      reporterUserName: 'Reporter Two',
      reporterEmailAddress: 'reporter2@example.com',
    },
  ],
  dataPoints: {
    [dataPointTypeId]: {
      dataPointType: dataPointTypeId,
      dataPointId: dataPointId,
      acceptedSource: undefined,
      qaReports: [
        {
          qaReportId: 'qa-report-1',
          verdict: QaReportDataPointVerdict.QaRejected,
          correctedData: JSON.stringify(correctedDataPoint),
          reporterUserId: reporterUserId1,
          uploadTime: 1000,
          active: true,
          dataPointId: dataPointId,
          dataPointType: dataPointTypeId,
          comment: 'qa-comment',
        },
      ],
    },
    [secondDataPointTypeId]: {
      dataPointType: secondDataPointTypeId,
      dataPointId: secondDataPointId,
      acceptedSource: undefined,
      qaReports: [],
    },
  },
};

const kpiRows: CellRow[] = [
  {
    type: 'cell',
    label: 'KPI Alpha Label',
    dataPointTypeId: dataPointTypeId,
    originalDisplay: MLDTDisplayObjectForEmptyString,
  },
  {
    type: 'cell',
    label: 'KPI Beta Label',
    dataPointTypeId: secondDataPointTypeId,
    originalDisplay: MLDTDisplayObjectForEmptyString,
  },
];

const overflowTestCases = [
  {
    description: 'shows and hides the overflow popover for original comment',
    sectionDataTest: 'original-datapoint-section',
    iconDataTest: 'comment-overflow-icon',
    expectedPopoverText: overflowingCommentEntry,
  },
  {
    description: 'shows and hides the overflow popover for original value',
    sectionDataTest: 'original-datapoint-section',
    iconDataTest: 'value-overflow-icon',
    expectedPopoverText: overflowingValueEntry,
  },
  {
    description: 'shows and hides the overflow popover for original document',
    sectionDataTest: 'original-datapoint-section',
    iconDataTest: 'document-overflow-icon',
    expectedPopoverText: overflowingDataSourceEntry.fileName,
  },
  {
    description: 'shows and hides the overflow popover for original quality',
    sectionDataTest: 'original-datapoint-section',
    iconDataTest: 'quality-overflow-icon',
    expectedPopoverText: overflowingQualityEntry,
  },
] as const;

// ===== Mount helper =====

/**
 * Mounts the JudgeDialog component with standard intercepts and Vue Query.
 */
function mountJudgeDialog(options?: {
  datasetJudgement?: DatasetJudgementResponse;
  datasetJudgementStatusCode?: number;
  originalDataPointBody?: object;
  originalDataPointStatusCode?: number;
  patchStatusCode?: number;
  patchErrorResponse?: object;
  dataPointTypeId?: string;
  kpiRows?: CellRow[];
  companyDocuments?: DocumentMetaInfoResponse[];
}): void {
  const judgement = options?.datasetJudgement ?? baseDatasetJudgement;

  cy.intercept('GET', `**/qa/dataset-judgements/${datasetJudgementId}`, {
    statusCode: options?.datasetJudgementStatusCode ?? 200,
    body: judgement,
  }).as('getDatasetJudgement');

  cy.intercept('GET', `**/api/data-points/${dataPointId}`, {
    statusCode: options?.originalDataPointStatusCode ?? 200,
    body: {
      dataPointId: dataPointId,
      dataPoint: JSON.stringify(options?.originalDataPointBody ?? originalDataPoint),
    },
  }).as('getOriginalDataPoint');

  cy.intercept('GET', `**/api/data-points/${secondDataPointId}`, {
    statusCode: 200,
    body: {
      dataPointId: secondDataPointId,
      dataPoint: JSON.stringify({ value: 'beta-value', quality: 'Estimated' }),
    },
  }).as('getSecondDataPoint');

  cy.intercept('PATCH', `**/qa/dataset-judgements/${datasetJudgementId}/data-points/**`, (req) => {
    if (options?.patchErrorResponse) {
      req.reply({
        statusCode: options?.patchStatusCode ?? 400,
        body: options?.patchErrorResponse,
      });
    } else {
      req.reply({
        statusCode: options?.patchStatusCode ?? 200,
        body: judgement,
      });
    }
  }).as('patchJudgementDetail');

  cy.intercept('GET', `**/?companyId=${judgement.companyId}`, {
    statusCode: 200,
    body: options?.companyDocuments ?? [],
  }).as('getCompanyDocuments');

  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  const mount = getMountingFunction();
  const keycloakMock = minimalKeycloakMock({ roles: [KEYCLOAK_ROLE_JUDGE] });
  const keycloakPromise = Promise.resolve(keycloakMock as unknown as Keycloak);
  const apiClientProvider = new ApiClientProvider(keycloakPromise);

  mount(JudgeDialog, {
    props: {
      datasetReviewId: datasetJudgementId,
      dataPointTypeId: options?.dataPointTypeId ?? dataPointTypeId,
      kpiRows: options?.kpiRows ?? kpiRows,
      isOpen: true,
    },
    global: {
      plugins: [[VueQueryPlugin, { queryClient }]],
      provide: {
        getKeycloakPromise: () => keycloakPromise,
        authenticated: computed(() => true),
        apiClientProvider: computed(() => apiClientProvider),
      },
    },
  });

  cy.wait('@getDatasetJudgement');
}

/**
 * Tests the overflow popover behavior for a specific field.
 * Mounts the dialog, triggers the overflow icon, verifies the popover content, and tests dismissal.
 *
 * @param {Object} params - Configuration for the test.
 * @param {string} params.description - Test description.
 * @param {string} params.sectionDataTest - Data test identifier for the section.
 * @param {string} params.iconDataTest - Data test identifier for the overflow icon.
 * @param {string} params.expectedPopoverText - Expected text in the popover.
 * @param {object} [params.originalDataPointBody] - Optional original data point override.
 */
function checkOverflowBehavior(params: {
  description: string;
  sectionDataTest: string;
  iconDataTest: string;
  expectedPopoverText: string;
  originalDataPointBody?: object;
}): void {
  const { description, sectionDataTest, iconDataTest, expectedPopoverText, originalDataPointBody } = params;

  it(description, () => {
    mountJudgeDialog({
      originalDataPointBody: originalDataPointBody ?? overflowingOriginalDataPoint,
    });

    cy.get(`[data-test="${sectionDataTest}"]`).within(() => {
      cy.get(`[data-test="${iconDataTest}"]`).should('be.visible').trigger('mouseenter');
    });
    cy.get('[data-test="overflow-popover"]').should('be.visible').and('contain.text', expectedPopoverText);
    cy.get(`[data-test="${sectionDataTest}"] [data-test="${iconDataTest}"]`).trigger('mouseleave');
    cy.get('[data-test="overflow-popover"]').should('not.exist');
  });
}

// ===== Tests =====

describe('JudgeDialog component tests', () => {
  // ---------------------------------------------------------------------------
  // 1. Display: original data point, corrected data points and KPI display name
  // ---------------------------------------------------------------------------
  describe('Data display', () => {
    it('shows the KPI display name in the dialog header', () => {
      mountJudgeDialog();

      cy.get('[data-test="judge-modal"]').should('be.visible');
      cy.contains('KPI Alpha Label').should('be.visible');
    });

    it('displays original data point fields after loading', () => {
      mountJudgeDialog();
      cy.wait('@getOriginalDataPoint');

      cy.get('[data-test="original-datapoint-section"]').within(() => {
        cy.contains('original-value').should('be.visible');
        cy.contains('Audited').should('be.visible');
        cy.contains('original-doc.pdf').should('be.visible');
        cy.contains('tr', 'Page(s)').should('contain.text', '3');
        cy.contains(overflowingCommentEntry).should('be.visible');
      });
    });

    it('displays corrected data point fields from the QA report', () => {
      mountJudgeDialog();

      cy.get('[data-test="corrected-datapoint-section"]').within(() => {
        cy.contains('corrected-value').should('be.visible');
        cy.contains('Estimated').should('be.visible');
        cy.contains('corrected-doc.pdf').should('be.visible');
        cy.contains('tr', 'Page(s)').should('contain.text', '7');
        cy.contains(correctedCommentEntry).should('be.visible');
      });
    });

    it('shows the QA reporter name in the corrected datapoint navigation', () => {
      mountJudgeDialog();

      cy.get('[data-test="corrected-datapoint-section"]').contains('Reporter One').should('be.visible');
    });

    it('shows "No QA reports available." when there are no QA reports', () => {
      const judgementWithNoReports: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            qaReports: [],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementWithNoReports });

      cy.get('[data-test="corrected-datapoint-section"]').within(() => {
        cy.get('[data-test="empty-text"]').should('be.visible').and('contain.text', 'No QA reports available.');
        cy.get('table').should('not.exist');
      });
    });
  });

  // ---------------------------------------------------------------------------
  // 2. QA verdict badge
  // ---------------------------------------------------------------------------
  describe('QA verdict badge', () => {
    it('shows "QA NOT ATTEMPTED" badge when there are no QA reports', () => {
      const judgement: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            qaReports: [],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgement });

      cy.get('[data-test="verdict-badge"]').should('contain.text', 'QA NOT ATTEMPTED');
    });

    it('shows "QA ACCEPTED" badge when all reports are accepted', () => {
      const judgement: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            qaReports: [
              {
                qaReportId: 'qa-report-1',
                verdict: QaReportDataPointVerdict.QaAccepted,
                correctedData: '',
                reporterUserId: reporterUserId1,
                uploadTime: 1000,
                active: true,
                dataPointId: dataPointId,
                dataPointType: dataPointTypeId,
                comment: '',
              },
            ],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgement });

      cy.get('[data-test="verdict-badge"]').should('contain.text', 'QA ACCEPTED');
    });

    it('shows "QA REJECTED" badge when at least one report is rejected', () => {
      mountJudgeDialog();

      cy.get('[data-test="verdict-badge"]').should('contain.text', 'QA REJECTED');
    });

    it('shows "MIXED VERDICTS" badge when no report is rejected but not all are accepted', () => {
      const judgement: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            qaReports: [
              {
                qaReportId: 'qa-report-inconclusive',
                verdict: QaReportDataPointVerdict.QaInconclusive,
                correctedData: '',
                reporterUserId: reporterUserId1,
                uploadTime: 1000,
                active: true,
                dataPointId: dataPointId,
                dataPointType: dataPointTypeId,
                comment: '',
              },
            ],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgement });

      cy.get('[data-test="verdict-badge"]').should('contain.text', 'MIXED VERDICTS');
    });
  });

  // ---------------------------------------------------------------------------
  // 3. Accept buttons call the correct endpoints with the correct input
  // ---------------------------------------------------------------------------
  describe('Accept buttons and API calls', () => {
    it('calls PATCH with AcceptedDataPointSource.Original when accepting the original datapoint', () => {
      mountJudgeDialog();
      cy.wait('@getOriginalDataPoint');

      cy.get('[data-test="accept-original-button"]').click();

      cy.wait('@patchJudgementDetail').then((interception) => {
        expect(interception.request.url).to.contain(
          `/qa/dataset-judgements/${datasetJudgementId}/data-points/${dataPointTypeId}`
        );
        expect(interception.request.body).to.deep.include({
          acceptedSource: AcceptedDataPointSource.Original,
        });
        expect(interception.request.body.reporterUserIdOfAcceptedQaReport).to.be.undefined;
        expect(interception.request.body.customDataPoint).to.be.undefined;
      });
    });

    it('calls PATCH with AcceptedDataPointSource.Qa and reporter userId when accepting the QA report', () => {
      mountJudgeDialog();

      cy.get('[data-test="accept-report-button"]').click();

      cy.wait('@patchJudgementDetail').then((interception) => {
        expect(interception.request.url).to.contain(
          `/qa/dataset-judgements/${datasetJudgementId}/data-points/${dataPointTypeId}`
        );
        expect(interception.request.body).to.deep.include({
          acceptedSource: AcceptedDataPointSource.Qa,
          reporterUserIdOfAcceptedQaReport: reporterUserId1,
        });
        expect(interception.request.body.customDataPoint).to.be.undefined;
      });
    });

    it('calls PATCH with AcceptedDataPointSource.Custom and form data JSON when accepting a custom datapoint', () => {
      mountJudgeDialog();

      cy.get('[data-test="custom-value-field"]').clear().type('my-custom-value');

      cy.get('[data-test="accept-custom-button"]').click();

      cy.wait('@patchJudgementDetail').then((interception) => {
        expect(interception.request.url).to.contain(
          `/qa/dataset-judgements/${datasetJudgementId}/data-points/${dataPointTypeId}`
        );
        expect(interception.request.body.acceptedSource).to.eq(AcceptedDataPointSource.Custom);
        expect(interception.request.body.customDataPoint).to.be.a('string');
        const parsed = JSON.parse(interception.request.body.customDataPoint);
        expect(parsed.value).to.eq('my-custom-value');
      });
    });

    it('advances the header to the next KPI after a successful accept', () => {
      mountJudgeDialog();

      cy.contains('KPI Alpha Label').should('be.visible');
      cy.get('[data-test="accept-original-button"]').click();
      cy.wait('@patchJudgementDetail');

      cy.get('[data-test="dialog-title"]').should('contain.text', 'KPI Beta Label');
      cy.get('[data-test="dialog-title"]').should('not.contain.text', 'KPI Alpha Label');
    });

    it('calls PATCH with AcceptedDataPointSource.Custom and JSON content when accepting from JSON mode', () => {
      mountJudgeDialog();

      cy.get('[data-test="edit-mode-toggle"]').click();

      const customJson = JSON.stringify({ value: 'json-mode-value', quality: 'Audited' }, null, 2);
      cy.get('[data-test="custom-json-textarea"]').clear().type(customJson, { parseSpecialCharSequences: false });

      cy.get('[data-test="accept-custom-button"]').click();

      cy.wait('@patchJudgementDetail').then((interception) => {
        expect(interception.request.body.acceptedSource).to.eq(AcceptedDataPointSource.Custom);
        const parsed = JSON.parse(interception.request.body.customDataPoint);
        expect(parsed.value).to.eq('json-mode-value');
        expect(parsed.quality).to.eq('Audited');
      });
    });

    it('disables the accept-report button when there are no QA reports', () => {
      const judgement: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            qaReports: [],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgement });

      cy.get('[data-test="accept-report-button"]').should('be.disabled');
    });

    it('shows popup confirmation when there are no more unreviewed datapoints', () => {
      const judgementAllReviewed: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            acceptedSource: AcceptedDataPointSource.Original,
          },
          [secondDataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[secondDataPointTypeId],
            acceptedSource: AcceptedDataPointSource.Original,
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementAllReviewed });
      cy.get('[data-test="accept-original-button"]').should('be.visible');
      cy.get('[data-test="accept-original-button"]').click();
      cy.get('[data-test="confirmation-modal"]')
        .should('be.visible')
        .should('contain.text', 'All data points reviewed');
      cy.get('[data-test="judge-modal"]').should('exist');
    });
  });

  // ---------------------------------------------------------------------------
  // 4. Custom data point: form ↔ JSON switching
  // ---------------------------------------------------------------------------
  describe('Custom data point form/JSON switching', () => {
    it('starts in form mode and shows form fields', () => {
      mountJudgeDialog();

      cy.get('[data-test="custom-datapoint-section"]').within(() => {
        cy.get('[data-test="custom-value-field"]').should('be.visible');
        cy.get('[data-test="custom-json-textarea"]').should('not.exist');
      });
    });

    it('switches to JSON mode when the toggle is enabled', () => {
      mountJudgeDialog();

      cy.get('[data-test="edit-mode-toggle"]').click();

      cy.get('[data-test="custom-json-textarea"]').should('be.visible');
      cy.get('[data-test="custom-value-field"]').should('not.exist');
    });

    it('preserves form data as JSON when switching to JSON mode', () => {
      mountJudgeDialog();

      cy.get('[data-test="custom-value-field"]').clear().type('form-entered-value');
      cy.get('[data-test="edit-mode-toggle"]').click();

      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'form-entered-value');
    });

    it('converts JSON back to form fields when switching back to form mode', () => {
      mountJudgeDialog();

      cy.get('[data-test="edit-mode-toggle"]').click();

      const customJson = JSON.stringify({ value: 'from-json-value', quality: 'Audited' });
      cy.get('[data-test="custom-json-textarea"]').clear().type(customJson, { parseSpecialCharSequences: false });

      cy.get('[data-test="edit-mode-toggle"]').click();

      cy.get('[data-test="custom-value-field"]').should('have.value', 'from-json-value');
    });

    it('shows a validation error when the JSON textarea contains invalid JSON', () => {
      mountJudgeDialog();

      cy.get('[data-test="edit-mode-toggle"]').click();

      cy.get('[data-test="custom-json-textarea"]')
        .clear()
        .type('{ this is not valid json }', { parseSpecialCharSequences: false });

      cy.get('[data-test="custom-datapoint-section"]').contains('Custom JSON must be valid JSON.').should('be.visible');
      cy.get('[data-test="accept-custom-button"]').should('be.disabled');
    });

    it('disables the accept-custom button when the form is completely empty', () => {
      mountJudgeDialog();

      cy.get('[data-test="accept-custom-button"]').should('be.disabled');
    });
  });

  // ---------------------------------------------------------------------------
  // 5. Copy original / corrected into the custom field
  // ---------------------------------------------------------------------------
  describe('Copy buttons', () => {
    it('copies the original data point into the custom form fields', () => {
      mountJudgeDialog();
      cy.wait('@getOriginalDataPoint');

      cy.get('[data-test="copy-original-to-custom"]').click();

      cy.get('[data-test="custom-value-field"]').should('have.value', 'original-value');
      cy.get('[data-test="custom-pages-field"]').should('have.value', '3');
      cy.get('[data-test="custom-comment-field"]').should('have.value', overflowingCommentEntry);
      cy.get('[data-test="custom-quality-field"]').should('contain', 'Audited');
      cy.get('[data-test="custom-document-field"]').should('contain', 'Select Document');
    });

    it('copies the corrected data point into the custom form fields', () => {
      mountJudgeDialog();

      cy.get('[data-test="copy-corrected-to-custom"]').click();

      cy.get('[data-test="custom-value-field"]').should('have.value', 'corrected-value');
      cy.get('[data-test="custom-pages-field"]').should('have.value', '7');
      cy.get('[data-test="custom-comment-field"]').should('have.value', correctedCommentEntry);
      cy.get('[data-test="custom-quality-field"]').should('contain', 'Estimated');
      cy.get('[data-test="custom-document-field"]').should('contain', 'Select Document');
    });

    it('copies the original data point into the JSON textarea when in JSON mode', () => {
      mountJudgeDialog();
      cy.wait('@getOriginalDataPoint');

      cy.get('[data-test="edit-mode-toggle"]').click();
      cy.get('[data-test="copy-original-to-custom"]').click();

      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'original-value');
      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'Audited');
      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'original-doc.pdf');
      cy.get('[data-test="custom-json-textarea"]').should('contain.value', '3');
      cy.get('[data-test="custom-json-textarea"]').should('contain.value', overflowingCommentEntry);
    });

    it('copies the corrected data point into the JSON textarea when in JSON mode', () => {
      mountJudgeDialog();

      cy.get('[data-test="edit-mode-toggle"]').click();
      cy.get('[data-test="copy-corrected-to-custom"]').click();

      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'corrected-value');
      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'Estimated');
      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'corrected-doc.pdf');
      cy.get('[data-test="custom-json-textarea"]').should('contain.value', '7');
      cy.get('[data-test="custom-json-textarea"]').should('contain.value', correctedCommentEntry);
    });

    it('disables the copy-original button when there is no original data point loaded yet', () => {
      const judgement: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            dataPointId: '' as DataPointJudgement['dataPointId'],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgement });

      cy.get('[data-test="copy-original-to-custom"]').should('be.disabled');
    });

    it('disables the copy-corrected button when there is no corrected data point', () => {
      const judgement: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            qaReports: [],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgement });

      cy.get('[data-test="copy-corrected-to-custom"]').should('be.disabled');
    });

    // ---------------------------------------------------------------------------
    // 6. Previously accepted custom value is pre-populated on load
    // ---------------------------------------------------------------------------
    describe('Pre-population from previously accepted custom value', () => {
      it('pre-populates the custom form with a previously accepted custom value', () => {
        const previousCustomValue = {
          value: 'previously-accepted-value',
          quality: 'Estimated',
          comment: 'previously-accepted-comment',
          dataSource: { fileName: 'previous-doc.pdf', page: '12' },
        };

        const judgementWithPreviousCustom: DatasetJudgementResponse = {
          ...baseDatasetJudgement,
          dataPoints: {
            ...baseDatasetJudgement.dataPoints,
            [dataPointTypeId]: {
              ...baseDatasetJudgement.dataPoints[dataPointTypeId],
              acceptedSource: AcceptedDataPointSource.Custom,
              customValue: JSON.stringify(previousCustomValue),
            },
          },
        };
        mountJudgeDialog({ datasetJudgement: judgementWithPreviousCustom });

        cy.get('[data-test="custom-value-field"]').should('have.value', 'previously-accepted-value');
        cy.get('[data-test="custom-pages-field"]').should('have.value', '12');
        cy.get('[data-test="custom-comment-field"]').should('have.value', 'previously-accepted-comment');
        cy.get('[data-test="custom-quality-field"]').should('contain', 'Estimated');
        cy.get('[data-test="custom-document-field"]').should('contain', 'Select Document');
      });

      it('pre-populates the custom JSON textarea in JSON mode with a previously accepted custom value', () => {
        const previousCustomValue = {
          value: 'json-pre-populated-value',
          quality: 'Audited',
          comment: 'previously-accepted-comment',
          dataSource: { page: '12' },
        };

        const judgementWithPreviousCustom: DatasetJudgementResponse = {
          ...baseDatasetJudgement,
          dataPoints: {
            ...baseDatasetJudgement.dataPoints,
            [dataPointTypeId]: {
              ...baseDatasetJudgement.dataPoints[dataPointTypeId],
              acceptedSource: AcceptedDataPointSource.Custom,
              customValue: JSON.stringify(previousCustomValue),
            },
          },
        };
        mountJudgeDialog({ datasetJudgement: judgementWithPreviousCustom });

        cy.get('[data-test="edit-mode-toggle"]').click();
        cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'json-pre-populated-value');
        cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'Audited');
        cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'previously-accepted-comment');
        cy.get('[data-test="custom-json-textarea"]').should('contain.value', '12');
      });

      it('resets the custom form to defaults when navigating to a KPI without a previously accepted custom value', () => {
        const previousCustomValue = { value: 'should-not-appear', quality: 'Audited' };

        const judgementWithMixedKpis: DatasetJudgementResponse = {
          ...baseDatasetJudgement,
          dataPoints: {
            [dataPointTypeId]: {
              ...baseDatasetJudgement.dataPoints[dataPointTypeId],
              acceptedSource: AcceptedDataPointSource.Custom,
              customValue: JSON.stringify(previousCustomValue),
            },
            [secondDataPointTypeId]: {
              dataPointType: secondDataPointTypeId,
              dataPointId: secondDataPointId,
              acceptedSource: undefined,
              qaReports: [],
            },
          },
        };
        mountJudgeDialog({ datasetJudgement: judgementWithMixedKpis });

        cy.get('[data-test="custom-value-field"]').should('have.value', 'should-not-appear');

        cy.get('[data-test="next-datapoint-select"]').click();
        cy.get('.p-select-overlay').should('be.visible');
        cy.contains('KPI Beta Label').click();
        cy.get('[data-test="go-to-datapoint-button"]').click();

        cy.get('[data-test="custom-value-field"]').should('have.value', '');
        cy.get('[data-test="custom-quality-field"]').should('have.value', '');
        cy.get('[data-test="custom-document-field"]').should('have.value', '');
        cy.get('[data-test="custom-pages-field"]').should('have.value', '');
        cy.get('[data-test="custom-comment-field"]').should('have.value', '');
      });
    });

    // ---------------------------------------------------------------------------
    // 7. Custom data point field: access to correct documents
    // ---------------------------------------------------------------------------
    describe('Custom data point document access', () => {
      it('shows the available documents in the document select dropdown', () => {
        const docs: DocumentMetaInfoResponse[] = [
          {
            documentId: 'ref-123',
            documentName: 'Annual Report 2023',
            reportingPeriod: '2023',
            publicationDate: '2023-01-01',
            uploaderId: 'u1',
          },
          {
            documentId: 'ref-456',
            documentName: 'Sustainability Report',
            reportingPeriod: '2023',
            publicationDate: '2023-01-01',
            uploaderId: 'u1',
          },
          { documentId: 'ref-789', documentName: 'Monthly Report', publicationDate: '2023-01-01', uploaderId: 'u1' },
        ];
        mountJudgeDialog({ companyDocuments: docs });
        cy.wait('@getCompanyDocuments');

        cy.get('[data-test="custom-document-field"]').click();
        cy.get('.p-select-overlay').should('be.visible');

        cy.contains('Annual Report 2023').should('be.visible');
        cy.contains('Sustainability Report').should('be.visible');
        cy.contains('Monthly Report').should('be.visible');
      });

      it('uses the selected document dataSource when building the custom datapoint JSON', () => {
        const docs: DocumentMetaInfoResponse[] = [
          {
            documentId: 'ref-123',
            documentName: 'Annual Report 2023',
            reportingPeriod: '2023',
            publicationDate: '2023-01-01',
            uploaderId: 'u1',
          },
        ];
        mountJudgeDialog({ companyDocuments: docs });
        cy.wait('@getCompanyDocuments');

        cy.get('[data-test="custom-value-field"]').clear().type('doc-test-value');
        cy.get('[data-test="custom-document-field"]').click();
        cy.get('.p-select-overlay').should('be.visible');
        cy.contains('Annual Report 2023').click();

        cy.get('[data-test="accept-custom-button"]').click();

        cy.wait('@patchJudgementDetail').then((interception) => {
          const parsed = JSON.parse(interception.request.body.customDataPoint);
          expect(parsed.dataSource.fileName).to.eq('Annual Report 2023');
        });
      });

      it('shows no documents in the dropdown when the API returns none', () => {
        mountJudgeDialog({ companyDocuments: [] });

        cy.get('[data-test="custom-document-field"]').click();
        cy.get('.p-select-overlay').should('be.visible');

        cy.contains('Annual Report 2023').should('not.exist');
        cy.contains('Sustainability Report').should('not.exist');
      });

      it('shows company documents in the dropdown', () => {
        const companyOnlyDoc: DocumentMetaInfoResponse = {
          documentId: 'api-ref-001',
          documentName: 'Company Policy 2023',
          reportingPeriod: '2023',
          publicationDate: '2023-06-01',
          uploaderId: 'uploader-1',
        };
        mountJudgeDialog({ companyDocuments: [companyOnlyDoc] });
        cy.wait('@getCompanyDocuments');

        cy.get('[data-test="custom-document-field"]').click();
        cy.get('.p-select-overlay').should('be.visible');

        cy.contains('Company Policy 2023').should('be.visible');
      });

      it('filters out company documents with a reporting period earlier than the dataset', () => {
        const oldDoc: DocumentMetaInfoResponse = {
          documentId: 'api-ref-old',
          documentName: 'Old Report 2022',
          reportingPeriod: '2022',
          publicationDate: '2022-12-31',
          uploaderId: 'uploader-1',
        };
        mountJudgeDialog({ companyDocuments: [oldDoc] });
        cy.wait('@getCompanyDocuments');

        cy.get('[data-test="custom-document-field"]').click();
        cy.get('.p-select-overlay').should('be.visible');

        cy.contains('Old Report 2022').should('not.exist');
      });

      it('includes company documents with no reporting period regardless of dataset period', () => {
        const noperiodDoc: DocumentMetaInfoResponse = {
          documentId: 'api-ref-nop',
          documentName: 'Timeless Policy',
          publicationDate: '2020-01-01',
          uploaderId: 'uploader-1',
        };
        mountJudgeDialog({ companyDocuments: [noperiodDoc] });
        cy.wait('@getCompanyDocuments');

        cy.get('[data-test="custom-document-field"]').click();
        cy.get('.p-select-overlay').should('be.visible');

        cy.contains('Timeless Policy').should('be.visible');
      });

      it('pre-selects the correct document when only fileReference is present', () => {
        const previousCustomValue = {
          value: 'previously-accepted-value',
          quality: 'Estimated',
          comment: 'previously-accepted-comment',
          dataSource: { fileName: null, fileReference: 'ref-789', page: '12' },
        };

        const judgementWithPreviousCustom: DatasetJudgementResponse = {
          ...baseDatasetJudgement,
          dataPoints: {
            ...baseDatasetJudgement.dataPoints,
            [dataPointTypeId]: {
              ...baseDatasetJudgement.dataPoints[dataPointTypeId],
              acceptedSource: AcceptedDataPointSource.Custom,
              customValue: JSON.stringify(previousCustomValue),
            },
          },
        };

        const docsWithReferenceOnly: DocumentMetaInfoResponse[] = [
          {
            documentId: 'ref-789',
            documentName: 'Monthly Report',
            publicationDate: '2023-01-01',
            uploaderId: 'u1',
          },
        ];

        mountJudgeDialog({ datasetJudgement: judgementWithPreviousCustom, companyDocuments: docsWithReferenceOnly });
        cy.wait('@getCompanyDocuments');
        cy.get('[data-test="custom-document-field"]').should('contain', 'Monthly Report');
      });
    });
  });

  // ---------------------------------------------------------------------------
  // 8. Error message when PATCH fails
  // ---------------------------------------------------------------------------
  describe('PATCH error handling', () => {
    it('does not show an error message on initial load without any action', () => {
      mountJudgeDialog();

      cy.get('[data-test="confirmation-modal"]').should('not.exist');
      cy.get('[data-test="confirmation-modal-error-message"]').should('not.exist');
    });

    it('shows a popup modal when the PATCH request fails and close using confirm', () => {
      mountJudgeDialog({ patchStatusCode: 500 });
      cy.get('[data-test="accept-original-button"]').click();
      cy.get('[data-test="confirmation-modal-error-message"]').should('contain', '500');
      cy.get('[data-test="ok-confirmation-modal-button"]').click();
      cy.get('[data-test="confirmation-modal"]').should('not.exist');
    });

    it('shows backend validation details when the PATCH request fails with invalid-input', () => {
      const patchErrorResponse = {
        errors: [
          {
            errorType: 'invalid-input',
            summary: 'Custom datapoint not valid.',
            message:
              'Custom datapoint given does not match the specification of extendedDecimalScope1GhgEmissionsInTonnes.',
            httpStatus: 400,
          },
        ],
      };

      mountJudgeDialog({ patchStatusCode: 400, patchErrorResponse });

      cy.get('[data-test="custom-value-field"]').clear().type('invalid-custom-value');
      cy.get('[data-test="accept-custom-button"]').click();
      cy.get('[data-test="confirmation-modal"]').should('be.visible');

      cy.get('[data-test="confirmation-modal-error-message"]')
        .should('contain.text', 'Custom datapoint not valid.')
        .and(
          'contain.text',
          'Custom datapoint given does not match the specification of extendedDecimalScope1GhgEmissionsInTonnes.'
        )
        .and('contain.text', 'invalid-input')
        .and('contain.text', 'HTTP 400');

      cy.get('[data-test="ok-confirmation-modal-button"]').click();
      cy.get('[data-test="confirmation-modal"]').should('not.exist');
    });
  });

  // ---------------------------------------------------------------------------
  // 9. Multi-report navigation and QaAccepted filtering
  // ---------------------------------------------------------------------------
  describe('Corrected datapoint QA report navigation', () => {
    const secondCorrectedDataPoint = {
      value: 'value of second corrected data point',
      quality: 'Audited',
      comment: 'second-corrected-comment',
      dataSource: { fileName: 'second-corrected-doc.pdf', page: '15' },
    };

    /**
     * A judgement where kpiAlpha has two non-accepted QA reports so the prev/next
     * navigation is exercised.
     */
    const judgementWithTwoReports: DatasetJudgementResponse = {
      ...baseDatasetJudgement,
      dataPoints: {
        ...baseDatasetJudgement.dataPoints,
        [dataPointTypeId]: {
          ...baseDatasetJudgement.dataPoints[dataPointTypeId],
          qaReports: [
            {
              qaReportId: 'qa-report-1',
              verdict: QaReportDataPointVerdict.QaRejected,
              correctedData: JSON.stringify(correctedDataPoint),
              reporterUserId: reporterUserId1,
              uploadTime: 1000,
              active: true,
              dataPointId: dataPointId,
              dataPointType: dataPointTypeId,
              comment: 'qa-comment-1',
            },
            {
              qaReportId: 'qa-report-2',
              verdict: QaReportDataPointVerdict.QaRejected,
              correctedData: JSON.stringify(secondCorrectedDataPoint),
              reporterUserId: reporterUserId2,
              uploadTime: 2000,
              active: true,
              dataPointId: dataPointId,
              dataPointType: dataPointTypeId,
              comment: 'qa-comment-2',
            },
          ],
        },
      },
    };

    it('shows the report count and first reporter when there are multiple QA reports', () => {
      mountJudgeDialog({ datasetJudgement: judgementWithTwoReports });

      cy.get('[data-test="corrected-datapoint-section"]').within(() => {
        cy.contains('1 / 2').should('be.visible');
        cy.contains('Reporter One').should('be.visible');
      });
    });

    it('advances to the next QA report and updates the corrected data when clicking next', () => {
      mountJudgeDialog({ datasetJudgement: judgementWithTwoReports });

      cy.get('[data-test="corrected-datapoint-section"]').within(() => {
        cy.get('[data-test="qa-next-button"]').click();
        cy.contains('2 / 2').should('be.visible');
        cy.contains('Reporter Two').should('be.visible');
        cy.contains('value of second corrected data point').should('be.visible');
      });
    });

    it('goes back to the previous QA report when clicking prev after next', () => {
      mountJudgeDialog({ datasetJudgement: judgementWithTwoReports });

      cy.get('[data-test="corrected-datapoint-section"]').within(() => {
        cy.get('[data-test="qa-next-button"]').click();
        cy.get('[data-test="qa-prev-button"]').click();
        cy.contains('1 / 2').should('be.visible');
        cy.contains('Reporter One').should('be.visible');
        cy.contains('corrected-value').should('be.visible');
      });
    });

    it('includes QaAccepted reports in the corrected section like every other report', () => {
      const judgementWithAcceptedAndRejected: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            qaReports: [
              {
                qaReportId: 'qa-report-accepted',
                verdict: QaReportDataPointVerdict.QaAccepted,
                correctedData: JSON.stringify(correctedDataPoint),
                reporterUserId: reporterUserId1,
                uploadTime: 1000,
                active: true,
                dataPointId: dataPointId,
                dataPointType: dataPointTypeId,
                comment: '',
              },
              {
                qaReportId: 'qa-report-rejected',
                verdict: QaReportDataPointVerdict.QaRejected,
                correctedData: JSON.stringify(secondCorrectedDataPoint),
                reporterUserId: reporterUserId2,
                uploadTime: 2000,
                active: true,
                dataPointId: dataPointId,
                dataPointType: dataPointTypeId,
                comment: '',
              },
            ],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementWithAcceptedAndRejected });

      cy.get('[data-test="corrected-datapoint-section"]').within(() => {
        cy.contains('1 / 2').should('be.visible');
        cy.contains('Reporter One').should('be.visible');
        cy.contains('corrected-value').should('be.visible');

        cy.get('[data-test="qa-next-button"]').click();
        cy.contains('2 / 2').should('be.visible');
        cy.contains('Reporter Two').should('be.visible');
        cy.contains('value of second corrected data point').should('be.visible');
      });
    });
  });

  // ---------------------------------------------------------------------------
  // 10. Accepted-source checkmark
  // ---------------------------------------------------------------------------
  describe('Accepted-source checkmark', () => {
    it('shows the accepted-check on the original section when acceptedSource is Original', () => {
      const judgementWithOriginalAccepted: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            acceptedSource: AcceptedDataPointSource.Original,
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementWithOriginalAccepted });

      cy.get('[data-test="original-datapoint-section"]').find('[data-test="accepted-check"]').should('be.visible');
      cy.get('[data-test="corrected-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
      cy.get('[data-test="custom-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
    });

    it('shows the accepted-check on the reviewed section when acceptedSource is Qa and the reporter matches', () => {
      const judgementWithQaAccepted: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            acceptedSource: AcceptedDataPointSource.Qa,
            reporterUserIdOfAcceptedQaReport: reporterUserId1,
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementWithQaAccepted });

      cy.get('[data-test="corrected-datapoint-section"]').find('[data-test="accepted-check"]').should('be.visible');
      cy.get('[data-test="original-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
      cy.get('[data-test="custom-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
    });

    it('shows message indicating which qa report has been selected on the reviewed section when acceptedSource is Qa for another report and the reporter does not match', () => {
      const judgementWithQaAccepted: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            acceptedSource: AcceptedDataPointSource.Qa,
            reporterUserIdOfAcceptedQaReport: reporterUserId2,
            qaReports: [
              {
                qaReportId: 'qa-report-1',
                verdict: QaReportDataPointVerdict.QaAccepted,
                correctedData: JSON.stringify(correctedDataPoint),
                reporterUserId: reporterUserId1,
                uploadTime: 1000,
                active: true,
                dataPointId: dataPointId,
                dataPointType: dataPointTypeId,
                comment: '',
              },
              {
                qaReportId: 'qa-report-2',
                verdict: QaReportDataPointVerdict.QaRejected,
                correctedData: JSON.stringify(correctedDataPoint),
                reporterUserId: reporterUserId2,
                uploadTime: 2000,
                active: true,
                dataPointId: dataPointId,
                dataPointType: dataPointTypeId,
                comment: '',
              },
            ],
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementWithQaAccepted });

      cy.get('[data-test="qa-accepted-info-text"]').should('be.visible');
      cy.get('[data-test="qa-accepted-info-text"]').should('contain.text', 'report 2/2 accepted');
      cy.get('[data-test="original-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
      cy.get('[data-test="corrected-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
      cy.get('[data-test="custom-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
    });

    it('shows the accepted-check on the custom section when acceptedSource is Custom', () => {
      const previousCustomValue = { value: 'accepted-custom-value', quality: 'Audited' };
      const judgementWithCustomAccepted: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            acceptedSource: AcceptedDataPointSource.Custom,
            customValue: JSON.stringify(previousCustomValue),
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementWithCustomAccepted });

      cy.get('[data-test="custom-datapoint-section"]').should('be.visible');
      cy.get('[data-test="original-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
      cy.get('[data-test="corrected-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
    });

    it('shows no accepted-check on any section when there is no accepted source', () => {
      mountJudgeDialog();

      cy.get('[data-test="original-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
      cy.get('[data-test="corrected-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
      cy.get('[data-test="custom-datapoint-section"]').find('[data-test="accepted-check"]').should('not.exist');
    });
  });

  // ---------------------------------------------------------------------------
  // 11. "Go To" advances the header KPI label
  // ---------------------------------------------------------------------------
  describe('Go To navigation', () => {
    it('updates the dialog title to the selected KPI after clicking Go To', () => {
      mountJudgeDialog();

      cy.get('[data-test="dialog-title"]').should('have.text', 'KPI Alpha Label');

      cy.get('[data-test="next-datapoint-select"]').click();
      cy.get('.p-select-overlay').should('be.visible');
      cy.contains('KPI Beta Label').click();
      cy.get('[data-test="go-to-datapoint-button"]').click();

      cy.get('[data-test="dialog-title"]').should('have.text', 'KPI Beta Label');
    });

    it('shows all KPIs in the dropdown when no KPIs have been reviewed yet', () => {
      mountJudgeDialog();

      cy.get('[data-test="next-datapoint-select"]').click();
      cy.get('.p-select-overlay').should('be.visible');

      cy.contains('KPI Alpha Label').should('be.visible');
      cy.contains('KPI Beta Label').should('be.visible');
    });

    it('hides reviewed KPIs from the dropdown when "only show unreviewed" toggle is on', () => {
      const judgementWithAlphaReviewed: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            acceptedSource: AcceptedDataPointSource.Original,
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementWithAlphaReviewed });

      cy.get('[data-test="next-datapoint-select"]').click();
      cy.get('.p-select-overlay')
        .should('be.visible')
        .within(() => {
          cy.contains('KPI Alpha Label').should('not.exist');
          cy.contains('KPI Beta Label').should('be.visible');
        });
    });

    it('shows reviewed KPIs in the dropdown after turning off the "only show unreviewed" toggle', () => {
      const judgementWithAlphaReviewed: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataPoints: {
          ...baseDatasetJudgement.dataPoints,
          [dataPointTypeId]: {
            ...baseDatasetJudgement.dataPoints[dataPointTypeId],
            acceptedSource: AcceptedDataPointSource.Original,
          },
        },
      };
      mountJudgeDialog({ datasetJudgement: judgementWithAlphaReviewed });

      cy.get('[data-test="only-unreviewed-toggle"]').click();

      cy.get('[data-test="next-datapoint-select"]').click();
      cy.get('.p-select-overlay').should('be.visible');

      cy.contains('KPI Alpha Label').should('be.visible');
      cy.contains('KPI Beta Label').should('be.visible');
    });
  });

  // ---------------------------------------------------------------------------
  // 12. overflow popup behavior
  // ---------------------------------------------------------------------------
  describe('Overflow behavior of contents', () => {
    it('Overflow behavior for original datapoint is behaving correctly', () => {
      mountJudgeDialog({ originalDataPointBody: overflowingOriginalDataPoint });
      cy.get('[data-test="original-datapoint-section"]').within(() => {
        cy.contains('th', 'Value')
          .parent('tr')
          .within(() => {
            cy.contains(overflowingValueEntry.substring(0, 10)).should('be.visible');
            cy.get('[data-test="value-overflow-icon"]').should('be.visible');
            cy.get('[data-test="value-overflow-icon"]').trigger('mouseenter');
          });
      });
    });
    overflowTestCases.forEach((testCase) => {
      checkOverflowBehavior(testCase);
    });
  });
});
