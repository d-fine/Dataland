import { type CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import CompanyCockpitPage from '@/components/pages/CompanyCockpitPage.vue';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { computed, ref } from 'vue';
import { type AggregatedFrameworkDataSummary, type CompanyInformation, type DataTypeEnum } from '@clients/backend';

const dummyUserId = 'mock-user-id';
const dummyFirstName = 'mock-first-name';
const dummyEmail = 'mock@Company.com';

const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
const dummyDocumentIds = [
  'a12d2cd3014c2601e6d3e32a7f0ec92fe3e5a9a8519519d93b8bb7c56141849d',
  'e0dfbaf044f44cacbb304a4686d890205a9f1acc493a4eb290ca355fb9e56dcf',
  'e30cf2dfd5ef4a6fb347bcca75e79f316f7e79399139462491f31baf1fdfe4f7',
];
const dummyPublicationDates = ['2025-02-25', '2024-01-13', undefined];
const dummyReportingPeriods = ['2025', '2024', '2023'];

/**
 * Generates a company role assignment
 * @param companyRole in the mock assignment
 * @param companyId of the company associated with the mock assignment
 * @returns a mock company role assignment
 */
export function generateCompanyRoleAssignment(
  companyRole: CompanyRole,
  companyId: string
): CompanyRoleAssignmentExtended {
  return {
    companyRole: companyRole,
    companyId: companyId,
    userId: dummyUserId,
    firstName: dummyFirstName,
    email: dummyEmail,
  };
}

/**
 * Generates a dummy document metainformation in response to an HTTP query.
 * @param index ranges inclusively from 0 to 2
 * @param query the associated HTTP query
 */
function generateDocumentMetaInformation(
  index: number,
  query: Record<string, string | number>
): {
  documentId: string;
  documentName: string;
  documentCategory: string;
  companyIds: string[];
  publicationDate: string | undefined;
  reportingPeriod: string;
} {
  return {
    documentId: dummyDocumentIds[index]!,
    documentName: 'test_' + (query['documentCategories'] ?? 'document') + `_${index + 1}`,
    documentCategory: (query['documentCategories'] as string) ?? 'AnnualReport',
    companyIds: [(query['companyId'] as string) ?? '???'],
    publicationDate: dummyPublicationDates[index],
    reportingPeriod: dummyReportingPeriods[index]!,
  };
}

/**
 * Mocks API requests that are triggered when the component is mounted.
 *
 * @param {boolean} hasCompanyAtLeastOneOwner - Indicates if the company has at least one owner.
 * @param {CompanyInformation} companyInformationForTest - Mock details of the company information to be returned.
 * @param {Map<DataTypeEnum, AggregatedFrameworkDataSummary>} mockMapOfDataTypeToAggregatedFrameworkDataSummary
 * - A mapping of data types to the corresponding aggregated framework data summary for testing.
 */
export function mockRequestsOnMounted(
  hasCompanyAtLeastOneOwner: boolean,
  companyInformationForTest: CompanyInformation,
  mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<DataTypeEnum, AggregatedFrameworkDataSummary>
): void {
  cy.intercept(`**/api/companies/*/info`, {
    body: companyInformationForTest,
    times: 1,
  }).as('fetchCompanyInfo');
  cy.intercept('**/api/companies/*/aggregated-framework-data-summary', {
    body: mockMapOfDataTypeToAggregatedFrameworkDataSummary,
    times: 1,
  }).as('fetchAggregatedFrameworkMetaInfo');
  const hasCompanyAtLeastOneOwnerStatusCode = hasCompanyAtLeastOneOwner ? 200 : 404;
  cy.intercept('**/community/company-ownership/*', {
    statusCode: hasCompanyAtLeastOneOwnerStatusCode,
  }).as('fetchCompanyOwnershipExistence');
  cy.intercept('HEAD', `/community/company-role-assignments/CompanyOwner/${dummyCompanyId}/${dummyUserId}`, {
    statusCode: 200,
  }).as('checkUserCompanyOwnerRole');
  cy.intercept('GET', '**/documents/**', (request) => {
    request.reply({
      statusCode: 200,
      body: [
        generateDocumentMetaInformation(0, request.query),
        generateDocumentMetaInformation(1, request.query),
        generateDocumentMetaInformation(2, request.query),
      ],
    });
  }).as('fetchDocumentMetadata');
}

/**
 * Mounts the company cockpit page with a specific authentication
 * @param isLoggedIn determines if the mount shall happen from a logged-in users perspective
 * @param isMobile determines if the mount shall happen from a mobile-users perspective
 * @param keycloakRoles defines the keycloak roles of the user if the mount happens from a logged-in users perspective
 * @param companyRoleAssignments defines the company role assignments that the current user shall have
 * @returns the mounted component
 */
export function mountCompanyCockpitWithAuthentication(
  isLoggedIn: boolean,
  isMobile: boolean,
  keycloakRoles?: string[],
  companyRoleAssignments?: CompanyRoleAssignmentExtended[]
): Cypress.Chainable {
  const chainable = getMountingFunction({
    keycloak: minimalKeycloakMock({
      authenticated: isLoggedIn,
      roles: keycloakRoles,
      userId: dummyUserId,
    }),
  })(CompanyCockpitPage, {
    global: {
      provide: {
        useMobileView: computed((): boolean => isMobile),
        companyRoleAssignments: ref(companyRoleAssignments),
      },
    },
    props: {
      companyId: dummyCompanyId,
    },
  });
  cy.wait('@fetchCompanyInfo');
  cy.wait('@fetchAggregatedFrameworkMetaInfo');
  cy.wait('@fetchCompanyOwnershipExistence');
  return chainable;
}

/**
 * Validates the vsme framework summary panel
 * @param isUserCompanyOwner is the current user company owner
 */
export function validateVsmeFrameworkSummaryPanel(isUserCompanyOwner: boolean): void {
  const frameworkName = 'vsme';
  const frameworkSummaryPanelSelector = `div[data-test="${frameworkName}-summary-panel"]`;
  if (isUserCompanyOwner) {
    cy.get(`${frameworkSummaryPanelSelector} [data-test="${frameworkName}-provide-data-button"]`).should('exist');
  } else {
    cy.get(`${frameworkSummaryPanelSelector} [data-test="${frameworkName}-provide-data-button"]`).should('not.exist');
  }
}
