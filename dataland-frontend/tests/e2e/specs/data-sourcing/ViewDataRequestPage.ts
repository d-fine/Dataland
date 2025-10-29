import { admin_name, admin_pw, getBaseUrl, uploader_name, uploader_pw } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import type { CompanyIdAndName } from '@clients/backend';
import { fetchTestCompanies } from '@e2e/utils/CompanyCockpitPage/CompanyCockpitUtils.ts';

const testYear = '2023';
const testMessage = 'Frontend test message';
let alphaCompanyIdAndName: CompanyIdAndName;
let requestId: string;

const apiBaseUrl = getBaseUrl();

/**
 * Creates a data sourcing request as uploader and returns the requestId.
 */
function createRequest(): Cypress.Chainable<string> {
  return getKeycloakToken(uploader_name, uploader_pw).then((token) => {
    return cy
      .request({
        method: 'POST',
        url: `${apiBaseUrl}/data-sourcing/requests`,
        headers: { Authorization: `Bearer ${token}` },
        body: {
          companyIdentifier: alphaCompanyIdAndName.companyId,
          dataType: 'pcaf',
          reportingPeriod: testYear,
          memberComment: testMessage,
        },
      })
      .then((createResp) => {
        return createResp.body.requestId || createResp.body.id;
      });
  });
}

/**
 * Patches the request to 'Processed' state as admin.
 */
function patchRequestToProcessed(requestId: string): Cypress.Chainable {
  return getKeycloakToken(admin_name, admin_pw).then((token) => {
    return cy.request({
      method: 'PATCH',
      url: `${apiBaseUrl}/data-sourcing/requests/${requestId}/state?requestState=Processed&adminComment=`,
      headers: { Authorization: `Bearer ${token}` },
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
    createRequest().then((id) => {
      requestId = id;
      patchRequestToProcessed(requestId).then(() => {
        cy.visit(getBaseUrl() + `/requests/${requestId}`);
      });
    });
  });

  it.only('should open and close the resubmit modal', () => {
    cy.get('[data-test="card-resubmit"]').should('be.visible');
      cy.pause();
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

  it('should check for correct display of request details', () => {
    cy.get('[data-test="request-details-company"]').should('contain.text', alphaCompanyIdAndName.companyName);
    cy.get('[data-test="request-details-year"]').should('contain.text', testYear);
    cy.get('[data-test="request-details-type"]').should('contain.text', 'pcaf');
    cy.get('[data-test="card_requestIs"] .dataland-inline-tag').should('contain.text', 'Processed');
    cy.get('[data-test="request-details-email"]').should('contain.text', uploader_name);
  });
});
