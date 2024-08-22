// @ts-nocheck
import ClaimOwnershipPanel from '@/components/resources/companyCockpit/ClaimOwnershipPanel.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

describe('Component test for ClaimOwnershipPanel', () => {
  it('ClaimOwnershipPanel component works correctly for authenticated user', () => {
    cy.mountWithPlugins(ClaimOwnershipPanel, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          companyName: 'TestClaimOwnershipPanelCompany',
        };
      },
    }).then(() => {
      cy.get("[data-test='claimOwnershipPanelHeading']").should(
        'have.text',
        'Responsible for TestClaimOwnershipPanelCompany?'
      );
      cy.get("[data-test='claimOwnershipPanelLink']")
        .should('have.text', ' Claim company ownership. ')
        .click()
        .get('#claimOwnerShipDialog')
        .should('exist')
        .should('be.visible');
    });
  });
  it('ClaimOwnershipPanel component works correctly for non authenticated user', () => {
    const mockedKeycloak = minimalKeycloakMock({ authenticated: false });
    mockedKeycloak.register = function (): Promise<void> {
      return Promise.resolve();
    };
    const registerSpy = cy.spy(mockedKeycloak, 'register');
    cy.mountWithPlugins(ClaimOwnershipPanel, {
      keycloak: mockedKeycloak,
      data() {
        return {
          companyName: 'TestClaimOwnershipPanelCompany',
        };
      },
    }).then(() => {
      cy.get("[data-test='claimOwnershipPanelHeading']").should(
        'have.text',
        'Responsible for TestClaimOwnershipPanelCompany?'
      );
      cy.get("[data-test='claimOwnershipPanelLink']").should('have.text', ' Claim company ownership. ').click();
      cy.get("[data-test='claimOwnershipPanelLink']")
        .should('have.text', ' Claim company ownership. ')
        .click()
        .get('#claimOwnerShipDialog')
        .should('not.exist');
      cy.wrap(registerSpy).should('have.been.called');
    });
  });
});
