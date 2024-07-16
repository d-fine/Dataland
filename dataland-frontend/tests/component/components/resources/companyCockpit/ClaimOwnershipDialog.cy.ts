// @ts-nocheck
import ClaimOwnershipDialog from '@/components/resources/companyCockpit/ClaimOwnershipDialog.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

describe('Component test for ClaimOwnershipPanel', () => {
  beforeEach(() => {
    const mockApiResponse = { status: 200 };
    cy.intercept('**/company-ownership/*', mockApiResponse);
  });
  it('ClaimOwnershipPanel component first page works correctly', () => {
    cy.mountWithPlugins(ClaimOwnershipDialog, {
      data() {
        return {
          dialogIsVisible: true,
          companyName: 'TestClaimOwnershipDialogMessage',
          companyId: 'a8e513b8-c711-4dc0-915b-678c7758523d',
        };
      },
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.get('#claimOwnerShipDialog').should('exist').should('be.visible');
      cy.get("[data-test='claimOwnershipDialogMessage']").should(
        'contain.text',
        'Are you responsible for the datasets of TestClaimOwnershipDialogMessage? Claim company ownership in order to ensure high'
      );

      cy.get("textarea[name='claimOwnershipMessage']")
        .type('THIS IS A TEST MESSAGE')
        .should('have.value', 'THIS IS A TEST MESSAGE');

      cy.get('.p-dialog-footer .p-button-label').should('contain.text', 'SUBMIT');

      cy.get('.p-dialog-footer button').click();
    });
  });
  it('ClaimOwnershipPanel component second page works correctly', () => {
    cy.mountWithPlugins(ClaimOwnershipDialog, {
      data() {
        return {
          dialogIsVisible: true,
          companyName: 'TestClaimOwnershipDialogMessage',
          companyId: 'a8e513b8-c711-4dc0-915b-678c7758523d',
          claimIsSubmitted: true,
        };
      },
      keycloak: minimalKeycloakMock({}),
    }).then(({ wrapper }) => {
      cy.get("[data-test='claimOwnershipDialogSubmittedMessage']").should(
        'contain.text',
        'We will reach out to you soon via email.'
      );
      cy.get('.p-dialog-footer .p-button-label').should('contain.text', 'CLOSE');

      cy.get('.p-dialog-footer button')
        .click()
        .then(() => {
          expect(wrapper.emitted('closeDialog'));
        });
    });
  });
  it('ClaimOwnershipPanel component page works correctly when closed', () => {
    cy.mountWithPlugins(ClaimOwnershipDialog, {
      data() {
        return {
          dialogIsVisible: false,
        };
      },
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.get('#claimOwnerShipDialog').should('not.exist');
    });
  });
});
