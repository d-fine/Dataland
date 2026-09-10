import JudgeDialogNextSection from '@/components/resources/datasetReview/JudgeDialogNextSection.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { computed } from 'vue';
import { KEYCLOAK_ROLE_JUDGE } from '@/utils/KeycloakRoles.ts';
import type { PreApprovalConfig } from '@clients/qaservice';

// ===== Shared test data =====

const dataPointTypeId = 'kpiAlpha';
const dataType = 'sfdr';

const allPassingPreApprovalCheckResults = {
  areAllQaReportsAccepted: true,
  dataPointEligible: true,
  passesRandomSampling: true,
  passesSignificanceCheck: true,
};

const baseConfig: PreApprovalConfig = {
  exemptFields: {},
  samplingProbability: 0.25,
  decimalRelativeThreshold: 0.5,
  integerAbsoluteThreshold: 5,
  individualDecimalThresholds: {},
  individualIntegerThresholds: {},
  autoPreApprovalEnabled: true,
};

// ===== Mount helper =====

/**
 * Mounts the JudgeDialogNextSection component with standard intercepts and Vue Query.
 */
function mountJudgeDialogNextSection(options?: {
  preApprovalConfig?: PreApprovalConfig;
  delayConfigResponseMs?: number;
}): void {
  cy.intercept('GET', `**/qa/pre-approval/config`, {
    statusCode: 200,
    body: options?.preApprovalConfig ?? baseConfig,
    delay: options?.delayConfigResponseMs ?? 0,
  }).as('getPreApprovalConfig');

  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  const mount = getMountingFunction();
  const keycloakMock = minimalKeycloakMock({ roles: [KEYCLOAK_ROLE_JUDGE] });
  const keycloakPromise = Promise.resolve(keycloakMock);
  const apiClientProvider = new ApiClientProvider(keycloakPromise);

  mount(JudgeDialogNextSection, {
    props: {
      options: [],
      onlyShowUnreviewed: true,
      selectedNextDataPointTypeId: null,
      preApprovalCheckResults: allPassingPreApprovalCheckResults,
      dataPointTypeId: dataPointTypeId,
      dataType: dataType,
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
}

// ===== Tests =====

describe('JudgeDialogNextSection component tests', () => {
  describe('Pre-approval info dialog thresholds', () => {
    it('shows the global thresholds when no individual override exists', () => {
      mountJudgeDialogNextSection();
      cy.wait('@getPreApprovalConfig');

      cy.get('button[aria-label="Pre-approval info"]').click();

      cy.contains('50 %').should('be.visible');
      cy.contains('more than 5 for integer fields').should('be.visible');
    });

    it('shows the individual override thresholds when configured for the given dataType/dataPointTypeId', () => {
      const configWithOverrides: PreApprovalConfig = {
        ...baseConfig,
        individualDecimalThresholds: { [dataType]: { [dataPointTypeId]: 0.2 } },
        individualIntegerThresholds: { [dataType]: { [dataPointTypeId]: 3 } },
      };
      mountJudgeDialogNextSection({ preApprovalConfig: configWithOverrides });
      cy.wait('@getPreApprovalConfig');

      cy.get('button[aria-label="Pre-approval info"]').click();

      cy.contains('20 %').should('be.visible');
      cy.contains('more than 3 for integer fields').should('be.visible');
      cy.contains('50 %').should('not.exist');
      cy.contains('more than 5 for integer fields').should('not.exist');
    });

    it('shows "unknown" while the config is loading', () => {
      // A generous delay to safely exercise the pending state before the response resolves.
      mountJudgeDialogNextSection({ delayConfigResponseMs: 1000 });

      cy.get('button[aria-label="Pre-approval info"]').click();

      cy.contains('unknown deviation for numerical fields').should('be.visible');
      cy.contains('more than unknown for integer fields').should('be.visible');

      cy.wait('@getPreApprovalConfig');

      cy.contains('50 %').should('be.visible');
      cy.contains('more than 5 for integer fields').should('be.visible');
    });
  });
});
