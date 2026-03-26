import JudgeDialog from '@/components/resources/datasetReview/JudgeDialog.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { AcceptedDataPointSource, DatasetJudgementState, QaReportDataPointVerdict } from '@clients/qaservice';
import type { DatasetJudgementResponse, DataPointJudgement } from '@clients/qaservice';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { computed } from 'vue';
import type Keycloak from 'keycloak-js';
import type { DocumentOption } from '@/components/resources/datasetReview/JudgeDialogTypes.ts';
import type { CellRow } from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import { MLDTDisplayObjectForEmptyString } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { KEYCLOAK_ROLE_JUDGE } from '@/utils/KeycloakRoles.ts';

// ===== Shared test data =====

const datasetJudgementId = 'test-judgement-id';
const dataPointTypeId = 'kpiAlpha';
const dataPointId = 'dp-id-alpha';
const secondDataPointTypeId = 'kpiBeta';
const secondDataPointId = 'dp-id-beta';

const reporterUserId1 = 'reporter-user-id-1';
const reporterUserId2 = 'reporter-user-id-2';

const originalDataPoint = {
  value: 'original-value',
  quality: 'Audited',
  comment: 'original-comment',
  dataSource: { fileName: 'original-doc.pdf', page: '3' },
};

const correctedDataPoint = {
  value: 'corrected-value',
  quality: 'Estimated',
  comment: 'corrected-comment',
  dataSource: { fileName: 'corrected-doc.pdf', page: '7' },
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

const availableDocuments: DocumentOption[] = [
  {
    label: 'Annual Report 2023',
    value: 'annual-report-2023',
    dataSource: { fileName: 'AnnualReport2023.pdf', fileReference: 'ref-123' },
  },
  {
    label: 'Sustainability Report',
    value: 'sustainability-report',
    dataSource: { fileName: 'SustainabilityReport.pdf', fileReference: 'ref-456' },
  },
];

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
  dataPointTypeId?: string;
  kpiRows?: CellRow[];
  availableDocuments?: DocumentOption[];
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
      dataPoint: JSON.stringify(originalDataPoint),
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
    req.reply({
      statusCode: options?.patchStatusCode ?? 200,
      body: judgement,
    });
  }).as('patchJudgementDetail');

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
      availableDocuments: options?.availableDocuments ?? availableDocuments,
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
        cy.contains('3').should('be.visible');
        cy.contains('original-comment').should('be.visible');
      });
    });

    it('displays corrected data point fields from the QA report', () => {
      mountJudgeDialog();

      cy.get('[data-test="corrected-datapoint-section"]').within(() => {
        cy.contains('corrected-value').should('be.visible');
        cy.contains('Estimated').should('be.visible');
        cy.contains('corrected-doc.pdf').should('be.visible');
        cy.contains('7').should('be.visible');
        cy.contains('corrected-comment').should('be.visible');
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

    it('shows "QA INCONCLUSIVE" badge when no report is rejected but not all are accepted', () => {
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

      cy.get('[data-test="verdict-badge"]').should('contain.text', 'QA INCONCLUSIVE');
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

      it('advances the header to the next KPI after a successful accept', () => {
        mountJudgeDialog();

        cy.contains('KPI Alpha Label').should('be.visible');

        cy.get('[data-test="accept-original-button"]').click();
        cy.wait('@patchJudgementDetail');

        cy.contains('KPI Beta Label').should('be.visible');
        cy.contains('KPI Alpha Label').should('not.exist');
      });
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
      cy.get('[data-test="custom-comment-field"]').should('have.value', 'original-comment');
    });

    it('copies the corrected data point into the custom form fields', () => {
      mountJudgeDialog();

      cy.get('[data-test="copy-corrected-to-custom"]').click();

      cy.get('[data-test="custom-value-field"]').should('have.value', 'corrected-value');
      cy.get('[data-test="custom-pages-field"]').should('have.value', '7');
      cy.get('[data-test="custom-comment-field"]').should('have.value', 'corrected-comment');
    });

    it('copies the original data point into the JSON textarea when in JSON mode', () => {
      mountJudgeDialog();
      cy.wait('@getOriginalDataPoint');

      cy.get('[data-test="edit-mode-toggle"]').click();
      cy.get('[data-test="copy-original-to-custom"]').click();

      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'original-value');
    });

    it('copies the corrected data point into the JSON textarea when in JSON mode', () => {
      mountJudgeDialog();

      cy.get('[data-test="edit-mode-toggle"]').click();
      cy.get('[data-test="copy-corrected-to-custom"]').click();

      cy.get('[data-test="custom-json-textarea"]').should('contain.value', 'corrected-value');
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
    });

    it('pre-populates the custom JSON textarea in JSON mode with a previously accepted custom value', () => {
      const previousCustomValue = { value: 'json-pre-populated-value', quality: 'Audited' };

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
    });
  });

  // ---------------------------------------------------------------------------
  // 7. Custom data point field: access to correct documents
  // ---------------------------------------------------------------------------
  describe('Custom data point document access', () => {
    it('shows the available documents in the document select dropdown', () => {
      mountJudgeDialog();

      cy.get('[data-test="custom-document-field"]').click();
      cy.get('.p-select-overlay').should('be.visible');

      cy.contains('Annual Report 2023').should('be.visible');
      cy.contains('Sustainability Report').should('be.visible');
    });

    it('uses the selected document dataSource when building the custom datapoint JSON', () => {
      mountJudgeDialog();

      cy.get('[data-test="custom-value-field"]').clear().type('doc-test-value');
      cy.get('[data-test="custom-document-field"]').click();
      cy.get('.p-select-overlay').should('be.visible');
      cy.contains('Annual Report 2023').click();

      cy.get('[data-test="accept-custom-button"]').click();

      cy.wait('@patchJudgementDetail').then((interception) => {
        const parsed = JSON.parse(interception.request.body.customDataPoint);
        expect(parsed.dataSource.fileName).to.eq('AnnualReport2023.pdf');
      });
    });

    it('shows no documents in the dropdown when none are provided', () => {
      mountJudgeDialog({ availableDocuments: [] });

      cy.get('[data-test="custom-document-field"]').click();
      cy.get('.p-select-overlay').should('be.visible');

      cy.contains('Annual Report 2023').should('not.exist');
      cy.contains('Sustainability Report').should('not.exist');
    });
  });

  // ---------------------------------------------------------------------------
  // 8. Error message when PATCH fails
  // ---------------------------------------------------------------------------
  describe('PATCH error handling', () => {
    it('shows an error message when the PATCH request fails', () => {
      mountJudgeDialog({ patchStatusCode: 500 });

      cy.get('[data-test="accept-original-button"]').click();

      cy.get('[data-test="judge-modal-patch-error"]')
        .should('be.visible')
        .and('contain.text', 'Failed to update datapoint judgement. Please try again.');
    });

    it('does not show an error message on initial load without any action', () => {
      mountJudgeDialog();

      cy.get('[data-test="judge-modal-patch-error"]').should('not.exist');
    });
  });

  // ---------------------------------------------------------------------------
  // 9. Multi-report navigation and QaAccepted filtering
  // ---------------------------------------------------------------------------
  describe('Corrected datapoint QA report navigation', () => {
    const secondCorrectedDataPoint = {
      value: 'second-corrected-value',
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
        cy.contains('second-corrected-value').should('be.visible');
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

    it.skip('excludes QaAccepted reports from the corrected section', () => {
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
        cy.contains('1 / 1').should('not.exist');
        cy.contains('Reporter Two').should('be.visible');
        cy.contains('second-corrected-value').should('be.visible');
        cy.contains('Reporter One').should('not.exist');
      });
    });
  });

  // ---------------------------------------------------------------------------
  // 10. "Go To" advances the header KPI label
  // ---------------------------------------------------------------------------
  describe('Go To navigation', () => {
    it('updates the dialog title to the selected KPI after clicking Go To', () => {
      mountJudgeDialog();

      cy.get('.p-dialog-title').should('have.text', 'KPI Alpha Label');

      cy.get('[data-test="next-datapoint-select"]').click();
      cy.get('.p-select-overlay').should('be.visible');
      cy.contains('KPI Beta Label').click();
      cy.get('[data-test="go-to-datapoint-button"]').click();

      cy.get('.p-dialog-title').should('have.text', 'KPI Beta Label');
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

  describe('Failed patch', () => {
    it('shows a popup modal when the PATCH request fails and close using confirm', () => {
      mountJudgeDialog({ patchStatusCode: 500 });
      cy.get('[data-test="accept-original-button"]').click();
      cy.get('[data-test="confirmation-modal-error-message"]').should('contain', '500');
      cy.get('[data-test="ok-confirmation-modal-button"]').click();
      cy.get('[data-test="confirmation-modal"]').should('not.exist');
    });
  });
});
