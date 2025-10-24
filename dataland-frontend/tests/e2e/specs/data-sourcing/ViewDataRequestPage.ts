import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import type { CompanyIdAndName } from '@clients/backend';
import { fetchTestCompanies } from '@e2e/utils/CompanyCockpitPage/CompanyCockpitUtils.ts';

const testYear = '2023';
const testMessage = 'Frontend test message';
let alphaCompanyIdAndName: CompanyIdAndName;
let requestId: string;

const apiBaseUrl = getBaseUrl();

/**
 * Creates a data sourcing request, patches it to 'Processed' state, and visits its page.
 */
function createRequestAndPatchItAndVisit(): void {
  getKeycloakToken(admin_name, admin_pw).then((token) => {
    cy.request({
      method: 'POST',
      url: `${apiBaseUrl}/data-sourcing/requests`,
      headers: { Authorization: `Bearer ${token}` },
      body: {
        companyIdentifier: alphaCompanyIdAndName.companyId,
        dataType: 'pcaf',
        reportingPeriod: testYear,
        memberComment: testMessage,
      },
    }).then((createResp) => {
      requestId = createResp.body.requestId || createResp.body.id;
      cy.request({
        method: 'PATCH',
        url: `${apiBaseUrl}/data-sourcing/requests/${requestId}/state?requestState=Processed&adminComment=`,
        headers: { Authorization: `Bearer ${token}` },
      }).then(() => {
        cy.visit(getBaseUrl() + `/requests/${requestId}`);
      });
    });
  });
}

describe('ViewDataRequestPage', () => {
  before(() => {
    fetchTestCompanies().then(([alpha]) => {
      alphaCompanyIdAndName = alpha;
    });
  });
  beforeEach(() => {
    cy.ensureLoggedIn(admin_name, admin_pw);
    createRequestAndPatchItAndVisit();
  });

  it('should open and close the resubmit modal', () => {
    cy.get('[data-test="card-resubmit"]').should('be.visible');
    cy.get('[data-test="resubmit-request-button"]').click();
    cy.get('[data-test="resubmit-modal"]').should('be.visible');
    cy.get('[data-test="resubmit-message"]').type('Resubmitting for more data.');
    cy.get('[data-test="resubmit-confirmation-button"]').click();
    cy.get('.p-dialog').should('contain.text', 'successfully resubmitted');
    cy.get('[data-test="close-success-modal-button"]').should('be.visible').click();
    cy.url().should('not.include', requestId);
    cy.get('[data-test="card_requestIs"] .dataland-inline-tag').should('exist').should('contain.text', 'Open');
  });

  it('should open and close the withdraw modal', () => {
    cy.get('[data-test="card_withdrawn"]').should('be.visible');
    cy.get('[data-test="withdraw-request-button"]').click();
    cy.get('.p-dialog').should('contain.text', 'successfully withdrawn');
    cy.get('[data-test="card_requestIs"] .dataland-inline-tag').should('contain.text', 'Withdrawn');
  });
});
